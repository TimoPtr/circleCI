package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromProgressiveAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002ToothbrushUpdater.GLOBAL_OTA_TIMEOUT_SECONDS;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002ToothbrushUpdater.MAX_OTA_RECOVERY_ATTEMPTS;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.KLTB002ToothbrushUpdater.RECONNECTION_TIMEOUT_SECONDS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdateFactory;
import com.kolibree.android.sdk.error.CriticalFailureException;
import com.kolibree.android.sdk.error.FailureReason;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.PublishSubject;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("KotlinInternalInJava")
public class KLTB002ToothbrushUpdaterTest extends BaseUnitTest {

  KLTB002ToothbrushUpdater updater;

  @Mock InternalKLTBConnection connection;

  @Mock ConnectionStateMonitor connectionStateMonitor;

  @Mock BleDriver driver;

  @Mock IBluetoothUtils bluetoothUtils;

  @Mock OtaUpdateFactory otaUpdateFactory;

  @Mock OtaToolsFactory otaToolsFactory;

  TestScheduler timeScheduler = new TestScheduler();

  @Override
  public void setup() throws Exception {
    super.setup();

    final Toothbrush toothbrush = mock(Toothbrush.class);
    when(toothbrush.getModel()).thenReturn(ToothbrushModel.ARA);

    when(connection.toothbrush()).thenReturn(toothbrush);

    updater =
        spy(
            new KLTB002ToothbrushUpdater(
                connection,
                bluetoothUtils,
                driver,
                otaUpdateFactory,
                otaToolsFactory,
                connectionStateMonitor));

    doReturn(timeScheduler).when(updater).getTimeControlScheduler();
  }

  @Override
  public void tearDown() throws Exception {
    FailEarly.overrideDelegateWith(TestDelegate.INSTANCE);
    super.tearDown();
  }

  /*
  UPDATE
   */

  @Test
  public void update_otaUpdateFactoryThrowsException_returnsObservableError() throws Exception {
    doThrow(mock(IllegalStateException.class)).when(otaUpdateFactory).create(any(), any());

    updater.update(mock(AvailableUpdate.class)).test().assertError(IllegalStateException.class);
  }

  @Test
  public void update_withNonCompatibleOTAUpdate_returnsObservableError() throws Exception {
    OtaUpdate otaUpdate = mockUpdate();

    doThrow(mock(IllegalStateException.class)).when(updater).checkUpdateCompatibility(otaUpdate);

    updater.update(mock(AvailableUpdate.class)).test().assertError(IllegalStateException.class);
  }

  @Test
  public void update_checkCRCThrowsException_returnsObservableError() throws Exception {
    OtaUpdate otaUpdate = mockUpdate();

    doNothing().when(updater).checkUpdateCompatibility(otaUpdate);
    doThrow(mock(IllegalStateException.class)).when(otaUpdate).checkCRC();

    updater.update(mock(AvailableUpdate.class)).test().assertError(IllegalStateException.class);
  }

  @Test
  public void update_withCompatibleOTAUpdate_withValidCRC_returnsCorrectObservable()
      throws Exception {
    OtaUpdate otaUpdate = mockUpdate();

    doNothing().when(updater).checkUpdateCompatibility(otaUpdate);

    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);

    Observable<OtaUpdateEvent> expectedObservable = Observable.empty();
    doReturn(expectedObservable).when(updater).internalUpdate(otaUpdate);

    doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ACTIVE)
                .build()
                .state())
        .when(connection)
        .state();

    TestObserver<OtaUpdateEvent> testObserver = updater.update(mock(AvailableUpdate.class)).test();
    testObserver.assertNoValues();
    testObserver.assertNoErrors();
    testObserver.assertComplete();
  }

  /*
   * INTERNAL UPDATE
   */

  @Test
  public void internalUpdate_withEligibleOATUpdate_returnsCorrectObservable() throws Exception {
    OtaUpdateEvent expectedEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, 56);

    PublishSubject<OtaUpdateEvent> eventPublishSubject = PublishSubject.create();
    OtaUpdate otaUpdate = mockUpdate();
    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);
    doReturn(eventPublishSubject).when(otaUpdater).update(otaUpdate);

    doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.OTA)
                .build()
                .state())
        .when(connection)
        .state();

    TestObserver<OtaUpdateEvent> observer = updater.internalUpdate(otaUpdate).test();
    eventPublishSubject.onNext(expectedEvent);
    eventPublishSubject.onComplete();

    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(expectedEvent);
  }

  @Test
  public void
      internalUpdate_withEligibleOATUpdate_throwsTimeoutAfterPeriodAndChangesConnectionToActive()
          throws Exception {
    OtaUpdate otaUpdate = mockUpdate();
    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);
    doReturn(Observable.never()).when(otaUpdater).update(otaUpdate);

    doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.OTA)
                .build()
                .state())
        .when(connection)
        .state();

    TestObserver<OtaUpdateEvent> observer = updater.internalUpdate(otaUpdate).test();
    timeScheduler.advanceTimeBy(GLOBAL_OTA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

    observer.assertError(TimeoutException.class);
    verify(connection).setState(KLTBConnectionState.ACTIVE);
  }

  @Test
  public void internalUpdate_withEligibleOATUpdate_attemptsRecoveryInCaseOfError()
      throws Exception {
    OtaUpdateEvent expectedEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, 56);
    IllegalStateException expectedThrowable = new IllegalStateException("Initial OTA failed");

    OtaUpdate otaUpdate = mockUpdate();
    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);
    doReturn(Observable.error(expectedThrowable)).when(otaUpdater).update(otaUpdate);
    doReturn(Observable.just(expectedEvent))
        .when(updater)
        .tryToRecoverOta(eq(otaUpdate), any(), eq(expectedThrowable));

    doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.OTA)
                .build()
                .state())
        .when(connection)
        .state();

    TestObserver<OtaUpdateEvent> observer = updater.internalUpdate(otaUpdate).test();

    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(expectedEvent);
  }

  @Test
  public void internalUpdate_triesToRecoverMultipleTimes() throws Exception {
    IllegalStateException expectedThrowable = new IllegalStateException("Initial OTA failed");

    OtaUpdate otaUpdate = mockUpdate();
    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(Completable.complete()).when(connectionStateMonitor).waitForActiveConnection();
    doReturn(Completable.complete()).when(updater).waitForEnabledBluetooth();
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);
    doReturn(Observable.error(expectedThrowable)).when(otaUpdater).update(otaUpdate);
    doReturn(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.OTA)
                .build()
                .state())
        .when(connection)
        .state();
    doReturn(
            Observable.fromCallable(
                (Callable<OtaUpdateEvent>)
                    () -> {
                      throw new FailureReason(
                          "Recovery number " + updater.attemptCounter.incrementAndGet());
                    }))
        .when(updater)
        .recoverOta(eq(otaUpdate), anyInt());

    TestObserver<OtaUpdateEvent> observer = updater.internalUpdate(otaUpdate).test();

    observer.assertError(FailureReason.class);
    observer.assertErrorMessage("Recovery number " + MAX_OTA_RECOVERY_ATTEMPTS);
  }

  /*
   * TRY TO RECOVER OTA
   */

  @Test
  public void tryToRecoverOta_returnsErrorOnUnhandledExceptionType() throws Exception {
    OtaUpdate otaUpdate = mockUpdate();
    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);
    CriticalFailureException expectedThrowable =
        new CriticalFailureException("Unhandled exception");

    TestObserver<OtaUpdateEvent> observer =
        updater.tryToRecoverOta(otaUpdate, new AtomicInteger(), expectedThrowable).test();

    observer.assertError(expectedThrowable);
  }

  @Test
  public void tryToRecoverOta_recoversFromFailureReason() throws Exception {
    OtaUpdateEvent expectedEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, 56);
    FailureReason unexpectedThrowable = new FailureReason("Recoverable exception");

    OtaUpdate otaUpdate = mockUpdate();
    doReturn(Completable.complete()).when(updater).waitForEnabledBluetooth();
    doReturn(Observable.just(expectedEvent)).when(updater).recoverOta(otaUpdate, 1);

    TestObserver<OtaUpdateEvent> observer =
        updater.tryToRecoverOta(otaUpdate, new AtomicInteger(), unexpectedThrowable).test();

    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(expectedEvent);
  }

  /*
  RECOVER OTA
   */

  @Test
  public void recoverOta_recoversWhenAllIsOk() throws Exception {
    OtaUpdateEvent expectedEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, 56);

    OtaUpdate otaUpdate = mockUpdate();
    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);
    doReturn(Observable.just(expectedEvent)).when(otaUpdater).update(otaUpdate);
    doReturn(Completable.complete()).when(connection).reconnectCompletable();
    doReturn(Completable.complete()).when(connectionStateMonitor).waitForActiveConnection();

    TestObserver<OtaUpdateEvent> observer = updater.recoverOta(otaUpdate, 0).test();

    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(expectedEvent);
  }

  @Test
  public void recoverOta_timeoutsIfNoValueIsTransmitted() throws Exception {
    OtaUpdate otaUpdate = mockUpdate();
    OtaUpdater otaUpdater = mock(OtaUpdater.class);
    doReturn(otaUpdater).when(updater).otaUpdater(otaUpdate, 0);
    doReturn(Observable.never()).when(otaUpdater).update(otaUpdate);
    doReturn(Completable.complete()).when(connection).reconnectCompletable();
    doReturn(Completable.never()).when(connectionStateMonitor).waitForActiveConnection();

    TestObserver<OtaUpdateEvent> observer = updater.recoverOta(otaUpdate, 0).test();

    observer.assertNoErrors();
    observer.assertNoValues();
    observer.assertNotComplete();

    timeScheduler.advanceTimeBy(RECONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

    observer.assertError(FailureReason.class);
    observer.assertErrorMessage("Reconnection timeout during OTA recovery attempt");
  }

  /*
  OTA UPDATER
   */
  @Test
  public void otaUpdater_returnsValueFromOtaUpdaterFactory() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    OtaUpdater expectedOtaUpdater = mock(OtaUpdater.class);
    when(otaToolsFactory.createOtaUpdater(connection, driver, otaUpdate, 0))
        .thenReturn(expectedOtaUpdater);

    assertEquals(expectedOtaUpdater, updater.otaUpdater(otaUpdate, 0));
  }

  /*
  GET MODEL
   */
  @Test
  public void getModel_returnsModelFromConnection() {
    Toothbrush toothbrush = mock(Toothbrush.class);
    ToothbrushModel expectedModel = ToothbrushModel.CONNECT_M1;
    when(toothbrush.getModel()).thenReturn(expectedModel);
    when(connection.toothbrush()).thenReturn(toothbrush);

    assertEquals(expectedModel, updater.getModel());
  }

  /*
  GET FIRMWARE
   */
  @Test
  public void getFirmwareVersion_returnsFirmwareVersionFromConnection() {
    Toothbrush toothbrush = mock(Toothbrush.class);
    SoftwareVersion expectedSoftwareVersion = new SoftwareVersion(1, 2, 3);
    when(toothbrush.getFirmwareVersion()).thenReturn(expectedSoftwareVersion);
    when(connection.toothbrush()).thenReturn(toothbrush);

    assertEquals(expectedSoftwareVersion, updater.getFirmwareVersion());
  }

  /*
  GET HARDWARE
   */
  @Test
  public void getHardwareVersion_returnsFirmwareVersionFromConnection() {
    Toothbrush toothbrush = mock(Toothbrush.class);
    HardwareVersion expectedHardwareVersion = new HardwareVersion(1, 2);
    when(toothbrush.getHardwareVersion()).thenReturn(expectedHardwareVersion);
    when(connection.toothbrush()).thenReturn(toothbrush);

    assertEquals(expectedHardwareVersion, updater.getHardwareVersion());
  }

  /*
  CHECK UPDATE COMPATIBILITY
   */
  @Test(expected = IllegalStateException.class)
  public void checkUpdateCompatibility_updateIsNotCompatibleWithModel_throwsException()
      throws IllegalStateException {
    ToothbrushModel model = ToothbrushModel.ARA;
    doReturn(model).when(updater).getModel();

    OtaUpdate otaUpdate = mock(OtaUpdate.class);
    when(otaUpdate.isCompatible(model)).thenReturn(false);

    updater.checkUpdateCompatibility(otaUpdate);
  }

  @Test(expected = IllegalStateException.class)
  public void checkUpdateCompatibility_updateIsNotCompatibleWithFirmware_throwsException()
      throws IllegalStateException {
    SoftwareVersion softwareVersion = new SoftwareVersion(1, 2, 3);
    doReturn(softwareVersion).when(updater).getFirmwareVersion();

    OtaUpdate otaUpdate = mock(OtaUpdate.class);
    when(otaUpdate.isCompatible(softwareVersion)).thenReturn(false);

    ToothbrushModel model = ToothbrushModel.ARA;
    doReturn(model).when(updater).getModel();
    when(otaUpdate.isCompatible(model)).thenReturn(true);

    updater.checkUpdateCompatibility(otaUpdate);
  }

  @Test
  public void checkUpdateCompatibility_compatibleWithEverything_doesNothing()
      throws IllegalStateException {
    ToothbrushModel model = ToothbrushModel.ARA;
    doReturn(model).when(updater).getModel();

    SoftwareVersion softwareVersion = new SoftwareVersion(1, 2, 3);
    doReturn(softwareVersion).when(updater).getFirmwareVersion();

    OtaUpdate otaUpdate = mock(OtaUpdate.class);
    when(otaUpdate.isCompatible(softwareVersion)).thenReturn(true);
    when(otaUpdate.isCompatible(model)).thenReturn(true);

    updater.checkUpdateCompatibility(otaUpdate);
  }

  /*
   * WAIT FOR ENABLED BLUETOOTH
   */

  @Test
  public void waitForEnabledBluetooth_waitsIfBluetoothIsNotEnabled() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(Observable.never());

    TestObserver<Void> observer = updater.waitForEnabledBluetooth().test();

    observer.assertNotComplete();
    observer.assertNoErrors();
    observer.assertNoValues();
  }

  @Test
  public void waitForEnabledBluetooth_finishesIfBluetoothIsEnabled() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(Observable.never());

    TestObserver<Void> observer = updater.waitForEnabledBluetooth().test();

    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertNoValues();
  }

  @Test
  public void waitForEnabledBluetooth_finishesIfBluetoothIsEnabledRightAfter() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);
    when(bluetoothUtils.bluetoothStateObservable())
        .thenReturn(Observable.just(true).publish().refCount());

    TestObserver<Void> observer = updater.waitForEnabledBluetooth().test();

    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertNoValues();
  }

  @Test
  public void waitForEnabledBluetooth_finishesIfBluetoothGetsEnabledLater() {
    PublishSubject<Boolean> publishSubject = PublishSubject.create();
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(publishSubject.publish().refCount());

    TestObserver<Void> observer = updater.waitForEnabledBluetooth().test();

    publishSubject.onNext(false);
    observer.assertNotComplete();

    publishSubject.onNext(true);
    observer.assertComplete();
  }

  private OtaUpdate mockUpdate() throws Exception {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);
    when(otaUpdateFactory.create(any(), any())).thenReturn(otaUpdate);
    return otaUpdate;
  }
}
