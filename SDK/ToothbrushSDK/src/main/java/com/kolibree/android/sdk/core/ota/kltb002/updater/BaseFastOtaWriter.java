package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.TimberTagKt.otaTagFor;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_COMPLETED_PROGRESS;
import static com.kolibree.android.sdk.core.binary.PayloadWriter.MAX_UNSIGNED_SHORT;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.OtaWriterStatus.WRITE_COMPLETED;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.OtaWriterStatus.WRITE_IN_PROGRESS;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.NextObjectHeaderKt.BYTES_PER_CHUNK;
import static java.lang.Math.max;
import static java.lang.Math.min;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import timber.log.Timber;

abstract class BaseFastOtaWriter implements OtaWriter {

  @VisibleForTesting static final byte COMMAND_ID_NEXT_OBJECT_HEADER = 0x04;

  /*
   * Send 1 command every 4 with WRITE_WITH_RESPONSE to avoid disconnect with status=8
   *
   * Check https://github.com/kolibree-git/android-modules-SDK/pull/485 for explanation
   */
  @VisibleForTesting static final int CHUNK_WITH_RESPONSE_MAX_INTERVAL = 4;

  /*
   * We don't want to wait for response for every command
   */
  @VisibleForTesting static final int CHUNK_WITH_RESPONSE_MIN_INTERVAL = 2;

  /*
   * Details can be found in the Kolibree GATT OTA Update Specification
   *
   * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=0&range=G14
   */
  @VisibleForTesting static final int STATUS_RESPONSE_READY_FOR_NEXT_HEADER = 0x01;

  @VisibleForTesting static final int STATUS_RESPONSE_READY_FOR_NEXT_OBJECT = 0x02;

  @VisibleForTesting static final int STATUS_RESPONSE_OBJECT_TIMEOUT = 0x03;

  @VisibleForTesting static final int STATUS_RESPONSE_OBJECT_CRC_ERROR = 0x04;

  @VisibleForTesting static final int STATUS_RESPONSE_FULL_CRC_ERROR = 0x05;

  @VisibleForTesting static final int STATUS_RESPONSE_UPDATE_FINISHED = 0x06;

  @VisibleForTesting static final int STATUS_RESPONSE_UPDATE_TIMEOUT = 0x07;

  @VisibleForTesting static final int STATUS_RESPONSE_PROTOCOL_ERROR = 0x08;

  @VisibleForTesting static final int STATUS_RESPONSE_INCOMPATIBLE_MAINAPP = 0x09;

  private static final String TAG = otaTagFor(BaseFastOtaWriter.class);
  @VisibleForTesting final CompositeDisposable disposables = new CompositeDisposable();
  @VisibleForTesting final CompositeDisposable sendChunksDisposables = new CompositeDisposable();
  @VisibleForTesting final PublishSubject<Integer> updateEventSubject = PublishSubject.create();
  /** {@link BleDriver} implementation */
  private final BleDriver driver;

  private final int attempt;

  private final Scheduler singleScheduler;
  @VisibleForTesting OtaWriterStatus status;
  @VisibleForTesting OtaUpdate otaUpdate;
  /** Tracks how many chunks to send in total */
  @VisibleForTesting int totalChunks;
  /** Tracks how many chunks in total have been successfully transmitted. */
  @VisibleForTesting int totalChunksTransmitted;
  /**
   * Tracks how many chunks have been transmitted for the current Object. This is reset to 0 for
   * each Object.
   */
  @VisibleForTesting int currentObjectChunksTransmitted;
  /** Tracks the total number of chunks to be transmitted for the current Object. */
  @VisibleForTesting int currentObjectTotalChunks;

  BaseFastOtaWriter(BleDriver driver, Scheduler singleScheduler, int attempt) {
    this.driver = driver;
    this.singleScheduler = singleScheduler;
    this.attempt = attempt;
  }

  BaseFastOtaWriter(BleDriver driver, Scheduler singleScheduler) {
    this(driver, singleScheduler, 0);
  }

  protected final BleDriver driver() {
    return driver;
  }

  @NonNull
  @Override
  public Observable<Integer> write(@NonNull OtaUpdate otaUpdate) {
    this.otaUpdate = otaUpdate;

    totalChunks = countChunks();

    /*
    we don't want this Observable to complete when reaching 100. We delegate that to monitorOtaWriteObservable
     */
    return validateUpdatePreconditionsObservable()
        .concatWith(Observable.merge(updateEventsFromBLEObservable(), updateEventSubject))
        .doOnTerminate(this::close)
        .doOnDispose(this::close);
  }

  @VisibleForTesting
  int countChunks() {
    byte[] dataToTransmit = otaUpdate.getData();

    return dataToTransmit.length / BYTES_PER_CHUNK
        + (dataToTransmit.length % BYTES_PER_CHUNK == 0 ? 0 : 1);
  }

  @VisibleForTesting
  Observable<Integer> updateEventsFromBLEObservable() {
    return enableOtaStatusNotifications()
        .observeOn(singleScheduler)
        .doOnSubscribe(ignore -> startUpdate())
        .map(this::onNewOtaStatus)
        .toObservable();
  }

  @VisibleForTesting
  Flowable<byte[]> enableOtaStatusNotifications() {
    return driver.otaUpdateStatusCharacteristicChangedFlowable();
  }

  @VisibleForTesting
  void startUpdate() {
    status = WRITE_IN_PROGRESS;

    sendStartUpdateCommand();
  }

  @VisibleForTesting
  void sendStartUpdateCommand() {
    byte[] prepareStart = prepareStartOTACommand();

    DisposableUtils.addSafely(
        disposables,
        driver
            .writeOtaUpdateStartCharacteristic(prepareStart)
            .onTerminateDetach()
            .subscribe(() -> {}, updateEventSubject::onError));
  }

  @NonNull
  @VisibleForTesting
  byte[] prepareStartOTACommand() {
    /*
    Length: 16
    Payload:
    bytes 0-3 (uint32 little endian): The CRC32 of the complete image that will be transmitted.
    bytes 4-7 (uint32 little endian): The total length of the image to be transmitted. This will be used at the end of the update to check the flash against the given CRC32.
    byte 8: target image's version - major
    byte 9: target image's version - minor
    bytes 10-11 (uint16 little endian): target image's version - revision
    bytes 12-13 (uint16 little endian): hw version - major
    bytes 14-15 (uint16 little endian): hw version - minor
     */
    return new PayloadWriter(17)
        .writeByte(getStartOTACommandId())
        .writeInt32((int) otaUpdate.getCrc())
        .writeInt32(otaUpdate.getData().length)
        .writeSoftwareVersion(otaUpdate.getVersion())
        .writeHardwareVersion(driver.getHardwareVersion())
        .getBytes();
  }

  Integer onNewOtaStatus(byte[] toothbrushOTAStatusPayload) {
    int toothbrushOtaStatus = readToothbrushStatus(toothbrushOTAStatusPayload);

    Timber.tag(TAG).d("Received new ota status %s", toothbrushOtaStatus);

    switch (toothbrushOtaStatus) {
      case STATUS_RESPONSE_READY_FOR_NEXT_HEADER:
        sendStartNextObjectTransmission();
        break;
      case STATUS_RESPONSE_READY_FOR_NEXT_OBJECT:
        sendNextGroup();
        break;
      case STATUS_RESPONSE_OBJECT_TIMEOUT:
        onObjectTimeout();

        retryLastObject();
        break;
      case STATUS_RESPONSE_UPDATE_FINISHED:
        onWriteCompleted();
        break;
      case STATUS_RESPONSE_OBJECT_CRC_ERROR:
        onObjectError();

        sendStartNextObjectTransmission();
        break;
      case STATUS_RESPONSE_UPDATE_TIMEOUT:
      case STATUS_RESPONSE_FULL_CRC_ERROR:
      case STATUS_RESPONSE_PROTOCOL_ERROR:
      case STATUS_RESPONSE_INCOMPATIBLE_MAINAPP:
      default:
        onWriteError(toothbrushOtaStatus);
    }

    return calculateProgress();
  }

  @VisibleForTesting
  void onObjectError() {
    driver.cancelPendingOperations();

    sendChunksDisposables.clear();
  }

  @VisibleForTesting
  void onObjectTimeout() {
    onObjectError();
  }

  @VisibleForTesting
  void onWriteCompleted() {
    status = OtaWriterStatus.WRITE_COMPLETED;
  }

  @VisibleForTesting
  void onWriteError(int toothbrushOtaStatus) {
    onObjectError();

    status = OtaWriterStatus.WRITE_ERROR;

    Timber.tag(TAG).e("Status error received %s", toothbrushOtaStatus);

    updateEventSubject.onError(
        new IllegalStateException("Toothbrush returned OTA status " + toothbrushOtaStatus));
  }

  @VisibleForTesting
  Integer calculateProgress() {
    if (status == WRITE_COMPLETED) {
      return OTA_COMPLETED_PROGRESS;
    }

    if (totalChunks == 0) {
      return 0;
    }

    return (int) (totalChunksTransmitted * 100f / totalChunks);
  }

  @FastFirmwareWriter.StatusResponse
  private int readToothbrushStatus(byte[] newStatusPayload) {
    return newStatusPayload[1];
  }

  @VisibleForTesting
  void retryLastObject() {
    currentObjectChunksTransmitted = 0;

    sendStartNextObjectTransmission();
  }

  @VisibleForTesting
  void sendStartNextObjectTransmission() {
    adjustTotals();

    int chunksRemaining = chunksRemaining();
    if (chunksRemaining <= 0) {
      return;
    }

    NextObjectHeader nextObjectHeader = createNextObjectHeader(chunksRemaining);

    sendNextObjectHeader(nextObjectHeader);
  }

  @NonNull
  @VisibleForTesting
  NextObjectHeader createNextObjectHeader(int chunksRemaining) {
    return NextObjectHeader.create(otaUpdate.getData(), totalChunksTransmitted, chunksRemaining);
  }

  @VisibleForTesting
  int chunksRemaining() {
    return max(0, totalChunks - totalChunksTransmitted);
  }

  @VisibleForTesting
  void adjustTotals() {
    if (currentObjectChunksTransmitted > 0) {
      totalChunksTransmitted += currentObjectChunksTransmitted;
    }
  }

  @VisibleForTesting
  void sendNextObjectHeader(NextObjectHeader header) {
    currentObjectChunksTransmitted = 0;

    currentObjectTotalChunks = header.numberOfChunksToSend();

    int timeout = calculateObjectTimeout();

    Timber.tag(TAG).d("Timeout is %s", timeout);
    Timber.tag(TAG).d("For totalChunks %s, crc is %s", totalChunksTransmitted, header.crc32());

    /*
    Send Start Next Object Transmission

    byte 0: (bool) Is last object. If 0, the firmware will send a Satus update and wait for a Start Next Object Transmission command after it
    byte 1-2: Number of chunks in the object. This must be <= 800.  All chunks are 20 bytes long except for the last one which can be shorter.
    byte 3: number of bytes in the last chunk (considered only if isLastObject is true).
    bytes 4-5 (uint16 little endian): Object transmission timeout in milliseconds. This should be set according to the connection interval and number of packets per connection event. At 32ms connection interval (iOS typical) and 5 packets per connection event, 4000ms is a good value. If this timeout is reached and the bootloader hasn't received all the chunks, it will update the status characteristic with the corresponding error.
    bytes 6-9 (uint32 little endian): The CRC32 of the object.
     */
    PayloadWriter writer =
        new PayloadWriter(11)
            .writeByte(COMMAND_ID_NEXT_OBJECT_HEADER)
            .writeByte((byte) (header.isLastObject() ? 1 : 0))
            .writeUnsignedInt16(header.numberOfChunksToSend())
            .writeByte((byte) header.bytesInLastChunk())
            .writeUnsignedInt16(timeout)
            .writeInt32((int) header.crc32());

    DisposableUtils.addSafely(
        disposables,
        driver
            .writeOtaUpdateStartCharacteristic(writer.getBytes())
            .onTerminateDetach()
            .subscribe(() -> {}, updateEventSubject::onError));
  }

  @VisibleForTesting
  int calculateObjectTimeout() {
    return MAX_UNSIGNED_SHORT;
  }

  @VisibleForTesting
  void sendNextGroup() {
    if (status != OtaWriterStatus.WRITE_IN_PROGRESS) {
      return;
    }

    List<Completable> writeChunkCompletables = createWriteChunkCommands();

    if (!writeChunkCompletables.isEmpty()) {
      runCompletablesWithSingleSubscription(writeChunkCompletables);
    }
  }

  @NonNull
  @VisibleForTesting
  @SuppressWarnings("all")
  List<Completable> createWriteChunkCommands() {
    Timber.tag(TAG)
        .v(
            "createWriteChunkCommands with interval %d (attempt %d)",
            intervalBetweenResponses(), attempt);
    List<Completable> writeChunkCompletables = new ArrayList<>();
    for (int i = 0; i < currentObjectTotalChunks; i++) {
      /*
      we don't deal with writeCommand true/false because we are listening to notification updates,
      so we'll automatically receive a new OTA_UPDATE status when we send the last object
       */
      Completable completable;

      boolean withResponse;
      if (i > 0 && i % intervalBetweenResponses() == 0) {
        completable = driver.writeOtaChunkCharacteristicWithResponse(nextChunkData());
        withResponse = true;
      } else {
        completable = driver.writeOtaChunkCharacteristic(nextChunkData());
        withResponse = false;
      }

      writeChunkCompletables.add(enrichWriteChunkCommand(completable, i, withResponse));

      if (!incrementChunkTransmitted()) {
        break;
      }
    }

    return writeChunkCompletables;
  }

  @VisibleForTesting
  Completable enrichWriteChunkCommand(
      Completable writeChunkCommand, int completableIndex, boolean withResponse) {
    return writeChunkCommand
        .doOnSubscribe(
            s ->
                Timber.tag(TAG)
                    .v(
                        "Chunk %d/%d started (with response: %b)",
                        completableIndex, currentObjectTotalChunks - 1, withResponse))
        .doOnComplete(
            () ->
                Timber.tag(TAG)
                    .v(
                        "Chunk %d/%d completed (with response: %b)",
                        completableIndex, currentObjectTotalChunks - 1, withResponse))
        .doOnError(
            e ->
                Timber.tag(TAG)
                    .w(
                        e,
                        "Chunk %d/%d encountered an error (with response: %b)",
                        completableIndex,
                        currentObjectTotalChunks - 1,
                        withResponse));
  }

  @VisibleForTesting
  int intervalBetweenResponses() {
    AtomicInteger interval =
        new AtomicInteger(
            max(
                CHUNK_WITH_RESPONSE_MIN_INTERVAL,
                min(CHUNK_WITH_RESPONSE_MAX_INTERVAL, CHUNK_WITH_RESPONSE_MAX_INTERVAL - attempt)));
    return interval.get();
  }

  @VisibleForTesting
  byte[] nextChunkData() {
    int chunkToSend = totalChunksTransmitted + currentObjectChunksTransmitted;

    int offset = chunkToSend * BYTES_PER_CHUNK;

    return Arrays.copyOfRange(otaUpdate.getData(), offset, offset + chunkLength(chunkToSend));
  }

  @VisibleForTesting
  int chunkLength(int chunk) {
    if (chunk == totalChunks) {
      int modulo = otaUpdate.getData().length % BYTES_PER_CHUNK;

      if (modulo != 0) {
        return modulo;
      }
    }

    return BYTES_PER_CHUNK;
  }

  /**
   * Increments the number of chunks transmitted and check if the file or current Object has
   * finished transmitting.
   *
   * @return Whether or not transmission should stop to wait for toothbrush feedback. Always false
   *     if the update was cancelled
   */
  @SuppressWarnings("SimplifiableIfStatement")
  @VisibleForTesting
  boolean incrementChunkTransmitted() {
    currentObjectChunksTransmitted++;

    if (totalChunksTransmitted + currentObjectChunksTransmitted == totalChunks) {
      return false;
    }

    if (currentObjectChunksTransmitted == currentObjectTotalChunks) {
      return false;
    }

    return status == OtaWriterStatus.WRITE_IN_PROGRESS;
  }

  @VisibleForTesting
  void runCompletablesWithSingleSubscription(List<Completable> writeChunksCompletables) {
    DisposableUtils.addSafely(
        sendChunksDisposables,
        Completable.concat(writeChunksCompletables)
            .onTerminateDetach()
            .subscribeOn(singleScheduler)
            .observeOn(singleScheduler)
            .subscribe(
                () -> {
                  // we should receive a onNewOtaStatus
                },
                updateEventSubject::onError));
  }

  protected abstract byte getStartOTACommandId();

  protected abstract Observable<Integer> validateUpdatePreconditionsObservable();

  /** Clears the CompositeDisposable */
  @VisibleForTesting
  void close() {
    sendChunksDisposables.clear();
    disposables.clear();
  }

  enum OtaWriterStatus {
    WRITE_IN_PROGRESS,
    WRITE_ERROR,
    WRITE_CANCELLED,
    WRITE_COMPLETED
  }

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    STATUS_RESPONSE_READY_FOR_NEXT_HEADER,
    STATUS_RESPONSE_READY_FOR_NEXT_OBJECT,
    STATUS_RESPONSE_OBJECT_TIMEOUT,
    STATUS_RESPONSE_OBJECT_CRC_ERROR,
    STATUS_RESPONSE_FULL_CRC_ERROR,
    STATUS_RESPONSE_UPDATE_FINISHED,
    STATUS_RESPONSE_UPDATE_TIMEOUT,
    STATUS_RESPONSE_PROTOCOL_ERROR,
    STATUS_RESPONSE_INCOMPATIBLE_MAINAPP,
  })
  @interface StatusResponse {}
}
