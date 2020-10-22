package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.binary.PayloadWriter.MAX_UNSIGNED_SHORT;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_START;
import static com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.OTA_UPDATE_WRITE_CHUNK;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.CHUNK_WITH_RESPONSE_MAX_INTERVAL;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.CHUNK_WITH_RESPONSE_MIN_INTERVAL;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.COMMAND_ID_NEXT_OBJECT_HEADER;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_FULL_CRC_ERROR;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_INCOMPATIBLE_MAINAPP;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_OBJECT_CRC_ERROR;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_OBJECT_TIMEOUT;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_PROTOCOL_ERROR;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_READY_FOR_NEXT_HEADER;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_READY_FOR_NEXT_OBJECT;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_UPDATE_FINISHED;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.STATUS_RESPONSE_UPDATE_TIMEOUT;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.FastFirmwareWriter.COMMAND_ID_FAST_FW_UPDATE;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.NextObjectHeaderKt.BYTES_PER_CHUNK;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.NextObjectHeaderKt.CHUNKS_PER_OBJECT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic;
import com.kolibree.android.sdk.core.ota.kltb002.updater.BaseFastOtaWriter.OtaWriterStatus;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;
import com.kolibree.android.test.TestForcedException;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kotlin.random.Random;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;

@SuppressWarnings("KotlinInternalInJava")
public class BaseFastOtaWriterTest extends BaseUnitTest {

  @SuppressWarnings("KotlinInternalInJava")
  @Mock
  BleDriver driver;

  private StubFastOtaWriter updater;

  @Override
  public void setup() throws Exception {
    super.setup();
    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);
    updater = spy(new StubFastOtaWriter(driver, Schedulers.io()));
  }

  /*
  UPDATE
   */

  @Test
  public void onObjectError_invokesCancelPendingOperations() {
    updater.onObjectError();

    verify(driver).cancelPendingOperations();
  }

  @Test
  public void onObjectError_clearsSendChunksDisposables() {
    Disposable disposable = mock(Disposable.class);
    updater.sendChunksDisposables.add(disposable);

    updater.onObjectError();

    verify(disposable).dispose();
  }

  /*
  WRITE
   */

  @Test
  public void write_storesOtaUpdate() {
    doReturn(PublishSubject.<Integer>create()).when(updater).updateEventsFromBLEObservable();

    doReturn(0).when(updater).countChunks();

    assertNull(updater.otaUpdate);

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    OtaUpdate expectedOtaUpdate = mockOtaUpdate();
    updater.write(expectedOtaUpdate).test().assertNoErrors();

    assertEquals(expectedOtaUpdate, updater.otaUpdate);
  }

  @Test
  public void write_storesTotalChunks() {
    doReturn(PublishSubject.<Integer>create()).when(updater).updateEventsFromBLEObservable();

    int expectedChunks = 76;
    doReturn(expectedChunks).when(updater).countChunks();

    assertEquals(0, updater.totalChunks);

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    updater.write(mockOtaUpdate()).test();

    assertEquals(expectedChunks, updater.totalChunks);
  }

  @Test
  public void write_validateUpdatePreconditionsObservableEmitsError_emitsError() {
    doReturn(PublishSubject.<Integer>create()).when(updater).updateEventsFromBLEObservable();

    doReturn(0).when(updater).countChunks();

    Throwable expectedThrowable = mock(Throwable.class);
    doReturn(Observable.error(expectedThrowable))
        .when(updater)
        .validateUpdatePreconditionsObservable();

    updater.write(mockOtaUpdate()).test().assertError(expectedThrowable);
  }

  @Test
  public void
      write_validateUpdatePreconditionsCompletes_returnsMergedObservableFromUpdateEventsFromBLEObservableAndOtaEventRelay() {
    PublishSubject<Integer> subject = PublishSubject.create();
    doReturn(subject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    observer.assertEmpty();

    int firstExpectedValue = 3;
    int secondExpectedValue = 70;

    doNothing().when(updater).close();

    updater.updateEventSubject.onNext(firstExpectedValue);
    subject.onNext(secondExpectedValue);

    observer.assertValues(firstExpectedValue, secondExpectedValue);
  }

  @Test
  public void write_validateUpdatePreconditionsCompletes_eventsFromBleEmitsValue_emits() {
    PublishSubject<Integer> eventsFromBleSubject = PublishSubject.create();
    doReturn(eventsFromBleSubject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    observer.assertEmpty();

    eventsFromBleSubject.onNext(30);

    observer.assertValue(30);

    eventsFromBleSubject.onNext(70);

    observer.assertValues(30, 70);
  }

  @Test
  public void write_validateUpdatePreconditionsCompletes_updateEventSubjectEmitsValue_emits() {
    PublishSubject<Integer> eventsFromBleSubject = PublishSubject.create();
    doReturn(eventsFromBleSubject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    observer.assertEmpty();

    updater.updateEventSubject.onNext(30);

    observer.assertValue(30);

    updater.updateEventSubject.onNext(70);

    observer.assertValues(30, 70);
  }

  @Test
  public void write_validateUpdatePreconditionsCompletes_eventsFromBleEmits100_neverCompletes() {
    PublishSubject<Integer> eventsFromBleSubject = PublishSubject.create();
    doReturn(eventsFromBleSubject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    observer.assertNotComplete();

    eventsFromBleSubject.onNext(30);

    observer.assertNotComplete();

    eventsFromBleSubject.onNext(100);

    observer.assertNotComplete();
  }

  @Test
  public void write_validateUpdatePreconditionsCompletes_observableIsDisposed_invokesClose() {
    PublishSubject<Integer> eventsFromBleSubject = PublishSubject.create();
    doReturn(eventsFromBleSubject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    verify(updater, never()).close();

    observer.dispose();

    verify(updater).close();
  }

  @Test
  public void
      write_validateUpdatePreconditionsCompletes_eventsFromBleObservableError_invokesClose() {
    PublishSubject<Integer> eventsFromBleSubject = PublishSubject.create();
    doReturn(eventsFromBleSubject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    verify(updater, never()).close();

    eventsFromBleSubject.onError(mock(Throwable.class));

    verify(updater).close();

    observer.assertError(Throwable.class);
  }

  @Test
  public void write_validateUpdatePreconditionsCompletes_updateEventSubjectError_invokesClose() {
    PublishSubject<Integer> eventsFromBleSubject = PublishSubject.create();
    doReturn(eventsFromBleSubject).when(updater).updateEventsFromBLEObservable();
    doReturn(0).when(updater).countChunks();

    doReturn(Observable.empty()).when(updater).validateUpdatePreconditionsObservable();

    TestObserver<Integer> observer = updater.write(mockOtaUpdate()).test();

    verify(updater, never()).close();

    updater.updateEventSubject.onError(mock(Throwable.class));

    verify(updater).close();

    observer.assertError(Throwable.class);
  }
  /*
  CLOSE
   */

  @Test
  public void close_clearsCompositeDisposable() throws Exception {
    Disposable disposable = Single.create(e -> {}).subscribe();
    updater.disposables.add(disposable);
    assertEquals(1, updater.disposables.size());

    assertFalse(disposable.isDisposed());

    updater.close();

    assertTrue(disposable.isDisposed());
    assertEquals(0, updater.disposables.size());
    assertFalse(updater.disposables.isDisposed());
  }

  /*
  UPDATE EVENTS FROM BLE OBSERVABLE
   */

  @Test
  public void updateEventsFromBLEObservable_subscribesToToEnableCharacteristicNotification()
      throws Exception {
    when(driver.otaUpdateStatusCharacteristicChangedFlowable())
        .thenReturn(PublishProcessor.create());

    doNothing().when(updater).startUpdate();

    updater.updateEventsFromBLEObservable().test();

    verify(driver).otaUpdateStatusCharacteristicChangedFlowable();
  }

  @Test
  public void updateEventsFromBLEObservable_mapsEventsThroughOnNewOta() throws Exception {
    PublishProcessor<byte[]> processor = PublishProcessor.create();
    when(driver.otaUpdateStatusCharacteristicChangedFlowable()).thenReturn(processor);

    byte[] emittedBytes = new byte[0];
    Integer expectedProgress = 54;
    doReturn(expectedProgress).when(updater).onNewOtaStatus(emittedBytes);

    doNothing().when(updater).startUpdate();

    TestObserver<Integer> observer = updater.updateEventsFromBLEObservable().test();

    observer.assertEmpty();

    processor.onNext(emittedBytes);

    observer.assertValue(expectedProgress);
  }

  /*
  START UPDATE
   */

  @Test
  public void startUpdate_setsStatusWRITE_IN_PROGRESS() {
    doNothing().when(updater).sendStartUpdateCommand();

    assertNull(updater.status);

    updater.startUpdate();

    assertEquals(OtaWriterStatus.WRITE_IN_PROGRESS, updater.status);
  }

  @Test
  public void startUpdate_invokeSendStartUpdateCommand() {
    doNothing().when(updater).sendStartUpdateCommand();

    updater.startUpdate();

    verify(updater).sendStartUpdateCommand();
  }

  /*
  SEND START UPDATE COMMAND
   */
  @Test
  public void sendStartUpdateCommand_invokesWriteCharacteristicCompletableWithExpectedData() {
    byte[] expectedPayload = new byte[3];
    doReturn(expectedPayload).when(updater).prepareStartOTACommand();

    Completable completable =
        runnableWriteCharacteristicCompletableMock(OTA_UPDATE_START, expectedPayload);

    updater.sendStartUpdateCommand();

    verify(completable).subscribe(any(Action.class), any(Consumer.class));
  }

  /*
  PREPARE START OTA COMMAND
   */
  @Test
  public void prepareStartOTACommand_returnsExpectedData() {
    doReturn(COMMAND_ID_FAST_FW_UPDATE).when(updater).getStartOTACommandId();

    byte[] data = new byte[] {1, 2, 3, 4};
    OtaUpdate otaUpdate = mockOTAUpdate(data);
    long crc = 98L;
    when(otaUpdate.getCrc()).thenReturn(crc);
    SoftwareVersion softwareVersion = new SoftwareVersion(1, 2, 3);
    when(otaUpdate.getVersion()).thenReturn(softwareVersion);

    HardwareVersion hardwareVersion = new HardwareVersion(1, 2);
    when(driver.getHardwareVersion()).thenReturn(hardwareVersion);

    byte expectedId = 21;
    doReturn(expectedId).when(updater).getStartOTACommandId();

    byte[] expectedData =
        new PayloadWriter(17)
            .writeByte(expectedId)
            .writeInt32((int) crc)
            .writeInt32(data.length)
            .writeSoftwareVersion(softwareVersion)
            .writeHardwareVersion(hardwareVersion)
            .getBytes();

    assertArrayEquals(expectedData, updater.prepareStartOTACommand());
  }

  /*
  ON WRITE COMPLETED
   */
  @Test
  public void onWriteCompleted_updatesStatusToCompleted() {
    assertNull(updater.status);

    updater.onWriteCompleted();

    assertEquals(OtaWriterStatus.WRITE_COMPLETED, updater.status);
  }

  /*
  ON NEW OTA STATUS
   */
  @Test
  public void
      onNewOTAStatus_UPDATE_STATUS_READY_FOR_NEXT_HEADER_invokesSendStartNextObjectTransmission() {
    doNothing().when(updater).sendStartNextObjectTransmission();
    doReturn(0).when(updater).calculateProgress();

    updater.onNewOtaStatus(createStatusPayloader(STATUS_RESPONSE_READY_FOR_NEXT_HEADER));

    verify(updater).sendStartNextObjectTransmission();
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_READY_FOR_NEXT_OBJECT_invokesSendNextGroup() {
    doNothing().when(updater).sendNextGroup();
    doReturn(0).when(updater).calculateProgress();

    updater.onNewOtaStatus(createStatusPayloader(STATUS_RESPONSE_READY_FOR_NEXT_OBJECT));

    verify(updater).sendNextGroup();
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_UPDATE_FINISHED_invokesOnWriteCompleted() {
    doReturn(0).when(updater).calculateProgress();
    doNothing().when(updater).onWriteCompleted();

    updater.onNewOtaStatus(createStatusPayloader(STATUS_RESPONSE_UPDATE_FINISHED));

    verify(updater).onWriteCompleted();
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_OBJECT_TIMEOUT_invokesRetryLastObject() {
    doNothing().when(updater).onObjectTimeout();
    doNothing().when(updater).retryLastObject();
    doReturn(0).when(updater).calculateProgress();

    updater.onNewOtaStatus(createStatusPayloader(STATUS_RESPONSE_OBJECT_TIMEOUT));

    verify(updater).retryLastObject();
  }

  @Test
  public void
      onNewOTAStatus_STATUS_RESPONSE_OBJECT_TIMEOUT_invokesOnObjectErrorAndRetryLastObjectInAppropriateOrder() {
    doNothing().when(updater).onObjectTimeout();
    doNothing().when(updater).retryLastObject();
    doReturn(0).when(updater).calculateProgress();

    InOrder inOrder = inOrder(updater);

    updater.onNewOtaStatus(createStatusPayloader(STATUS_RESPONSE_OBJECT_TIMEOUT));

    inOrder.verify(updater).onObjectTimeout();
    inOrder.verify(updater).retryLastObject();
  }

  @Test
  public void
      onNewOTAStatus_STATUS_RESPONSE_OBJECT_CRC_ERROR_invokesOnObjectErrorAndSendStartNextObjectTransmissionInAppropriateOrder() {
    doNothing().when(updater).retryLastObject();
    doReturn(0).when(updater).calculateProgress();

    InOrder inOrder = inOrder(updater);

    updater.onNewOtaStatus(createStatusPayloader(STATUS_RESPONSE_OBJECT_CRC_ERROR));

    inOrder.verify(updater).onObjectError();
    inOrder.verify(updater).sendStartNextObjectTransmission();
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_FULL_CRC_ERROR_invokesOnWriteError() {
    doNothing().when(updater).onWriteError(anyInt());

    int expectedStatus = STATUS_RESPONSE_FULL_CRC_ERROR;
    updater.onNewOtaStatus(createStatusPayloader(expectedStatus));

    verify(updater).onWriteError(expectedStatus);
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_UPDATE_TIMEOUT_invokesOnWriteError() {
    doNothing().when(updater).onObjectTimeout();
    doNothing().when(updater).onWriteError(anyInt());

    int expectedStatus = STATUS_RESPONSE_UPDATE_TIMEOUT;
    updater.onNewOtaStatus(createStatusPayloader(expectedStatus));

    verify(updater).onWriteError(expectedStatus);
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_PROTOCOL_ERROR_invokesOnWriteError() {
    doNothing().when(updater).onWriteError(anyInt());

    int expectedStatus = STATUS_RESPONSE_PROTOCOL_ERROR;
    updater.onNewOtaStatus(createStatusPayloader(expectedStatus));

    verify(updater).onWriteError(expectedStatus);
  }

  @Test
  public void onNewOTAStatus_STATUS_RESPONSE_INCOMPATIBLE_MAINAPP_invokesOnWriteError() {
    doNothing().when(updater).onWriteError(anyInt());

    int expectedStatus = STATUS_RESPONSE_INCOMPATIBLE_MAINAPP;
    updater.onNewOtaStatus(createStatusPayloader(expectedStatus));

    verify(updater).onWriteError(expectedStatus);
  }

  @Test
  public void onNewOTAStatus_UnknownStatus_invokesOnWriteError() {
    doNothing().when(updater).onWriteError(anyInt());

    int expectedStatus = 30;
    updater.onNewOtaStatus(createStatusPayloader(expectedStatus));

    verify(updater).onWriteError(expectedStatus);
  }

  /*
  ON WRITE ERROR
   */
  @Test
  public void onWriteError_invokesOnObjectError() {
    updater.onWriteError(1);

    verify(updater).onObjectError();
  }

  @Test
  public void onWriteError_setsErrorStatus() {
    assertNotEquals(OtaWriterStatus.WRITE_ERROR, updater.status);

    updater.onWriteError(1);

    assertEquals(OtaWriterStatus.WRITE_ERROR, updater.status);
  }

  @Test
  public void onWriteError_emitsError() {
    TestObserver<Integer> observer = updater.updateEventSubject.test();

    observer.assertEmpty();

    updater.onWriteError(1);

    observer.assertError(IllegalStateException.class);
  }

  /*
  ON OBJECT TIMEDOUT
   */

  @Test
  public void onObjectTimeout_invokesOnObjectError() {
    updater.onObjectTimeout();

    verify(updater).onObjectError();
  }

  /*
  PROGRESS PERCENT
   */
  @Test
  public void calculateProgress_noTotalChunks_returns0() {
    assertEquals(0, updater.calculateProgress().intValue());
  }

  @Test
  public void calculateProgress_withTotalChunks_with0Transmitted_returns0() {
    updater.totalChunks = 45;

    assertEquals(0, updater.totalChunksTransmitted);

    assertEquals(0, updater.calculateProgress().intValue());
  }

  @Test
  public void calculateProgress_withTotalChunks_withTransmitted_returnsExpectedPercent() {
    updater.totalChunks = 100;

    int expectedProgress = 45;
    updater.totalChunksTransmitted = expectedProgress;

    assertEquals(expectedProgress, updater.calculateProgress().intValue());
  }

  /*
  RETRY LAST OBJECT
   */
  @Test
  public void retryLastObject_resetsCurrentObjectTransmittedTo0() {
    updater.currentObjectChunksTransmitted = 50;

    doNothing().when(updater).sendStartNextObjectTransmission();

    updater.retryLastObject();

    assertEquals(0, updater.currentObjectChunksTransmitted);
  }

  @Test
  public void retryLastObject_invokesSendStartNextObjectTransmission() {
    doNothing().when(updater).sendStartNextObjectTransmission();

    updater.retryLastObject();

    verify(updater).sendStartNextObjectTransmission();
  }

  /*
  SEND START NEXT OBJECT TRANSMISSION
   */
  @Test
  public void sendStartNextObjectTransmission_invokesAdjustTotalsBeforeChunksRemaining() {
    InOrder inOrder = inOrder(updater);

    updater.sendStartNextObjectTransmission();

    inOrder.verify(updater).adjustTotals();
    inOrder.verify(updater).chunksRemaining();
  }

  @Test
  public void sendStartNextObjectTransmission_0ChunksRemaining_doesNothing() {
    updater.sendStartNextObjectTransmission();

    verify(updater, never()).sendNextObjectHeader(any(NextObjectHeader.class));
  }

  @Test
  public void sendStartNextObjectTransmission_withChunksRemaining_invokesSendNextObjectHeader() {
    int chunksRemaining = 543;
    doReturn(chunksRemaining).when(updater).chunksRemaining();

    NextObjectHeader expectedNextObjectHeader = mock(NextObjectHeader.class);
    doReturn(expectedNextObjectHeader).when(updater).createNextObjectHeader(chunksRemaining);

    doNothing().when(updater).sendNextObjectHeader(any(NextObjectHeader.class));

    updater.sendStartNextObjectTransmission();

    verify(updater).sendNextObjectHeader(expectedNextObjectHeader);
  }

  /*
  CREATE NEXT OBJECT HEADER
   */
  @Test
  public void createNextObjectHeader_createsHeaderFromExpectedData() {
    byte[] data = new byte[CHUNKS_PER_OBJECT];
    mockOTAUpdate(data);
    int chunksRemaining = 5;

    int chunksTransmitted = 0;
    updater.totalChunksTransmitted = chunksTransmitted;

    NextObjectHeader expectedNextObjectHeader =
        NextObjectHeader.create(data, chunksTransmitted, chunksRemaining);

    assertEquals(expectedNextObjectHeader, updater.createNextObjectHeader(chunksRemaining));
  }

  /*
  CHUNKS REMAINING
   */
  @Test
  public void chunksRemaining_returnsExpectedValue() {
    updater.totalChunks = 50;
    updater.totalChunksTransmitted = 30;

    assertEquals(20, updater.chunksRemaining());
  }

  @Test
  public void chunksRemaining_transmittedBiggerThanTotalChunks_returns0() {
    updater.totalChunks = 50;
    updater.totalChunksTransmitted = updater.totalChunks + 5;

    assertEquals(0, updater.chunksRemaining());
  }

  /*
  ADJUST TOTALS
   */

  @Test
  public void adjustTotals_withTransmitted_addsCurrentObjectsTransmittedToTotalChunksTransmitted() {
    updater.totalChunksTransmitted = 10;

    updater.currentObjectChunksTransmitted = 30;

    updater.adjustTotals();

    assertEquals(40, updater.totalChunksTransmitted);
  }

  /*
  SEND NEXT OBJECT HEADER
   */
  @Test
  public void sendNextObjectHeader_resetsCurrentObjectsTransmittedTo0() {
    updater.currentObjectChunksTransmitted = 30;

    runnableWriteCharacteristicCompletableMock(OTA_UPDATE_START);

    NextObjectHeader nextObjectHeader = mockNextObjectHeader();

    doReturn(1).when(updater).calculateObjectTimeout();

    updater.sendNextObjectHeader(nextObjectHeader);

    assertEquals(0, updater.currentObjectChunksTransmitted);
  }

  @Test
  public void sendNextObjectHeader_storesCurrentObjectTotalChunks() {
    assertEquals(0, updater.currentObjectTotalChunks);

    int expectedObjectChunks = 98;
    NextObjectHeader nextObjectHeader = mockNextObjectHeader(expectedObjectChunks);

    runnableWriteCharacteristicCompletableMock(OTA_UPDATE_START);

    doReturn(1).when(updater).calculateObjectTimeout();

    updater.sendNextObjectHeader(nextObjectHeader);

    assertEquals(expectedObjectChunks, updater.currentObjectTotalChunks);
  }

  @Test
  public void sendNextObjectHeader_invokesWriteCharacteristicWithExpectedPayload() {
    int bytesInLastObject = 15;
    boolean isLastObject = true;

    int objectChunks = 98;
    long crc = 54654L;
    NextObjectHeader nextObjectHeader =
        mockNextObjectHeader(objectChunks, bytesInLastObject, isLastObject, crc);

    int timeout = 54356;
    doReturn(timeout).when(updater).calculateObjectTimeout();

    byte[] expectedPayload =
        new PayloadWriter(11)
            .writeByte(COMMAND_ID_NEXT_OBJECT_HEADER)
            .writeByte((byte) 0x01)
            .writeUnsignedInt16(objectChunks)
            .writeByte((byte) bytesInLastObject)
            .writeUnsignedInt16(timeout)
            .writeInt32((int) crc)
            .getBytes();

    Completable completable =
        runnableWriteCharacteristicCompletableMock(OTA_UPDATE_START, expectedPayload);

    updater.sendNextObjectHeader(nextObjectHeader);

    verify(driver).writeOtaUpdateStartCharacteristic(expectedPayload);

    verify(completable).subscribe(any(Action.class), any(Consumer.class));
  }

  /*
  CALCULATE OBJECT TIMEOUT
   */

  @Test
  public void calculateObjectTimeout_returnsMAX_UNSIGNED_SHORT() {
    assertEquals(MAX_UNSIGNED_SHORT, updater.calculateObjectTimeout());
  }

  /*
  SEND NEXT GROUP
   */

  @Test
  public void sendNextGroup_statusWRITE_ERROR_doesNothing() {
    updater.status = OtaWriterStatus.WRITE_ERROR;

    updater.sendNextGroup();

    verify(updater, never()).createWriteChunkCommands();
  }

  @Test
  public void
      sendNextGroup_statusWRITE_IN_PROGRESS_createWriteChunkCommandsReturnsEmptyList_neverInvokesRunCompletablesWithSingleSubscription() {
    updater.status = OtaWriterStatus.WRITE_IN_PROGRESS;

    doReturn(Collections.emptyList()).when(updater).createWriteChunkCommands();

    updater.sendNextGroup();

    verify(updater, never()).runCompletablesWithSingleSubscription(anyList());
  }

  @Test
  public void
      sendNextGroup_statusWRITE_IN_PROGRESS_createWriteChunkCommandsReturnsList_invokesRunCompletablesWithSingleSubscription() {
    updater.status = OtaWriterStatus.WRITE_IN_PROGRESS;

    List<Completable> expectedList = Collections.singletonList(Completable.complete());
    doReturn(expectedList).when(updater).createWriteChunkCommands();

    updater.sendNextGroup();

    verify(updater).runCompletablesWithSingleSubscription(expectedList);
  }

  /*
  CREATE WRITE CHUNK COMMANDS
   */
  @Test
  public void createWriteChunkCommands_currentObjectTotalChunks0_returnsEmptyList() {
    assertTrue(updater.createWriteChunkCommands().isEmpty());
  }

  @Test
  public void createWriteChunkCommands_withCurrentObjectTotalChunks1_returnsListWithSameSize() {
    int objectTotalChunks = 1;
    updater.currentObjectTotalChunks = objectTotalChunks;

    doReturn(new byte[0]).when(updater).nextChunkData();

    Completable expectedCompletable =
        runnableWriteCharacteristicCompletableMock(OTA_UPDATE_WRITE_CHUNK);

    doReturn(true).when(updater).incrementChunkTransmitted();
    doReturn(true).when(updater).incrementChunkTransmitted();

    List<Completable> returnedList = updater.createWriteChunkCommands();
    assertEquals(objectTotalChunks, returnedList.size());

    assertEquals(expectedCompletable, returnedList.get(0));
  }

  @Test
  public void
      createWriteChunkCommands_withCurrentObjectTotalChunks1_invokesDriverWriteCharacteristicCompletableWithExpectedData() {
    int objectTotalChunks = 1;
    updater.currentObjectTotalChunks = objectTotalChunks;

    byte[] expectedChunkData = new byte[30];
    doReturn(expectedChunkData).when(updater).nextChunkData();

    doReturn(true).when(updater).incrementChunkTransmitted();

    updater.createWriteChunkCommands();

    verify(driver).writeOtaChunkCharacteristic(expectedChunkData);
  }

  @Test
  public void
      createWriteChunkCommands_withCurrentObjectTotalChunks2_incrementChunksReturnsFalse_returnsListSize1() {
    updater.currentObjectTotalChunks = 2;

    doReturn(new byte[0]).when(updater).nextChunkData();

    doReturn(false).when(updater).incrementChunkTransmitted();

    assertEquals(1, updater.createWriteChunkCommands().size());
  }

  @Test
  public void
      createWriteChunkCommands_withCurrentObjectTotalChunks2_incrementChunksReturnsTrue_returnsListSize2() {
    updater.currentObjectTotalChunks = 2;

    doReturn(new byte[0]).when(updater).nextChunkData();

    Completable expectedCompletable =
        runnableWriteCharacteristicCompletableMock(OTA_UPDATE_WRITE_CHUNK);

    doReturn(true).when(updater).incrementChunkTransmitted();

    List<Completable> returnedList = updater.createWriteChunkCommands();
    assertEquals(2, returnedList.size());

    assertEquals(expectedCompletable, returnedList.get(0));
    assertEquals(expectedCompletable, returnedList.get(1));
  }

  @Test
  public void
      createWriteChunkCommands_withCurrentObjectTotalChunks5_invokesWriteWithResponseOnTheLastChunk() {
    updater.currentObjectTotalChunks = 5;

    byte[] chunk1 = new byte[1];
    byte[] chunk2 = new byte[2];
    byte[] chunk3 = new byte[3];
    byte[] chunk4 = new byte[4];
    byte[] chunk5 = new byte[5];
    doReturn(chunk1, chunk2, chunk3, chunk4, chunk5).when(updater).nextChunkData();

    doReturn(true).when(updater).incrementChunkTransmitted();

    updater.createWriteChunkCommands();

    verify(driver).writeOtaChunkCharacteristic(chunk1);
    verify(driver).writeOtaChunkCharacteristic(chunk2);
    verify(driver).writeOtaChunkCharacteristic(chunk3);
    verify(driver).writeOtaChunkCharacteristic(chunk4);
    verify(driver).writeOtaChunkCharacteristicWithResponse(chunk5);
  }

  @Test
  public void
      createWriteChunkCommands_withManyObjects_invokesWriteWithResponseEveryWRITE_CHUNK_WITH_RESPONSE_INTERVAL() {
    int totalChunks = Random.Default.nextInt(50, 100);
    updater.currentObjectTotalChunks = totalChunks;

    doReturn(new byte[0]).when(updater).nextChunkData();
    when(driver.writeOtaChunkCharacteristicWithResponse(any())).thenReturn(Completable.complete());
    when(driver.writeOtaChunkCharacteristic(any())).thenReturn(Completable.complete());

    doReturn(true).when(updater).incrementChunkTransmitted();

    /*
    We ignore position 0 in our modulo operation, so we need to subtract 1 from total chunks
     */
    int expectedWriteWithResponse =
        (int) Math.floor(((totalChunks - 1) * 1f) / CHUNK_WITH_RESPONSE_MAX_INTERVAL);
    int expectedWriteWithoutResponse = totalChunks - expectedWriteWithResponse;

    updater.createWriteChunkCommands();

    String failureMessage = "Total chunks " + totalChunks + ", expected ";

    verify(
            driver,
            VerificationModeFactory.times(expectedWriteWithoutResponse)
                .description(failureMessage + expectedWriteWithoutResponse))
        .writeOtaChunkCharacteristic(any());

    verify(
            driver,
            VerificationModeFactory.times(expectedWriteWithResponse)
                .description(failureMessage + expectedWriteWithoutResponse))
        .writeOtaChunkCharacteristicWithResponse(any());
  }

  /*
  RUN COMPLETABLES WITH SINGLE SUBSCRIPTION
   */
  @Test
  public void runCompletablesWithSingleSubscription_emptyList_doesNotCrash() {
    List<Completable> list = Collections.emptyList();
    updater.runCompletablesWithSingleSubscription(list);
  }

  @Test
  public void runCompletablesWithSingleSubscription_singleCompletable_isExecuted() {
    Completable completable = mockSubscribableCompletable();

    List<Completable> list = Collections.singletonList(completable);
    updater.runCompletablesWithSingleSubscription(list);

    verify(completable).subscribe(any(CompletableObserver.class));
  }

  @Test
  public void
      runCompletablesWithSingleSubscription_multipleCompletable_areExecutedOneAfterTheOther() {
    CompletableSubject completable1 = CompletableSubject.create();
    CompletableSubject completable2 = CompletableSubject.create();

    List<Completable> list = Arrays.asList(completable1, completable2);
    updater.runCompletablesWithSingleSubscription(list);

    assertTrue(completable1.hasObservers());
    assertFalse(completable2.hasObservers());

    completable1.onComplete();

    assertTrue(completable2.hasObservers());
  }

  @Test
  public void runCompletablesWithSingleSubscription_completableError_isEmmittedThroughSubject() {
    TestObserver<Integer> observer = updater.updateEventSubject.test();

    Throwable expectedError = new TestForcedException();
    List<Completable> list = Collections.singletonList(Completable.error(expectedError));

    observer.assertNoErrors();

    updater.runCompletablesWithSingleSubscription(list);

    observer.assertError(expectedError);
  }

  @Test
  public void runCompletablesWithSingleSubscription_addsDisposablesToSendChunksDisposables() {
    Completable completable1 = Completable.never();
    Completable completable2 = Completable.never();

    List<Completable> list = Arrays.asList(completable1, completable2);
    updater.runCompletablesWithSingleSubscription(list);

    assertEquals(1, updater.sendChunksDisposables.size());
  }

  /*
  INCREMENT CHUNK TRANSMITTED
   */
  @Test
  public void incrementChunkTransmitted_incrementsCurrentObjectChunksTransmitted() {
    assertEquals(0, updater.currentObjectChunksTransmitted);

    updater.incrementChunkTransmitted();

    assertEquals(1, updater.currentObjectChunksTransmitted);
  }

  @Test
  public void
      incrementChunkTransmitted_withTotalChunksEqualToTotalChunksTransmittedPlusCurrentObjectChunksTransmitted_returnsFalse() {
    int totalChunks = 100;
    updater.totalChunks = totalChunks;

    updater.totalChunksTransmitted = totalChunks - 10;

    updater.currentObjectChunksTransmitted = 9; // 9++ will be 10, so we'll be equal to totalChunks

    assertFalse(updater.incrementChunkTransmitted());
  }

  @Test
  public void incrementChunkTransmitted_currentObjectFullyTransmitted_returnsFalse() {
    int currentObjectChunks = 10;
    updater.currentObjectTotalChunks = currentObjectChunks;

    updater.currentObjectChunksTransmitted = currentObjectChunks - 1;

    assertFalse(updater.incrementChunkTransmitted());
  }

  @Test
  public void
      incrementChunkTransmitted_currentObjectNotFullyTransmittedAndTotalChunksNotReached_statusWRITE_IN_PROGRESS_returnsTrue() {
    int totalChunks = 100;
    updater.totalChunks = totalChunks;

    updater.totalChunksTransmitted = totalChunks - 50;

    int currentObjectChunks = 10;
    updater.currentObjectTotalChunks = currentObjectChunks;

    updater.currentObjectChunksTransmitted = currentObjectChunks - 2;

    updater.status = OtaWriterStatus.WRITE_IN_PROGRESS;

    assertTrue(updater.incrementChunkTransmitted());
  }

  /*
  NEXT CHUNK DATA
   */
  @Test
  public void nextChunkData_with0ObjectsTransmitted() {
    int totalChunks = 5;
    updater.totalChunks = totalChunks;

    int nextChunk = 3;
    updater.totalChunksTransmitted = nextChunk;

    byte[] data = new byte[totalChunks * BYTES_PER_CHUNK];
    int nextChunkOffset = nextChunk * BYTES_PER_CHUNK;
    Arrays.fill(data, nextChunkOffset, nextChunkOffset + BYTES_PER_CHUNK, (byte) 1);

    mockOTAUpdate(data);

    byte[] expectedData = new byte[BYTES_PER_CHUNK];
    Arrays.fill(expectedData, (byte) 1);

    doReturn(BYTES_PER_CHUNK).when(updater).chunkLength(anyInt());

    assertArrayEquals(expectedData, updater.nextChunkData());
  }

  @Test
  public void nextChunkData_withObjectsTransmitted() {
    int totalChunks = 5;
    updater.totalChunks = totalChunks;

    int nextChunk = 3;
    updater.totalChunksTransmitted = nextChunk;

    int currentObjectChunksTransmitted = 1;
    updater.currentObjectChunksTransmitted = currentObjectChunksTransmitted;

    byte[] data = new byte[totalChunks * BYTES_PER_CHUNK];
    int nextChunkOffset = (nextChunk + currentObjectChunksTransmitted) * BYTES_PER_CHUNK;
    Arrays.fill(data, nextChunkOffset, nextChunkOffset + BYTES_PER_CHUNK, (byte) 1);

    mockOTAUpdate(data);

    byte[] expectedData = new byte[BYTES_PER_CHUNK];
    Arrays.fill(expectedData, (byte) 1);

    doReturn(BYTES_PER_CHUNK).when(updater).chunkLength(anyInt());

    assertArrayEquals(expectedData, updater.nextChunkData());
  }

  @Test
  public void nextChunkData_withSmallerChunkLength() {
    int totalChunks = 5;
    updater.totalChunks = totalChunks;

    int nextChunk = 4;
    updater.totalChunksTransmitted = nextChunk;

    int expectedLastChunkLength = 4;
    byte[] data = new byte[(totalChunks - 1) * BYTES_PER_CHUNK + expectedLastChunkLength];
    int nextChunkOffset = (totalChunks - 1) * BYTES_PER_CHUNK;
    Arrays.fill(data, nextChunkOffset, nextChunkOffset + expectedLastChunkLength, (byte) 1);

    mockOTAUpdate(data);

    byte[] expectedData = new byte[expectedLastChunkLength];
    Arrays.fill(expectedData, (byte) 1);

    doReturn(expectedLastChunkLength).when(updater).chunkLength(anyInt());

    assertArrayEquals(expectedData, updater.nextChunkData());
  }

  /*
  INTERVAL BETWEEN RESPONSES
   */

  @Test
  public void intervalBetweenResponses_handlesPositiveAttemptValuesCorrectly() {
    final int maxAttempts = 100;
    for (int attempt = 0; attempt < maxAttempts; attempt++) {
      updater = spy(new StubFastOtaWriter(driver, Schedulers.io(), attempt));
      int interval = updater.intervalBetweenResponses();
      if (attempt == 0) {
        assertEquals(CHUNK_WITH_RESPONSE_MAX_INTERVAL, interval);
      } else if (attempt == 1) {
        assertEquals(CHUNK_WITH_RESPONSE_MAX_INTERVAL - 1, interval);
      } else {
        assertEquals(CHUNK_WITH_RESPONSE_MIN_INTERVAL, interval);
      }
    }
  }

  @Test
  public void intervalBetweenResponses_recoversFromNegativeValues() {
    for (int attempt = -100; attempt < 0; attempt++) {
      updater = spy(new StubFastOtaWriter(driver, Schedulers.io(), attempt));
      int interval = updater.intervalBetweenResponses();
      assertEquals(CHUNK_WITH_RESPONSE_MAX_INTERVAL, interval);
    }
  }

  /*
  UTILS
   */

  private byte[] createStatusPayloader(int statusByte) {
    return new byte[] {0, (byte) statusByte};
  }

  private OtaUpdate mockOTAUpdate(byte[] data) {
    OtaUpdate otaUpdate = mockOTAUpdate();

    when(otaUpdate.getData()).thenReturn(data);

    return otaUpdate;
  }

  private OtaUpdate mockOTAUpdate() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    updater.otaUpdate = otaUpdate;

    return otaUpdate;
  }

  private OtaUpdate mockOtaUpdate() {
    return mock(OtaUpdate.class);
  }

  @NonNull
  private Completable runnableWriteCharacteristicCompletableMock(
      GattCharacteristic characteristic, byte[] expectedPayload) {
    Completable completable = mockSubscribableCompletable();
    if (characteristic == OTA_UPDATE_WRITE_CHUNK) {
      when(driver.writeOtaChunkCharacteristic(expectedPayload)).thenReturn(completable);
    } else if (characteristic == OTA_UPDATE_START) {
      when(driver.writeOtaUpdateStartCharacteristic(expectedPayload)).thenReturn(completable);
    }
    return completable;
  }

  @NonNull
  private Completable runnableWriteCharacteristicCompletableMock(
      GattCharacteristic characteristic) {
    Completable completable = mockSubscribableCompletable();
    if (characteristic == OTA_UPDATE_WRITE_CHUNK) {
      when(driver.writeOtaChunkCharacteristic(any(byte[].class))).thenReturn(completable);
    } else if (characteristic == OTA_UPDATE_START) {
      when(driver.writeOtaUpdateStartCharacteristic(any(byte[].class))).thenReturn(completable);
    }

    return completable;
  }

  @NonNull
  private Completable mockSubscribableCompletable() {
    Completable completable = mock(Completable.class);
    when(completable.onTerminateDetach()).thenReturn(completable);
    when(completable.subscribe(any(Action.class), any(Consumer.class)))
        .thenReturn(mock(Disposable.class));
    return completable;
  }

  private NextObjectHeader mockNextObjectHeader() {
    return mockNextObjectHeader(0);
  }

  private NextObjectHeader mockNextObjectHeader(int numberOfChunks) {
    return mockNextObjectHeader(numberOfChunks, BYTES_PER_CHUNK, false, 0);
  }

  private NextObjectHeader mockNextObjectHeader(
      int numberOfChunks, int bytesInLastChunk, boolean isLastObject, long crc) {
    NextObjectHeader nextObjectHeader = mock(NextObjectHeader.class);
    when(nextObjectHeader.numberOfChunksToSend()).thenReturn(numberOfChunks);
    when(nextObjectHeader.bytesInLastChunk()).thenReturn(bytesInLastChunk);
    when(nextObjectHeader.isLastObject()).thenReturn(isLastObject);
    when(nextObjectHeader.crc32()).thenReturn(crc);

    return nextObjectHeader;
  }

  @SuppressWarnings("KotlinInternalInJava")
  private static class StubFastOtaWriter extends BaseFastOtaWriter {

    StubFastOtaWriter(BleDriver driver, Scheduler singleScheduler, int attempt) {
      super(driver, singleScheduler, attempt);
    }

    StubFastOtaWriter(BleDriver driver, Scheduler singleScheduler) {
      super(driver, singleScheduler);
    }

    @Override
    protected byte getStartOTACommandId() {
      return 0;
    }

    @Override
    Completable enrichWriteChunkCommand(
        Completable writeChunkCommand, int completableIndex, boolean withResponse) {
      return writeChunkCommand;
    }

    @Override
    protected Observable<Integer> validateUpdatePreconditionsObservable() {
      return null;
    }
  }
}
