package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromProgressiveAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_COMPLETED;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_REBOOTING;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002BootloaderOtaUpdater.CONFIRM_UPDATE_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.connection.state.ConnectionState;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002BootloaderOtaUpdater.OtaStatsLogger;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.error.DeviceNotConnectedException;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("KotlinInternalInJava")
public class KLTB002BootloaderOtaUpdaterTest extends BaseUnitTest {

  KLTB002BootloaderOtaUpdater bootloaderUpdater;

  @Mock InternalKLTBConnection connection;

  @Mock BleDriver driver;

  TestScheduler testScheduler = new TestScheduler();

  @Override
  public void setup() throws Exception {
    super.setup();

    bootloaderUpdater = spy(new KLTB002BootloaderOtaUpdater(connection, driver, 0));
    doReturn(testScheduler).when(bootloaderUpdater).getTimeControlScheduler();
  }

  /*
  UPDATE
   */

  @Test
  public void update_connectionNotActive_emitsError() {
    OtaUpdate otaUpdate = mockUpdate();
    mockStatsLogger();

    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToMainAppObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).confirmUpdateObservable();

    Throwable expectedError = mock(Throwable.class);
    doReturn(Completable.error(expectedError)).when(bootloaderUpdater).checkConnectionIsActive();

    doNothing().when(bootloaderUpdater).close();

    bootloaderUpdater.update(otaUpdate).test().assertError(expectedError);
  }

  @Test
  public void update_connectionNotActive_invokesClose() {
    OtaUpdate otaUpdate = mockUpdate();
    mockStatsLogger();

    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToMainAppObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).confirmUpdateObservable();

    doReturn(Completable.error(mock(Throwable.class)))
        .when(bootloaderUpdater)
        .checkConnectionIsActive();

    doNothing().when(bootloaderUpdater).close();

    bootloaderUpdater.update(otaUpdate).test();

    verify(bootloaderUpdater).close();
  }

  @Test
  public void update_connectionActive_invokesOnOtaStart() {
    OtaUpdate otaUpdate = mockUpdate();
    OtaStatsLogger statsLogger = mockStatsLogger();
    doNothing().when(bootloaderUpdater).onOtaStart(statsLogger);

    doReturn(Completable.complete()).when(bootloaderUpdater).checkConnectionIsActive();
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).otaWriterObservable(otaUpdate);
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToMainAppObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).confirmUpdateObservable();

    bootloaderUpdater.update(otaUpdate).test();

    verify(bootloaderUpdater).onOtaStart(statsLogger);
  }

  @Test
  public void update_rebootToMainAppEmitsError_emitsError() {
    OtaUpdate otaUpdate = mockUpdate();
    mockStatsLogger();

    doReturn(Completable.complete()).when(bootloaderUpdater).checkConnectionIsActive();
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).otaWriterObservable(otaUpdate);
    doReturn(Observable.empty()).when(bootloaderUpdater).confirmUpdateObservable();

    Throwable expectedError = mock(Throwable.class);
    doReturn(Observable.error(expectedError)).when(bootloaderUpdater).rebootToMainAppObservable();

    doNothing().when(bootloaderUpdater).close();

    bootloaderUpdater.update(otaUpdate).test().assertError(expectedError);
  }

  @Test
  public void update_confirmUpdateObservableEmitsCompletes_emitsError() {
    OtaUpdate otaUpdate = mockUpdate();
    mockStatsLogger();

    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Completable.complete()).when(bootloaderUpdater).checkConnectionIsActive();
    doReturn(Observable.empty()).when(bootloaderUpdater).otaWriterObservable(otaUpdate);
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToMainAppObservable();

    Throwable expectedError = mock(Throwable.class);
    doReturn(Observable.error(expectedError)).when(bootloaderUpdater).confirmUpdateObservable();

    doNothing().when(bootloaderUpdater).close();

    bootloaderUpdater.update(otaUpdate).test().assertError(expectedError);
  }

  @Test
  public void update_allOperationsRun_completesSuccessfully() {
    OtaUpdate otaUpdate = mockUpdate();
    mockStatsLogger();

    doReturn(Completable.complete()).when(bootloaderUpdater).checkConnectionIsActive();
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).otaWriterObservable(otaUpdate);
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToMainAppObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).confirmUpdateObservable();

    doNothing().when(bootloaderUpdater).close();

    bootloaderUpdater.update(otaUpdate).test().assertNoErrors().assertComplete();
  }

  @Test
  public void update_allOperationsRun_invokesClose() {
    OtaUpdate otaUpdate = mockUpdate();
    mockStatsLogger();

    doReturn(Completable.complete()).when(bootloaderUpdater).checkConnectionIsActive();
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToBootloaderObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).otaWriterObservable(otaUpdate);
    doReturn(Observable.empty()).when(bootloaderUpdater).rebootToMainAppObservable();
    doReturn(Observable.empty()).when(bootloaderUpdater).confirmUpdateObservable();

    doNothing().when(bootloaderUpdater).close();

    bootloaderUpdater.update(otaUpdate).test().assertNoErrors().assertComplete();

    verify(bootloaderUpdater).close();
  }

  /*
  OTA WRITER OBSERVABLE
   */

  @Test
  public void otaWriterObservable_updateIsDeferredUntilSubscription() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    OtaWriter otaWriter = mock(OtaWriter.class);
    doReturn(otaWriter).when(bootloaderUpdater).otaWriter(otaUpdate);

    PublishSubject<Integer> progressSubject = PublishSubject.create();
    when(otaWriter.write(otaUpdate)).thenReturn(progressSubject);

    bootloaderUpdater.otaWriterObservable(otaUpdate);

    verify(otaWriter, never()).write(any(OtaUpdate.class));

    bootloaderUpdater.otaWriterObservable(otaUpdate).test();

    verify(otaWriter).write(any(OtaUpdate.class));
  }

  @Test
  public void otaWriterObservable_mapsProgressToOtaUpdateEvent() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    OtaWriter otaWriter = mock(OtaWriter.class);
    doReturn(otaWriter).when(bootloaderUpdater).otaWriter(otaUpdate);

    PublishSubject<Integer> progressSubject = PublishSubject.create();
    when(otaWriter.write(otaUpdate)).thenReturn(progressSubject);

    TestObserver<OtaUpdateEvent> observer = bootloaderUpdater.otaWriterObservable(otaUpdate).test();

    observer.assertEmpty();

    progressSubject.onNext(1);

    OtaUpdateEvent expectedEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, 1);
    observer.assertValue(expectedEvent);

    progressSubject.onNext(50);

    OtaUpdateEvent expectedEvent2 = fromProgressiveAction(OTA_UPDATE_INSTALLING, 50);
    observer.assertValues(expectedEvent, expectedEvent2);
  }

  /*
  DFU_BOOTLOADER REBOOTER
   */

  @Test
  public void bootloaderRebooterObservable_invokesRebootToBootloaderAndDisconnect() {
    BootloaderRebooter rebooter = mock(BootloaderRebooter.class);
    mockToothbrushRebooter(rebooter);

    bootloaderUpdater.rebootToBootloaderObservable().test();

    verify(rebooter).rebootToBootloader(connection, driver);
  }

  @Test
  public void bootloaderRebooterObservable_startsWithOtaEventRebooting() {
    mockToothbrushRebooter();

    TestObserver<OtaUpdateEvent> observer = bootloaderUpdater.rebootToBootloaderObservable().test();

    observer.assertValue(fromAction(OTA_UPDATE_REBOOTING));
  }

  /*
  CLOSE
   */

  @Test
  public void close_neverTouchesDriver() {
    doNothing().when(bootloaderUpdater).maybeFlagConnectionAsActive();

    bootloaderUpdater.close();

    verifyNoMoreInteractions(driver);
  }

  @Test
  public void close_invokesMaybeFlagConnectionAsActive() {
    doNothing().when(bootloaderUpdater).maybeFlagConnectionAsActive();

    bootloaderUpdater.close();

    verify(bootloaderUpdater).maybeFlagConnectionAsActive();
  }

  /*
  MAYBE FLAG CONNECTION AS ACTIVE
   */

  @Test
  public void maybeFlagConnectionAsActive_connectionOTA_invokesSetActive() {
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connectionState.getCurrent()).thenReturn(KLTBConnectionState.OTA);
    when(connection.state()).thenReturn(connectionState);

    bootloaderUpdater.maybeFlagConnectionAsActive();

    verify(connection).setState(KLTBConnectionState.ACTIVE);
  }

  @Test
  public void maybeFlagConnectionAsActive_connectionOther_neverInvokesSetState() {
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connection.state()).thenReturn(connectionState);

    for (KLTBConnectionState state : KLTBConnectionState.values()) {
      if (state == KLTBConnectionState.OTA) {
        continue;
      }

      when(connectionState.getCurrent()).thenReturn(state);
      bootloaderUpdater.maybeFlagConnectionAsActive();

      verify(connection, never()).setState(any(KLTBConnectionState.class));
    }
  }

  /*
  CHECK CONNECTION STATE
   */
  @Test
  public void checkConnectionIsActive_notActive_throwsDeviceNotConnectedException() {
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connectionState.getCurrent()).thenReturn(KLTBConnectionState.ESTABLISHING);
    when(connection.state()).thenReturn(connectionState);

    bootloaderUpdater
        .checkConnectionIsActive()
        .test()
        .assertError(DeviceNotConnectedException.class);
  }

  @Test
  public void checkConnectionIsActive_active_doesNothing() {
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connectionState.getCurrent()).thenReturn(KLTBConnectionState.ACTIVE);
    when(connection.state()).thenReturn(connectionState);

    bootloaderUpdater.checkConnectionIsActive().test().assertNoErrors().assertComplete();
  }

  /*
  CONFIRM UPDATE COMPLETABLE
   */
  @Test
  public void confirmUpdateCompletable_returnsCompletableToWriteCharacteristic() {
    byte[] expectedPayload = new PayloadWriter(4).writeInt32(CONFIRM_UPDATE_CODE).getBytes();

    when(driver.writeOtaUpdateValidateCharacteristic(expectedPayload))
        .thenReturn(Completable.complete());

    bootloaderUpdater
        .confirmUpdateObservable()
        .test()
        .assertValue(fromAction(OTA_UPDATE_COMPLETED))
        .assertComplete();

    verify(driver).writeOtaUpdateValidateCharacteristic(expectedPayload);
  }

  /*
  ON OTA START
   */

  @Test
  public void onOtaStart_flagsConnectionAsStateOta() {
    bootloaderUpdater.onOtaStart(mock(OtaStatsLogger.class));

    verify(connection).setState(KLTBConnectionState.OTA);
  }

  @Test
  public void onOtaStart_invokesOnUpdateStart() {
    OtaStatsLogger otaStatsLogger = mock(OtaStatsLogger.class);
    bootloaderUpdater.onOtaStart(otaStatsLogger);

    verify(otaStatsLogger).onUpdateStart();
  }

  /*
  UTILS
   */

  private OtaUpdate mockUpdate() {
    return mock(OtaUpdate.class);
  }

  private OtaStatsLogger mockStatsLogger() {
    OtaStatsLogger otaStatsLogger = mock(OtaStatsLogger.class);

    doReturn(otaStatsLogger).when(bootloaderUpdater).createStatsLogger();

    return otaStatsLogger;
  }

  private Completable mockToothbrushRebooter() {
    return mockToothbrushRebooter(mock(BootloaderRebooter.class));
  }

  private Completable mockToothbrushRebooter(BootloaderRebooter toothbrushRebooter) {
    doReturn(toothbrushRebooter).when(bootloaderUpdater).bootloaderRebooter();

    Completable completable = Completable.create(CompletableEmitter::onComplete);

    when(toothbrushRebooter.rebootToBootloader(
            any(InternalKLTBConnection.class), any(BleDriver.class)))
        .thenReturn(completable);

    return completable;
  }
}
