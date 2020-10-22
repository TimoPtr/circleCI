package com.kolibree.android.sdk.core.ota.kltb002.updater;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.binary.PayloadReader;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.driver.ble.ParameterSet;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.error.FailureReason;
import com.kolibree.android.sdk.util.KolibreeUtils;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;
import timber.log.Timber;

/**
 * {@link com.kolibree.android.sdk.connection.toothbrush.Toothbrush} implementation for Ara and
 * Connect E1 models until Bootloader version 0.17.65535.
 */
final class LegacyOtaWriter implements OtaWriter {

  /*
   * Details can be found in the Kolibree GATT OTA Update Specification
   */
  private static final int UPDATER_STATUS_ERROR = 0x0002;
  private static final int UPDATER_STATUS_WAITING_FOR_NEXT_CHUNK = 0x0001;
  private static final int UPDATER_STATUS_CRC_ERROR = 0x0008;
  private static final int UPDATER_STATUS_FINISHED = 0x0004;

  /** Connection reference for OTA updates. */
  private final InternalKLTBConnection connection;

  /** {@link BleDriver} implementation */
  private final BleDriver driver;

  /**
   * A bug in the firmware causes the toothbrush to go to sleep mode while receiving write command
   * operations, so we increase the timeout to make sure the toothbrush won't go to sleep mode
   * during an OTA update.
   */
  private int autoShutdownTimeoutCache;

  public static LegacyOtaWriter create(InternalKLTBConnection kltbConnection, BleDriver driver) {
    return new LegacyOtaWriter(kltbConnection, driver);
  }

  @VisibleForTesting
  LegacyOtaWriter(@NonNull InternalKLTBConnection connection, @NonNull BleDriver driver) {
    this.connection = connection;
    this.driver = driver;
  }

  @NonNull
  @Override
  public Observable<Integer> write(@NonNull OtaUpdate otaUpdate) {
    return sendStartUpdateCommand(otaUpdate)
        .andThen(Completable.fromAction(this::maybeSaveAutoShutdownTimeout))
        .andThen(Completable.fromAction(() -> maybeSetAutoShutdownTimeout(600))) // Ten minutes
        .andThen(writeUpdate(otaUpdate))
        .doFinally(
            () -> {
              maybeRestoreAutoShutdownTimeout();
              connection.setState(KLTBConnectionState.ACTIVE);
            });
  }

  /**
   * Send start update command.
   *
   * @param update non null OtaUpdate
   */
  private Completable sendStartUpdateCommand(@NonNull OtaUpdate update) {
    return Completable.create(
        emitter -> {
          try {
            byte[] otaStatusPayload =
                driver
                    .otaUpdateStatusCharacteristicChangedFlowable()
                    .doOnSubscribe(
                        ignore ->
                            driver
                                .writeOtaUpdateStartCharacteristic(prepareStartOTACommand(update))
                                .blockingAwait())
                    .take(1)
                    .blockingFirst();

            PayloadReader payloadReader = new PayloadReader(otaStatusPayload);
            final int status = payloadReader.readUnsignedInt16();

            if (status == UPDATER_STATUS_ERROR) {
              throw new Exception("Toothbrush rejected update request");
            } else if (status == UPDATER_STATUS_WAITING_FOR_NEXT_CHUNK
                && update.getType() == OtaUpdate.TYPE_FIRMWARE
                && !isRunningBootloader()) {
              throw new Exception("Fatal update error : illegal updater status");
            }

            emitter.onComplete();
          } catch (Exception e) {
            emitter.tryOnError(e);
          }
        });
  }

  /**
   * Create a start update command payload.
   *
   * <p>Fast mode is not yet supported
   *
   * @param update non null OtaUpdate implementation
   * @return non null byte array
   */
  @NonNull
  private byte[] prepareStartOTACommand(@NonNull OtaUpdate update) {

    final int chunkCount =
        update.getData().length / 16
            + (update.getData().length % 16 == 0 ? 0 : 1); // Chunk size is 16 bytes

    return new PayloadWriter(16)
        .writeByte(update.getType() == OtaUpdate.TYPE_FIRMWARE ? 0x00 : (byte) 0x01)
        .writeUnsignedInt16(chunkCount)
        .writeInt32((int) update.getCrc())
        .writeSoftwareVersion(update.getVersion())
        .writeHardwareVersion(driver.getHardwareVersion())
        .writeByte((byte) 0x00) // Disable async mode
        .getBytes();
  }

  private boolean isRunningBootloader() {
    return driver.isRunningBootloader();
  }

  /**
   * Write an update over the air.
   *
   * @param update non null OtaUpdate
   * @return non null percent of completion Observable
   */
  @NonNull
  private Observable<Integer> writeUpdate(@NonNull OtaUpdate update) {
    return Observable.<Integer>create(
            emitter -> {
              final int chunkCount =
                  update.getData().length / 16
                      + (update.getData().length % 16 == 0 ? 0 : 1); // Chunk size is 16 bytes

              emitter.onNext(0);

              final Disposable[] notificationDisposable = new Disposable[1];

              try (InputStream in = new ByteArrayInputStream(update.getData())) {
                final byte[] buffer = new byte[16];
                final PayloadWriter chunkWriter = new PayloadWriter(19);

                Flowable<byte[]> notificationFlowable =
                    driver
                        .otaUpdateStatusCharacteristicChangedFlowable()
                        .onBackpressureBuffer(20)
                        .publish()
                        .autoConnect(1, disposable -> notificationDisposable[0] = disposable);

                for (int chunk = 0; chunk < chunkCount; chunk++) {
                  final int read = in.read(buffer, 0, buffer.length);

                  if (read != 16) { // End of file, fill with 0
                    for (int i = read; i < 16; i++) {
                      buffer[i] = 0x00;
                    }
                  }

                  // Send command
                  chunkWriter.clear();
                  chunkWriter.writeUnsignedInt16(chunk);
                  chunkWriter.writeByteArray(buffer);
                  chunkWriter.writeByte(KolibreeUtils.sumOfBytesChecksum(buffer));

                  byte[] otaStatusPayload =
                      notificationFlowable
                          .doOnSubscribe(
                              ignore ->
                                  driver
                                      .writeOtaChunkCharacteristic(chunkWriter.getBytes())
                                      .blockingAwait())
                          .take(1)
                          .blockingFirst();

                  PayloadReader payloadReader = new PayloadReader(otaStatusPayload);

                  final int writeChunkStatus = payloadReader.readUnsignedInt16();

                  if (writeChunkStatus
                      == (UPDATER_STATUS_WAITING_FOR_NEXT_CHUNK | UPDATER_STATUS_CRC_ERROR)) {
                    Timber.e(getClass().getSimpleName(), "Chunk " + chunk + " CRC error");
                    chunk--; // CRC error
                  } else if (writeChunkStatus != UPDATER_STATUS_WAITING_FOR_NEXT_CHUNK
                      && writeChunkStatus != UPDATER_STATUS_FINISHED) {
                    throw new FailureReason(
                        "Write chunk " + chunk + " failed with status " + writeChunkStatus);
                  }

                  emitter.onNext(chunk * 100 / chunkCount);
                }

                emitter.onNext(100);
                emitter.onComplete();
              } catch (Exception e) {
                emitter.tryOnError(e);
              } finally {
                if (notificationDisposable[0] != null) {
                  notificationDisposable[0].dispose();
                }
              }
            })
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()));
  }

  /**
   * Save the auto shutdown timeout if the toothbrush is not in bootloader.
   *
   * @throws Exception if the command could not be sent
   */
  private void maybeSaveAutoShutdownTimeout() throws Exception {
    if (!connection.toothbrush().isRunningBootloader()) {
      autoShutdownTimeoutCache =
          driver
              .getDeviceParameter(ParameterSet.getAutoShutdownTimeoutParameterPayload())
              .skip(1)
              .readUnsignedInt16();
    }
  }

  /**
   * Restore the auto shutdown timeout to the value previously set, if the toothbrush is not in
   * bootloader mode, and if the value was set (ie the brush was not in bootloader before the
   * update).
   *
   * @throws Exception on failure
   */
  private void maybeRestoreAutoShutdownTimeout() throws Exception {
    if (autoShutdownTimeoutCache != 0 && !connection.toothbrush().isRunningBootloader()) {
      maybeSetAutoShutdownTimeout(autoShutdownTimeoutCache);
    }
  }

  /**
   * Set the toothbrush auto shutdown timeout if it is not in bootloader mode.
   *
   * @param timeoutSeconds timeout in seconds
   * @throws Exception if the command could not be set
   */
  private void maybeSetAutoShutdownTimeout(int timeoutSeconds) throws Exception {
    if (!connection.toothbrush().isRunningBootloader()) {
      driver.setDeviceParameter(
          ParameterSet.setAutoShutdownTimeoutParameterPayload(timeoutSeconds));
    }
  }
}
