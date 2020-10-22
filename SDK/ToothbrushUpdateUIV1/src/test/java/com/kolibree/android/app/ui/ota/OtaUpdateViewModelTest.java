package com.kolibree.android.app.ui.ota;
/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.COMPLETED;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.ERROR;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.IN_PROGRESS;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.NOT_STARTED;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_EXIT_CANCEL;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_EXIT_SUCCESS;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_INSTALLING;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_REBOOTING;
import static com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER;
import static com.kolibree.android.commons.UpdateType.TYPE_DSP;
import static com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE;
import static com.kolibree.android.commons.UpdateType.TYPE_GRU;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromError;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromProgressiveAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_BLOCKED_NOT_CHARGING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_COMPLETED;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_ERROR;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_REBOOTING;
import static com.kolibree.android.test.mocks.KLTBConnectionBuilder.DEFAULT_MAC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.res.Resources;
import androidx.annotation.NonNull;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.test.TestForcedException;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCase;
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker;
import com.kolibree.android.toothbrushupdate.R;
import com.kolibree.sdkws.data.model.GruwareData;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("KotlinInternalInJava")
public class OtaUpdateViewModelTest extends BaseUnitTest {

  @Mock ServiceProvider serviceProvider;

  @Mock Resources resources;

  @Mock CheckOtaUpdatePrerequisitesUseCase checkOtaUpdatePrerequisitesUseCase;

  @Mock OtaUpdater otaUpdater;

  private StubOTaUpdateViewModel viewModel;
  private OtaUpdateViewState viewState;

  @Override
  public void setup() throws Exception {
    super.setup();

    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();

    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);
  }

  /*
  ON KOLIBREE SERVICE CONNECTED
   */

  @Test
  public void
      onKolibreeServiceConnected_updaterStatusNotStarted_invokesOnReceivedConnectionFromService() {
    initNoMandatoryUpdate();

    KolibreeService service = mock(KolibreeService.class);
    KLTBConnection expectedConnection = mock(KLTBConnection.class);
    when(service.getConnection(DEFAULT_MAC)).thenReturn(expectedConnection);

    doNothing().when(viewModel).onReceivedConnectionFromService(any(KLTBConnection.class));

    viewModel.updateStatus = UpdateStatus.NOT_STARTED;

    viewModel.onKolibreeServiceConnected(service);

    verify(viewModel).onReceivedConnectionFromService(expectedConnection);
  }

  @Test
  public void
      onKolibreeServiceConnected_updaterStatusOther_neverInvokesOnReceivedConnectionFromService() {
    initNoMandatoryUpdate();

    KolibreeService service = mock(KolibreeService.class);

    doReturn("da").when(viewModel).getString(anyInt());

    for (UpdateStatus value : UpdateStatus.values()) {
      if (value == NOT_STARTED) {
        continue;
      }

      viewModel.onKolibreeServiceConnected(service);
    }

    verify(viewModel, never()).onReceivedConnectionFromService(any(KLTBConnection.class));
  }

  /*
  CAN DISCONNECT FROM SERVICE
   */

  @Test
  public void canDisconnectFromService_returnsCanDisconnectFromServiceRelayWithDefaultValueTrue() {
    initNoMandatoryUpdate();

    viewModel.canDisconnectFromService().test().assertValue(true);
  }

  /*
  ON RECEIVED CONNECTION FROM SERVICE
   */

  @Test
  public void
      onReceivedConnectionFromService_isValidConnectionFalse_emitsErrorConnectionNotFound() {
    initNoMandatoryUpdate();

    assertViewModelInInitialState();

    String expectedError = "expecdd";
    doReturn(expectedError)
        .when(viewModel)
        .getString(R.string.popup_toothbrush_unavailable_message);

    doReturn(false).when(viewModel).isValidConnection(any());

    viewModel.onReceivedConnectionFromService(null);

    OtaUpdateViewState expectedViewState = viewState.withOtaError(expectedError);

    assertLastViewState(expectedViewState);
  }

  @Test
  public void
      onReceivedConnectionFromService_withConnection_emitsConnectionToSubjectAndCompletes() {
    initNoMandatoryUpdate();

    assertViewModelInInitialState();

    viewModel.connectionSubject.test().assertEmpty();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withModel(ToothbrushModel.ARA).build();

    doReturn(true).when(viewModel).isValidConnection(any());

    viewModel.onReceivedConnectionFromService(connection);

    viewModel.connectionSubject.test().assertValue(connection);

    assertViewModelInInitialState();
  }

  /*
  IS VALID CONNECTION
   */
  @Test
  public void isValidConnection_nullConnection_returnsFalse() {
    initNoMandatoryUpdate();

    assertFalse(viewModel.isValidConnection(null));
  }

  @Test
  public void isValidConnection_inactiveConnection_returnsFalse() {
    initNoMandatoryUpdate();

    assertFalse(
        viewModel.isValidConnection(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.TERMINATED)
                .build()));
  }

  @Test
  public void isValidConnection_activeConnection_returnsTrue() {
    initNoMandatoryUpdate();

    assertTrue(
        viewModel.isValidConnection(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ACTIVE)
                .build()));
  }

  /*
  SHOULD START UPDATE
   */
  @Test
  public void shouldStartUpdate_updateInProgress_returnsFalse() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = UpdateStatus.IN_PROGRESS;

    assertFalse(viewModel.shouldStartUpdate());
  }

  @Test
  public void
      shouldStartUpdate_updateInProgressNOT_STARTED_waitingForConnectionisTrue_returnsFalse() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = NOT_STARTED;
    viewModel.isWaitingForConnection = true;

    assertFalse(viewModel.shouldStartUpdate());
  }

  @Test
  public void
      shouldStartUpdate_updateInProgressFalse_waitingForConnectionDisposableNull_returnsTrue() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = NOT_STARTED;

    assertTrue(viewModel.shouldStartUpdate());
  }

  /*
  ON USER CLICKED BACK
   */
  @Test
  public void onUserClickedBack_UpdateStatusNOT_STARTED_emitsEXIT_CANCEL() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = NOT_STARTED;

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    viewModel.onUserClickedBack();

    observer.assertValueCount(2);

    observer.assertValueAt(1, viewState.withUpdateFinished(OTA_ACTION_EXIT_CANCEL));
  }

  @Test
  public void onUserClickedBack_UpdateStatusERROR_emitsEXIT_CANCEL() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = ERROR;

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    viewModel.onUserClickedBack();

    observer.assertValueCount(2);

    observer.assertValueAt(1, viewState.withUpdateFinished(OTA_ACTION_EXIT_CANCEL));
  }

  @Test
  public void onUserClickedBack_UpdateStatusCompleted_emitsEXIT_SUCCESS() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = COMPLETED;

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    viewModel.onUserClickedBack();

    observer.assertValueCount(2);

    observer.assertValueAt(1, viewState.withUpdateFinished(OTA_ACTION_EXIT_SUCCESS));
  }

  @Test
  public void onUserClickedBack_UpdateStatusInProgress_emitsNothing() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = IN_PROGRESS;

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    viewModel.onUserClickedBack();

    observer.assertValueCount(1);
  }

  /*
  ON USER CLICKED ACTION BUTTON
   */
  @Test
  public void onUserClickedActionButton_UpdateStatusNOT_STARTED_invokesOnUserClickedUpgrade() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = NOT_STARTED;

    doNothing().when(viewModel).onUserClickedUpgrade();

    viewModel.onUserClickedActionButton();

    verify(viewModel).onUserClickedUpgrade();
  }

  @Test
  public void onUserClickedActionButton_invokesOnUserClickedBack() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = COMPLETED;

    doNothing().when(viewModel).onUserClickedBack();

    viewModel.onUserClickedActionButton();

    verify(viewModel).onUserClickedBack();
  }

  /*
  checkBlockersSingle
   */
  @Test
  public void checkBlockersSingle_notCharging_blocked() {
    initNoMandatoryUpdate();
    mockViewModelConnection();

    CheckOtaUpdatePrerequisitesUseCase mockCheckUseCase =
        mock(CheckOtaUpdatePrerequisitesUseCase.class);
    viewModel.checkOtaUpdatePrerequisitesUseCase = mockCheckUseCase;

    List<OtaUpdateBlocker> otaBlockers = new ArrayList<>();
    otaBlockers.add(OtaUpdateBlocker.NOT_CHARGING);
    doReturn(Single.just(otaBlockers)).when(mockCheckUseCase).otaUpdateBlockersOnce(any());

    viewModel.checkBlockersSingle(any()).test().assertValue(true);
  }

  @Test
  public void checkBlockersSingle_charging_notBlocked() {
    initNoMandatoryUpdate();
    mockViewModelConnection();

    CheckOtaUpdatePrerequisitesUseCase mockCheckUseCase =
        mock(CheckOtaUpdatePrerequisitesUseCase.class);
    viewModel.checkOtaUpdatePrerequisitesUseCase = mockCheckUseCase;

    doReturn(Single.just(Collections.EMPTY_LIST))
        .when(mockCheckUseCase)
        .otaUpdateBlockersOnce(any());

    viewModel.checkBlockersSingle(any()).test().assertValue(false);
  }

  /*
  onUserClickedUpgrade
   */

  @Test
  public void onUserClickedUpgrade_shouldStartUpdateFalse_doesNothing() {
    initNoMandatoryUpdate();

    doReturn(false).when(viewModel).shouldStartUpdate();

    viewModel.onUserClickedUpgrade();

    verify(viewModel, never()).upgradeToothbrushOnConnectionAvailableObservable();
  }

  @Test
  public void onUserClickedUpgrade_upgradeToothbrushObservableError_invokesPublishError() {
    initNoMandatoryUpdate();

    Exception exception = new TestForcedException();
    doReturn(Observable.error(exception))
        .when(viewModel)
        .upgradeToothbrushOnConnectionAvailableObservable();

    doNothing().when(viewModel).publishError(any(Throwable.class));

    doReturn(true).when(viewModel).shouldStartUpdate();
    doReturn(Single.just(false)).when(viewModel).checkBlockersSingle(any());

    viewModel.onUserClickedUpgrade();

    verify(viewModel).publishError(exception);
  }

  @Test
  public void onUserClickedUpgrade_upgradeToothbrushObservableEmits_invokesPublishUpdateEvent() {
    initNoMandatoryUpdate();

    OtaUpdateEvent expectedEvent = fromError(R.string.ota_low_battery_subtitle);

    doReturn(Observable.just(expectedEvent))
        .when(viewModel)
        .upgradeToothbrushOnConnectionAvailableObservable();

    doNothing().when(viewModel).publishUpdateEvent(any(OtaUpdateEvent.class));
    doNothing().when(viewModel).publishSuccessfulCompletion();

    doReturn(true).when(viewModel).shouldStartUpdate();
    doReturn(Single.just(false)).when(viewModel).checkBlockersSingle(any());

    viewModel.onUserClickedUpgrade();

    verify(viewModel).publishUpdateEvent(expectedEvent);
  }

  /*
  upgradeToothbrushOnConnectionAvailableObservable
   */

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_setsWaitingForConnectionToTrueOnSubscription() {
    initNoMandatoryUpdate();

    assertFalse(viewModel.isWaitingForConnection);

    viewModel.upgradeToothbrushOnConnectionAvailableObservable().test();

    assertTrue(viewModel.isWaitingForConnection);
  }

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_connectionSubjectError_setsWaitingForConnectionFalse() {
    initNoMandatoryUpdate();

    assertFalse(viewModel.isWaitingForConnection);

    viewModel.upgradeToothbrushOnConnectionAvailableObservable().test();

    assertTrue(viewModel.isWaitingForConnection);

    viewModel.connectionSubject.onError(new TestForcedException());

    assertFalse(viewModel.isWaitingForConnection);
  }

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_connectionSubjectComplete_setsWaitingForConnectionToFalse() {
    initNoMandatoryUpdate();

    viewModel.upgradeToothbrushOnConnectionAvailableObservable().test();

    assertTrue(viewModel.isWaitingForConnection);

    viewModel.connectionSubject.onComplete();

    assertFalse(viewModel.isWaitingForConnection);
  }

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_withoutConnectionAvailable_subscribesToCheckPrerequisiteObservable_AfterConnectionIsAvailable() {
    initNoMandatoryUpdate();

    String fwPath = "dasdas";
    GruwareData availableUpdate = mockGruwareData(fwPath, "");
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withOTAAvailable(availableUpdate).build();

    PublishSubject<OtaUpdateEvent> checkPrerequisiteSubject = PublishSubject.create();
    doReturn(checkPrerequisiteSubject).when(viewModel).checkPrerequisiteAndStartObservable();

    viewModel.upgradeToothbrushOnConnectionAvailableObservable().test();

    assertFalse(checkPrerequisiteSubject.hasObservers());

    emitConnection(connection);

    assertTrue(viewModel.connection == connection);
    assertTrue(checkPrerequisiteSubject.hasObservers());
  }

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_withConnectionAvailable_subscribesToCheckPrerequisiteAndStartObservable() {
    initNoMandatoryUpdate();

    String fwPath = "dasdas";
    GruwareData availableUpdate = mockGruwareData(fwPath, "");
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withOTAAvailable(availableUpdate).build();
    emitConnection(connection);

    PublishSubject<OtaUpdateEvent> checkPrerequisiteSubject = PublishSubject.create();
    doReturn(checkPrerequisiteSubject).when(viewModel).checkPrerequisiteAndStartObservable();

    viewModel.upgradeToothbrushOnConnectionAvailableObservable().test();

    assertTrue(viewModel.connection == connection);
    assertTrue(checkPrerequisiteSubject.hasObservers());
  }

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_emitsCheckPrerequisiteAndStartObservableEvents() {
    initNoMandatoryUpdate();

    String fwPath = "dasdas";
    GruwareData availableUpdate = mockGruwareData(fwPath, "");
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withOTAAvailable(availableUpdate).build();
    emitConnection(connection);

    PublishSubject<OtaUpdateEvent> checkPrerequisiteSubject = PublishSubject.create();
    doReturn(checkPrerequisiteSubject).when(viewModel).checkPrerequisiteAndStartObservable();

    doNothing().when(viewModel).publishUndefinedProgress(anyInt());

    TestObserver<OtaUpdateEvent> observer =
        viewModel.upgradeToothbrushOnConnectionAvailableObservable().test().assertNotComplete();

    OtaUpdateEvent expectedEvent = fromAction(OTA_UPDATE_COMPLETED);
    checkPrerequisiteSubject.onNext(expectedEvent);

    observer.assertValue(expectedEvent).assertNotComplete();
  }

  @Test
  public void
      upgradeToothbrushOnConnectionAvailableObservable_CheckPrerequisiteAndStartObservableCompletes_observableCompletes() {
    initNoMandatoryUpdate();

    String fwPath = "dasdas";
    GruwareData availableUpdate = mockGruwareData(fwPath, "");
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withOTAAvailable(availableUpdate).build();
    emitConnection(connection);

    PublishSubject<OtaUpdateEvent> checkPrerequisiteSubject = PublishSubject.create();
    doReturn(checkPrerequisiteSubject).when(viewModel).checkPrerequisiteAndStartObservable();

    doNothing().when(viewModel).publishUndefinedProgress(anyInt());

    TestObserver<OtaUpdateEvent> observer =
        viewModel.upgradeToothbrushOnConnectionAvailableObservable().test().assertNotComplete();

    assertTrue(checkPrerequisiteSubject.hasObservers());

    checkPrerequisiteSubject.onComplete();

    observer.assertComplete();
  }

  /*
  checkPrerequisiteAndStartObservable
   */
  @Test
  public void checkPrerequisiteAndStartObservable_publishBlockedEvent_whenBlocked() {
    initNoMandatoryUpdate();
    doReturn(Single.just(true)).when(viewModel).checkBlockersSingle(any());

    viewModel
        .checkPrerequisiteAndStartObservable()
        .test()
        .assertValueAt(
            0, otaUpdateEvent -> otaUpdateEvent.action() == OTA_UPDATE_BLOCKED_NOT_CHARGING);

    verify(viewModel).publishCheckingPrerequisite();
    verify(viewModel).publishCheckPrerequisiteComplete();
  }

  @Test
  public void
      checkPrerequisiteAndStartObservable_subscribeToCheckGruwareBatteryObservable_whenNotBlocked() {
    initNoMandatoryUpdate();
    doReturn(Single.just(false)).when(viewModel).checkBlockersSingle(any());

    PublishSubject<Object> checkGruwareBatterySubject = PublishSubject.create();
    doReturn(checkGruwareBatterySubject).when(viewModel).checkGruwareAndBatteryAndStartObservable();

    viewModel.checkPrerequisiteAndStartObservable().test();

    verify(viewModel).publishCheckingPrerequisite();
    verify(viewModel).publishCheckPrerequisiteComplete();
    assertTrue(checkGruwareBatterySubject.test().hasSubscription());
  }

  @Test
  public void checkPrerequisiteAndStartObservable_publishCheckingPrerequisiteComplete_whenError() {
    initNoMandatoryUpdate();
    doReturn(Single.error(new Exception())).when(viewModel).checkBlockersSingle(any());

    viewModel.checkPrerequisiteAndStartObservable().test();

    verify(viewModel).publishCheckingPrerequisite();
    verify(viewModel).publishCheckPrerequisiteComplete();
  }

  /*
  checkGruwareAndBatteryAndStartObservable
   */
  @Test
  public void
      checkGruwareAndBatteryAndStartObservable_emitsOtaUpdateEventWithError_IfGruwareDataIsnull() {
    initNoMandatoryUpdate();

    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().build();
    viewModel.connection = connection;

    viewModel
        .checkGruwareAndBatteryAndStartObservable()
        .test()
        .assertValue(fromError(R.string.firmware_upgrade_ota_not_available));
  }

  @Test
  public void checkGruwareAndBatteryAndStartObservable_storesGruwareDataOnSubscription() {
    initNoMandatoryUpdate();

    GruwareData gruwareData = mockGruwareData("", "");
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withOTAAvailable(gruwareData).build();
    viewModel.connection = connection;

    Observable<OtaUpdateEvent> observable = viewModel.checkGruwareAndBatteryAndStartObservable();

    assertNull(viewModel.gruwareData);

    //noinspection ResultOfMethodCallIgnored
    observable.test();

    assertEquals(gruwareData, viewModel.gruwareData);
  }

  @Test
  public void
      checkGruwareAndBatteryAndStartObservable_toothbrushIsInBootloader_subscribesToUpdateToothbrushObservable() {
    initNoMandatoryUpdate();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withOTAAvailable(mockGruwareData("", ""))
            .withBootloader(true)
            .build();
    viewModel.connection = connection;

    PublishSubject<OtaUpdateEvent> updateToothbrushSubject = PublishSubject.create();
    doReturn(updateToothbrushSubject).when(viewModel).updateToothbrushObservable();

    viewModel.checkGruwareAndBatteryAndStartObservable().test().assertNoValues();

    assertTrue(updateToothbrushSubject.hasObservers());
  }

  @Test
  public void
      checkGruwareAndBatteryAndStartObservable_toothbrushIsNotInBootloader_subscribesToStartUpdateIfToothbrushHasEnoughBattery() {
    initNoMandatoryUpdate();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withOTAAvailable(mockGruwareData("", ""))
            .withBootloader(false)
            .build();
    viewModel.connection = connection;

    PublishSubject<OtaUpdateEvent> enoughBatterySubject = PublishSubject.create();
    doReturn(enoughBatterySubject).when(viewModel).startUpdateIfToothbrushHasEnoughBattery();

    viewModel.checkGruwareAndBatteryAndStartObservable().test().assertNoValues();

    assertTrue(enoughBatterySubject.hasObservers());
  }

  @Test
  public void
      checkGruwareAndBatteryAndStartObservable_publishUndefinedProgressOnSubscribe_publishSuccessfulCompletionOnComplete() {
    initNoMandatoryUpdate();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withOTAAvailable(mockGruwareData("", ""))
            .withBootloader(true)
            .build();
    viewModel.connection = connection;

    PublishSubject<OtaUpdateEvent> updateToothbrushSubject = PublishSubject.create();
    doReturn(updateToothbrushSubject).when(viewModel).updateToothbrushObservable();

    viewModel.checkGruwareAndBatteryAndStartObservable().test();
    verify(viewModel).publishUndefinedProgress(OTA_ACTION_REBOOTING);

    updateToothbrushSubject.onComplete();
    verify(viewModel).publishSuccessfulCompletion();
  }

  /*
  startUpdateIfToothbrushHasEnoughBattery
   */
  @Test
  public void
      startUpdateIfToothbrushHasEnoughBattery_hasEnoughBatteryEmitsFalse_emitsEventWithError_ota_low_battery_subtitle() {
    initNoMandatoryUpdate();
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().build();
    viewModel.connection = connection;

    doReturn(Single.just(false)).when(viewModel).hasEnoughBattery(connection);

    viewModel
        .startUpdateIfToothbrushHasEnoughBattery()
        .test()
        .assertValue(fromError(R.string.ota_low_battery_subtitle));
  }

  @Test
  public void
      startUpdateIfToothbrushHasEnoughBattery_hasEnoughBatteryEmitsTrue_returnsUpdateToothbrushObservable() {
    initNoMandatoryUpdate();
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().build();
    viewModel.connection = connection;

    doReturn(Single.just(true)).when(viewModel).hasEnoughBattery(connection);

    PublishSubject<OtaUpdateEvent> updateToothbrushSubject = PublishSubject.create();
    doReturn(updateToothbrushSubject).when(viewModel).updateToothbrushObservable();

    viewModel.startUpdateIfToothbrushHasEnoughBattery().test().assertNoValues();

    assertTrue(updateToothbrushSubject.hasObservers());
  }

  /*
  PREVENT KOLIBREE SERVICE STOP
   */

  @Test
  public void preventKolibreeServiceStop_emitsFalse() {
    initNoMandatoryUpdate();

    TestObserver<Boolean> observer = viewModel.canDisconnectFromService().test();

    observer.assertValue(true);

    viewModel.preventKolibreeServiceStop();

    observer.assertValues(true, false);
  }

  /*
  ALLOW KOLIBREE SERVICE STOP
   */

  @Test
  public void allowKolibreeServiceStop_emitsTrue() {
    initNoMandatoryUpdate();

    viewModel.preventKolibreeServiceStop();

    TestObserver<Boolean> observer = viewModel.canDisconnectFromService().test();

    observer.assertValues(false);

    viewModel.allowKolibreeServiceStop();

    observer.assertValues(false, true);
  }

  /*
  UPDATE FIRMWARE OBSERVABLE
   */
  @Test
  public void publishUpdateEvent_OTA_UPDATE_COMPLETED_invokesPublishCompletion() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).publishSuccessfulCompletion();

    OtaUpdateEvent input = fromAction(OTA_UPDATE_COMPLETED);
    mock(KLTBConnection.class);
    viewModel.publishUpdateEvent(input);

    verify(viewModel).publishSuccessfulCompletion();

    verify(viewModel).publishUpdateEvent(any(OtaUpdateEvent.class));

    verifyNoMoreInteractions(viewModel);
  }

  @Test
  public void publishUpdateEvent_OTA_UPDATE_REBOOTING_invokesPublishRebooting() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).publishRebooting();

    OtaUpdateEvent input = fromAction(OTA_UPDATE_REBOOTING);
    viewModel.publishUpdateEvent(input);

    verify(viewModel).publishRebooting();

    verify(viewModel).publishUpdateEvent(any(OtaUpdateEvent.class));

    verifyNoMoreInteractions(viewModel);
  }

  @Test
  public void publishUpdateEvent_OTA_UPDATE_ERROR_invokesPublishRebooting() {
    initNoMandatoryUpdate();

    String expectedString = "dasdsa";
    doReturn(expectedString).when(viewModel).getString(R.string.firmware_upgrade_error);

    doNothing().when(viewModel).publishError(anyString());

    OtaUpdateEvent input = fromAction(OTA_UPDATE_ERROR);
    viewModel.publishUpdateEvent(input);

    verify(viewModel).publishError(expectedString);

    verify(viewModel).getString(anyInt());

    verify(viewModel).publishUpdateEvent(any(OtaUpdateEvent.class));

    verifyNoMoreInteractions(viewModel);
  }

  @Test
  public void publishUpdateEvent_OTA_UPDATE_INSTALLING_invokesPublishProgress() {
    initNoMandatoryUpdate();

    String expectedString = "dasdsa";
    doReturn(expectedString).when(viewModel).getString(R.string.firmware_upgrade_error);

    doNothing().when(viewModel).publishProgress(anyInt());

    int expectedProgress = 5654;
    OtaUpdateEvent input = fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress);
    viewModel.publishUpdateEvent(input);

    verify(viewModel).publishProgress(expectedProgress);

    verify(viewModel).publishUpdateEvent(any(OtaUpdateEvent.class));

    verifyNoMoreInteractions(viewModel);
  }

  /*
  PUBLISH UNDEFINED PROGRESS
   */
  @Test
  public void publishUndefinedProgress_emitsExpectedViewState() {
    initNoMandatoryUpdate();

    String expectedMessage = "expecdd";
    doReturn(expectedMessage).when(viewModel).getString(R.string.firmware_upgrade_welcome);

    viewModel.viewStateObservable().test().assertValueCount(1);

    int actionId = OTA_UPDATE_REBOOTING;
    viewModel.publishUndefinedProgress(actionId);

    OtaUpdateViewState expectedViewState = viewState.withUndefinedProgress(actionId);

    viewModel.viewStateObservable().test().assertValueCount(2);

    assertLastViewState(expectedViewState);
  }

  /*
  PUBLISH ERROR EXCEPTION
   */

  @Test
  public void publishError_printsStackTrace() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).publishError(anyString());

    String expectedString = "da";
    doReturn(expectedString).when(viewModel).getString(R.string.firmware_upgrade_error);

    Throwable throwable = mock(Throwable.class);
    viewModel.publishError(throwable);

    verify(throwable).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void publishError_invokesPublishErrorWithSimpleMessage() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).allowKolibreeServiceStop();

    doNothing().when(viewModel).publishError(anyString());

    String expectedString = "da";
    doReturn(expectedString).when(viewModel).getString(R.string.firmware_upgrade_error);

    viewModel.publishError(new TestForcedException());

    verify(viewModel).publishError(expectedString);
  }

  @Test
  public void publishError_neverEmitsViewState() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).allowKolibreeServiceStop();

    doNothing().when(viewModel).publishError(anyString());

    String expectedString = "da";
    doReturn(expectedString).when(viewModel).getString(R.string.firmware_upgrade_error);

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    int previousValues = observer.valueCount();

    viewModel.publishError(new TestForcedException());

    observer.assertValueCount(previousValues);
  }

  /*
  PUBLISH ERROR STRING
   */
  @Test
  public void publishError_setsUpdateInProgressToFalse() {
    initNoMandatoryUpdate();

    assertEquals(NOT_STARTED, viewModel.updateStatus);

    viewModel.publishError("das");

    assertEquals(ERROR, viewModel.updateStatus);
  }

  @Test
  public void publishError_invokesAllowDisconnectFromServer() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).allowKolibreeServiceStop();

    viewModel.publishError("das");

    verify(viewModel).allowKolibreeServiceStop();
  }

  @Test
  public void publishError_emitsNewOtaViewState() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).allowKolibreeServiceStop();

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    int previousValues = observer.valueCount();

    String expectedError = "das";
    viewModel.publishError(expectedError);

    observer.assertValueCount(previousValues + 1);

    observer.assertValueAt(previousValues, viewState.withOtaError(expectedError));
  }

  /*
  PUBLISH PROGRESS
   */
  @Test
  public void publishProgress_noMandatoryUpdate_emitsViewStateWithIsCancelButtonDisplayedFalse() {
    initNoMandatoryUpdate();

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    @OtaActionId int expectedOtaId = OTA_ACTION_INSTALLING;
    int expectedPercent = 35;
    viewModel.publishProgress(expectedPercent);

    OtaUpdateViewState expectedViewState = viewState.withProgress(expectedOtaId, expectedPercent);

    observer.assertValueAt(1, expectedViewState);

    assertFalse(expectedViewState.isCancelButtonDisplayed());
  }

  @Test
  public void publishProgress_withMandatoryUpdate_emitsViewStateWithIsCancelButtonDisplayedFalse() {
    initWithMandatoryUpdate();

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    @OtaActionId int expectedOtaId = OTA_ACTION_INSTALLING;
    int expectedPercent = 35;
    viewModel.publishProgress(expectedPercent);

    OtaUpdateViewState expectedViewState = viewState.withProgress(expectedOtaId, expectedPercent);

    observer.assertValueAt(1, expectedViewState);

    assertFalse(expectedViewState.isCancelButtonDisplayed());
  }

  /*
  publishSuccessfulCompletion
   */
  @Test
  public void publishSuccessfulCompletion_setsUpdateStatusToCOMPLETED() {
    initNoMandatoryUpdate();

    viewModel.updateStatus = UpdateStatus.IN_PROGRESS;

    viewModel.publishSuccessfulCompletion();

    assertEquals(UpdateStatus.COMPLETED, viewModel.updateStatus);
  }

  @Test
  public void publishSuccessfulCompletion_invokesSetTagNull() {
    initNoMandatoryUpdate();

    KLTBConnection connection = mockViewModelConnection();
    viewModel.publishSuccessfulCompletion();

    verify(connection).setTag(isNull());
  }

  @Test
  public void publishSuccessfulCompletion_emitsOtaCompleted() {
    initNoMandatoryUpdate();

    assertViewModelInInitialState();

    viewModel.publishSuccessfulCompletion();

    OtaUpdateViewState expectedViewState = viewState.withUpdateCompleted();

    assertLastViewState(expectedViewState);
  }

  @Test
  public void publishSuccessfulCompletion_invokesAllowDisconnectFromServer() {
    initNoMandatoryUpdate();

    doNothing().when(viewModel).allowKolibreeServiceStop();

    viewModel.publishSuccessfulCompletion();

    verify(viewModel).allowKolibreeServiceStop();
  }

  /*
  percentageLevelIsEnoughForOta()
   */

  @Test
  public void percentageLevelIsEnoughForOta_tenOrLessReturnsFalse() throws Exception {
    initNoMandatoryUpdate();
    assertFalse(viewModel.percentageLevelIsEnoughForOta().apply(7));
    assertFalse(viewModel.percentageLevelIsEnoughForOta().apply(10));
  }

  @Test
  public void percentageLevelIsEnoughForOta_elevenOrMoreReturnsTrue() throws Exception {
    initNoMandatoryUpdate();
    assertTrue(viewModel.percentageLevelIsEnoughForOta().apply(11));
    assertTrue(viewModel.percentageLevelIsEnoughForOta().apply(45));
  }

  /*
  discreteLevelIsEnoughForOta()
   */

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_6_MONTHS_returnsTrue() throws Exception {
    initNoMandatoryUpdate();
    assertTrue(
        viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_6_MONTHS));
  }

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_3_MONTHS_returnsTrue() throws Exception {
    initNoMandatoryUpdate();
    assertTrue(
        viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_3_MONTHS));
  }

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_FEW_WEEKS_returnsTrue() throws Exception {
    initNoMandatoryUpdate();
    assertTrue(
        viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_FEW_WEEKS));
  }

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_FEW_DAYS_returnsFalse() throws Exception {
    initNoMandatoryUpdate();
    assertFalse(
        viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_FEW_DAYS));
  }

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_CHANGE_returnsFalse() throws Exception {
    initNoMandatoryUpdate();
    assertFalse(viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_CHANGE));
  }

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_CUT_OFF_returnsFalse() throws Exception {
    initNoMandatoryUpdate();
    assertFalse(
        viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_CUT_OFF));
  }

  @Test
  public void discreteLevelIsEnoughForOta_BATTERY_UNKNOWN_returnsFalse() throws Exception {
    initNoMandatoryUpdate();
    assertFalse(
        viewModel.discreteLevelIsEnoughForOta().apply(DiscreteBatteryLevel.BATTERY_UNKNOWN));
  }

  /*
  hasEnoughBattery()
   */

  @Test
  public void hasEnoughBattery_isBootloader_returnsTrue() {
    initWithMandatoryUpdate();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withBootloader(true).build();

    viewModel.hasEnoughBattery(connection).test().assertValue(true);
  }

  @Test
  public void hasEnoughBattery_usesPercentageLevels_callsPercentageBatteryLevel() {
    initNoMandatoryUpdate();
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withBattery(100).build();
    viewModel.hasEnoughBattery(connection);
    verify(connection.toothbrush().battery()).getBatteryLevel();
    verify(connection.toothbrush().battery(), never()).getDiscreteBatteryLevel();
  }

  @Test
  public void hasEnoughBattery_usesDiscreteLevels_callsDiscreteBatteryLevel() {
    initNoMandatoryUpdate();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withBattery(DiscreteBatteryLevel.BATTERY_FEW_WEEKS)
            .build();
    viewModel.hasEnoughBattery(connection);
    verify(connection.toothbrush().battery()).getDiscreteBatteryLevel();
    verify(connection.toothbrush().battery(), never()).getBatteryLevel();
  }

  /*
  ON USER CLICKED CANCEL
   */
  @Test
  public void onUserConfirmedExit_setsStatusCanceled() {
    initNoMandatoryUpdate();

    assertEquals(UpdateStatus.NOT_STARTED, viewModel.updateStatus);

    viewModel.onUserConfirmedExit();

    assertEquals(UpdateStatus.CANCELED, viewModel.updateStatus);
  }

  @Test
  public void onUserConfirmedExit_emitsExitAndCancel() {
    initNoMandatoryUpdate();

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    viewModel.onUserConfirmedExit();

    observer.assertValueAt(1, viewState.withUpdateFinished(OTA_ACTION_EXIT_CANCEL));
  }

  /*
  ON USER CLICKED CANCEL
   */
  @Test
  public void onUserClickedCancel_emitsConfirmExitViewState() {
    initNoMandatoryUpdate();

    TestObserver<OtaUpdateViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    viewModel.onUserClickedCancel();

    observer.assertValueAt(1, viewState.withConfirmExit());
  }

  /*
  UTILS
   */

  private void initWithMandatoryUpdate() {
    initViewModel(true, ToothbrushModel.CONNECT_E2);
  }

  private void initNoMandatoryUpdate() {
    initViewModel(false, ToothbrushModel.CONNECT_E2);
  }

  private void initWithMandatoryUpdate(ToothbrushModel toothbrushModel) {
    initViewModel(true, toothbrushModel);
  }

  private void initNoMandatoryUpdate(ToothbrushModel toothbrushModel) {
    initViewModel(false, toothbrushModel);
  }

  private InternalKLTBConnection mockViewModelConnection() {
    return mockViewModelConnection(KLTBConnectionBuilder.createAndroidLess().build());
  }

  private InternalKLTBConnection mockViewModelConnection(InternalKLTBConnection connection) {
    viewModel.connection = connection;

    return connection;
  }

  private void initViewModel(boolean isMandatoryUpdate, ToothbrushModel toothbrushModel) {
    viewModel =
        spy(
            new StubOTaUpdateViewModel(
                resources,
                serviceProvider,
                DEFAULT_MAC,
                toothbrushModel,
                isMandatoryUpdate,
                checkOtaUpdatePrerequisitesUseCase,
                otaUpdater));
    viewState = viewModel.viewState;
  }

  private void assertViewModelInInitialState() {
    assertLastViewState(viewState.empty());
  }

  private void assertLastViewState(OtaUpdateViewState expectedViewState) {
    TestObserver<OtaUpdateViewState> observable = viewModel.viewStateObservable().test();

    observable.assertValueAt(observable.valueCount() - 1, expectedViewState);
  }

  private GruwareData mockGruwareData(String fwPath, String gruPath) {
    AvailableUpdate fwAvailableUpdate = new AvailableUpdate("", fwPath, TYPE_FIRMWARE, 0L);
    AvailableUpdate gruAvailableUpdate = new AvailableUpdate("", gruPath, TYPE_GRU, 0L);

    return new GruwareData(
        fwAvailableUpdate,
        gruAvailableUpdate,
        AvailableUpdate.empty(TYPE_BOOTLOADER),
        AvailableUpdate.empty(TYPE_DSP));
  }

  private void emitConnection(KLTBConnection connection) {
    viewModel.connectionSubject.onNext(connection);
    viewModel.connectionSubject.onComplete();
  }

  private static class StubOTaUpdateViewModel extends OtaUpdateViewModel {

    StubOTaUpdateViewModel(
        Resources resources,
        @NonNull ServiceProvider serviceProvider,
        String macAddress,
        ToothbrushModel toothbrushModel,
        boolean mandatoryUpdate,
        CheckOtaUpdatePrerequisitesUseCase checkOtaUpdatePrerequisitesUseCase,
        OtaUpdater otaUpdater) {
      super(
          resources,
          serviceProvider,
          macAddress,
          toothbrushModel,
          mandatoryUpdate,
          checkOtaUpdatePrerequisitesUseCase,
          otaUpdater);
    }

    @Override
    protected Observable<Boolean> canDisconnectFromService() {
      return super.canDisconnectFromService();
    }
  }
}
