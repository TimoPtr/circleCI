package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromProgressiveAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_COMPLETED;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.test.mocks.KLTBConnectionBuilder.DEFAULT_MAC;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionState;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.KLTBConnectionProvider;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.error.DeviceNotConnectedException;
import com.kolibree.android.sdk.version.SoftwareVersion;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.SingleSubject;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("KotlinInternalInJava")
public class KLTB002FastGruUpdaterTest extends BaseUnitTest {

  private static final int SUBSCRIPTION_DELAY_IN_SECONDS = 0;
  @Mock InternalKLTBConnection connection;
  @Mock BleDriver driver;
  @Mock FastGruWriter fastGruWriter;
  @Mock KLTBConnectionProvider connectionProvider;
  TestScheduler testScheduler = new TestScheduler();
  private KLTB002FastGruUpdater fastGruUpdater;

  @Override
  public void setup() throws Exception {
    super.setup();

    fastGruUpdater =
        spy(
            new KLTB002FastGruUpdater(
                connection,
                driver,
                fastGruWriter,
                connectionProvider,
                SUBSCRIPTION_DELAY_IN_SECONDS));
    doReturn(testScheduler).when(fastGruUpdater).getTimeControlScheduler();
  }

  /*
  UPDATE
   */
  @Test
  public void update_connectionIsActiveCompletes_concatsWithOtaWriterObservable() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    CompletableEmitter[] emitters = new CompletableEmitter[1];
    Completable completable = Completable.create(emitter -> emitters[0] = emitter);
    doReturn(completable).when(fastGruUpdater).validateConnectionState();

    OtaUpdateEvent expectedEvent = OtaUpdateEvent.fromAction(OTA_UPDATE_COMPLETED);
    doReturn(Observable.just(expectedEvent)).when(fastGruUpdater).otaWriterObservable(otaUpdate);

    doReturn(Completable.complete()).when(fastGruUpdater).updateVersion(otaUpdate);

    TestObserver<OtaUpdateEvent> observer = fastGruUpdater.update(otaUpdate).test();

    observer.assertEmpty();

    emitters[0].onComplete();

    observer.assertValue(expectedEvent);

    testScheduler.advanceTimeBy(SUBSCRIPTION_DELAY_IN_SECONDS, TimeUnit.SECONDS);

    observer.assertComplete();
  }

  @Test
  public void update_completesSuccessfully_concatsWithUpdateVersions() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    doReturn(Completable.complete()).when(fastGruUpdater).validateConnectionState();
    PublishSubject<OtaUpdateEvent> subject = PublishSubject.create();
    doReturn(subject).when(fastGruUpdater).otaWriterObservable(otaUpdate);

    CompletableSubject uploadVersionsSubject = CompletableSubject.create();
    doReturn(uploadVersionsSubject).when(fastGruUpdater).updateVersion(otaUpdate);

    fastGruUpdater.update(otaUpdate).test();

    assertFalse(uploadVersionsSubject.hasObservers());

    subject.onComplete();

    assertFalse(uploadVersionsSubject.hasObservers());

    testScheduler.advanceTimeBy(SUBSCRIPTION_DELAY_IN_SECONDS, TimeUnit.SECONDS);

    assertTrue(uploadVersionsSubject.hasObservers());
  }

  @Test
  public void update_connectionIsActiveError_emitsError() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    CompletableEmitter[] emitters = new CompletableEmitter[1];
    Completable completable = Completable.create(emitter -> emitters[0] = emitter);
    doReturn(completable).when(fastGruUpdater).validateConnectionState();

    doReturn(Observable.empty()).when(fastGruUpdater).otaWriterObservable(otaUpdate);

    doReturn(Completable.complete()).when(fastGruUpdater).updateVersion(otaUpdate);

    TestObserver<OtaUpdateEvent> observer = fastGruUpdater.update(otaUpdate).test();

    observer.assertEmpty();

    Throwable expectedError = mock(Throwable.class);
    emitters[0].onError(expectedError);

    observer.assertError(expectedError);
  }

  /*
  UPDATE VERSION
   */
  @Test
  public void updateVersion_waitsForConnectionProviderToEmitBeforeAttemptingToReloadVersion() {
    prepareConnectionMac();

    SingleSubject<KLTBConnection> providerSubject = SingleSubject.create();
    when(connectionProvider.existingConnectionWithStates(eq(DEFAULT_MAC), any()))
        .thenReturn(providerSubject);

    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    CompletableSubject reloadSubject = CompletableSubject.create();
    when(driver.reloadVersions()).thenReturn(reloadSubject);

    fastGruUpdater.updateVersion(otaUpdate).test();

    assertFalse(reloadSubject.hasObservers());

    providerSubject.onSuccess(connection);

    assertTrue(reloadSubject.hasObservers());
  }

  @Test
  public void updateVersion_invokesConnectionSetGruDataUpdatedVersionAfterReloadDriverCompletes() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);
    SoftwareVersion expectedSoftwareVersion = SoftwareVersion.NULL;
    when(otaUpdate.getVersion()).thenReturn(expectedSoftwareVersion);

    prepareConnectionMac();

    when(connectionProvider.existingConnectionWithStates(eq(DEFAULT_MAC), any()))
        .thenReturn(Single.just(connection));

    CompletableSubject reloadSubject = CompletableSubject.create();
    when(driver.reloadVersions()).thenReturn(reloadSubject);

    fastGruUpdater.updateVersion(otaUpdate).test();

    verify(connection, never()).setGruDataUpdatedVersion(any(SoftwareVersion.class));

    reloadSubject.onComplete();

    verify(connection).setGruDataUpdatedVersion(expectedSoftwareVersion);
  }

  /*
  OTA WRITER OBSERVABLE
   */
  @Test
  public void otaWriterObservable_invokesFastGruWriterWrite() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    doReturn(Observable.empty()).when(fastGruWriter).write(any(OtaUpdate.class));

    //noinspection ResultOfMethodCallIgnored
    fastGruUpdater.internalOtaWriterObservable(otaUpdate).test();

    verify(fastGruWriter).write(otaUpdate);
  }

  @Test
  public void otaWriterObservable_mapsProgressToOtaUpdateEvent() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    PublishSubject<Integer> progressSubject = PublishSubject.create();
    doReturn(progressSubject).when(fastGruWriter).write(any(OtaUpdate.class));

    TestObserver<OtaUpdateEvent> observer =
        fastGruUpdater.internalOtaWriterObservable(otaUpdate).test();

    observer.assertEmpty();

    int progress = 78;
    progressSubject.onNext(progress);

    OtaUpdateEvent expectedEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, progress);

    observer.assertValue(expectedEvent);
  }

  @Test
  public void otaWriterObservable_emitsErrors() {
    OtaUpdate otaUpdate = mock(OtaUpdate.class);

    PublishSubject<Integer> progressSubject = PublishSubject.create();
    doReturn(progressSubject).when(fastGruWriter).write(any(OtaUpdate.class));

    TestObserver<OtaUpdateEvent> observer =
        fastGruUpdater.internalOtaWriterObservable(otaUpdate).test();

    observer.assertEmpty();

    Throwable expectedError = mock(Throwable.class);
    progressSubject.onError(expectedError);

    observer.assertError(expectedError);
  }

  /*
  VALIDATE CONNECTION STATE
   */
  @Test
  public void validateConnectionState_notInBootloader_active_completes() {
    ConnectionState state = mock(ConnectionState.class);
    when(state.getCurrent()).thenReturn(KLTBConnectionState.ACTIVE);
    when(connection.state()).thenReturn(state);

    when(driver.isRunningBootloader()).thenReturn(false);

    fastGruUpdater.validateConnectionState().test().assertComplete();
  }

  @Test
  public void
      validateConnectionState_notInBootloader_connectionNotActive_emitsDeviceNotConnectedException() {
    ConnectionState state = mock(ConnectionState.class);
    when(state.getCurrent()).thenReturn(KLTBConnectionState.ESTABLISHING);
    when(connection.state()).thenReturn(state);

    when(driver.isRunningBootloader()).thenReturn(false);

    fastGruUpdater.validateConnectionState().test().assertError(DeviceNotConnectedException.class);
  }

  @Test
  public void validateConnectionState_inBootloader_active_emitsIllegalStateException() {
    when(driver.isRunningBootloader()).thenReturn(true);

    fastGruUpdater.validateConnectionState().test().assertError(IllegalStateException.class);
  }

  private void prepareConnectionMac() {
    Toothbrush toothbrush = mock(Toothbrush.class);
    when(toothbrush.getMac()).thenReturn(DEFAULT_MAC);
    when(connection.toothbrush()).thenReturn(toothbrush);
  }
}
