/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush.legacy;

import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.MULTI_TOOTHBRUSH_CONNECTED;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.MULTI_TOOTHBRUSH_CONNECTING;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.MULTI_TOOTHBRUSH_DISCONNECTED;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.MULTI_TOOTHBRUSH_OTA_AVAILABLE;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.MULTI_TOOTHBRUSH_OTA_IN_PROGRESS;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.NO_BLUETOOH;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.NO_LOCATION;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.NO_SERVICE;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.NO_TOOTHBRUSHES;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.SINGLE_TOOTHBRUSH_CONNECTED;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.SINGLE_TOOTHBRUSH_CONNECTING;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.SINGLE_TOOTHBRUSH_DISCONNECTED;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.SINGLE_TOOTHBRUSH_OTA_AVAILABLE;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.STATE_NONE;
import static com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel.SYNCING_OFFLINE_BRUSHINGS;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.kolibree.account.utils.ToothbrushesForProfileUseCase;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.app.ui.navigation.MainActivityNavigationController;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiSyncingOfflineBrushing;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushConnected;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushConnecting;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushDisconnected;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushOtaAvailable;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushOtaInProgress;
import com.kolibree.android.app.ui.toolbartoothbrush.NoLocation;
import com.kolibree.android.app.ui.toolbartoothbrush.NoService;
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushes;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleSyncingOfflineBrushing;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaAvailable;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaInProgress;
import com.kolibree.android.app.ui.toolbartoothbrush.Unknown;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.formatter.ToolbarToothbrushFormatter;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.NoBluetooth;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.NoToothbrush;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.homeui.R;
import com.kolibree.android.location.LocationStatus;
import com.kolibree.android.offlinebrushings.sync.LastSyncData;
import com.kolibree.android.offlinebrushings.sync.LastSyncDate;
import com.kolibree.android.offlinebrushings.sync.LastSyncObservable;
import com.kolibree.android.offlinebrushings.sync.NeverSync;
import com.kolibree.android.offlinebrushings.sync.StartSync;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionState;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceDisconnected;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate;
import com.kolibree.sdkws.core.IKolibreeConnector;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mock;

/** Created by miguelaragues on 11/12/17. */
@SuppressWarnings("KotlinInternalInJava")
public class ToolbarToothbrushViewModelTest extends BaseUnitTest {

  @Mock ServiceProvider serviceProvider;

  @Mock IKolibreeConnector connector;

  @Mock ToothbrushRepository toothbrushRepository;

  @Mock IBluetoothUtils bluetoothUtils;

  @Mock MainActivityNavigationController mainActivityNavigatioController;

  @Mock LastSyncObservable lastSyncObservable;

  @Mock ToolbarToothbrushFormatter formatter;

  @Mock LocationStatus locationStatus;

  @Mock Lifecycle lifecycle;

  @Mock ToothbrushesForProfileUseCase toothbrushesForProfileUseCase;

  TestToolbarToothbrushViewModel viewModel;

  @Override
  public void setup() throws Exception {
    super.setup();

    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);

    viewModel =
        spy(
            new TestToolbarToothbrushViewModel(
                serviceProvider,
                connector,
                toothbrushRepository,
                bluetoothUtils,
                mainActivityNavigatioController,
                lastSyncObservable,
                formatter,
                locationStatus,
                lifecycle,
                toothbrushesForProfileUseCase));
  }

  /*
  Constructor
   */
  @Test
  public void toolbarToothbrushViewModel_addsSelfAsLifecycleObserverOnConstruction() {
    // since we are spying, we can't verify the original instance
    verify(lifecycle).addObserver(any(ToolbarToothbrushViewModel.class));
  }

  /*
  onKolibreeServiceConnected_invokesInit
   */

  @Test
  public void onKolibreeServiceConnected_invokesRefreshUI() {
    doNothing().when(viewModel).refreshToothbrushUI();

    verify(viewModel, never()).refreshToothbrushUI();

    viewModel.onKolibreeServiceConnected(mock(KolibreeService.class));

    verify(viewModel).refreshToothbrushUI();
  }

  /*
  ON BLUETOOTH STATUS UPDATED
   */

  @Test
  public void onBluetoothStateUpdated_true_invokesRefreshUI() {
    doNothing().when(viewModel).refreshToothbrushUI();

    verify(viewModel, never()).refreshToothbrushUI();

    viewModel.onBluetoothStateUpdated(true);

    verify(viewModel).refreshToothbrushUI();
  }

  @Test
  public void onBluetoothStateUpdated_false_invokesRefreshUI() {
    doNothing().when(viewModel).refreshToothbrushUI();

    verify(viewModel, never()).refreshToothbrushUI();

    viewModel.onBluetoothStateUpdated(false);

    verify(viewModel).refreshToothbrushUI();
  }

  /*
  ON BLUETOOTH NOT AVAILABLE
   */

  @Test
  public void onBluetoothNotAvailable_invokesUnregisterConnectionStateListenters() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).unregisterConnectionStateListeners();

    viewModel.onBluetoothNotAvailable();

    verify(viewModel).unregisterConnectionStateListeners();
  }

  @Test
  public void onBluetoothNotAvailable_setsStateNO_BLUETOOH() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).unregisterConnectionStateListeners();

    assertNotEquals(NO_BLUETOOH, viewModel.toolbarToothbrushState);

    viewModel.onBluetoothNotAvailable();

    assertEquals(NO_BLUETOOH, viewModel.toolbarToothbrushState);
  }

  @Test
  public void onBluetoothNotAvailable_invokesShowSingleDisconnected() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).unregisterConnectionStateListeners();

    viewModel.onBluetoothNotAvailable();

    verify(viewModel).showSingleDisconnectedToothbrush();
  }

  /*
  VIEW STATE OBSERVABLE
   */
  @Test
  public void viewStateObservable_emitAskForBluetooth_immediatelyEmitsWithACTION_NONE() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.toolbarToothbrushState = NO_BLUETOOH;
    ToolbarToothbrushViewState firstExpectedViewState =
        ToolbarToothbrushViewState.create(
                NoBluetooth.INSTANCE, R.drawable.ic_toothbrush_multi_connected)
            .withActionId(ToolbarToothbrushAction.ACTION_ASK_ENABLE_BLUETOOTH);

    viewModel.emitViewState(firstExpectedViewState);

    observer.assertValueCount(2);

    observer.assertValueAt(
        0,
        viewState ->
            viewState.getDrawable() == R.drawable.ic_toothbrush_multi_connected
                && viewState.isAskingToEnableBluetooth()
                && viewState.getToothbrushState()
                    instanceof com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth);

    observer.assertValueAt(
        1,
        viewState ->
            viewState.getDrawable() == R.drawable.ic_toothbrush_multi_connected
                && viewState.getActionId() == ToolbarToothbrushAction.ACTION_NONE
                && viewState.getToothbrushState()
                    instanceof com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth);
  }

  /*
  ON INITIALIZED WITHOUT BLUETOOTH
   */
  @Test
  public void onInitializedWithoutBluetooth_emitsWithActionAskToEnableBluetooth() {
    viewModel.toolbarToothbrushState = NO_BLUETOOH;
    ToolbarToothbrushViewState initialViewState =
        ToolbarToothbrushViewState.create(
            NoBluetooth.INSTANCE, R.drawable.ic_toothbrush_multi_connected);
    viewModel.emitViewState(initialViewState);

    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertValueCount(1);

    observer.assertValueAt(
        0,
        viewState ->
            viewState.getDrawable() == R.drawable.ic_toothbrush_multi_connected
                && !viewState.isAskingToEnableBluetooth()
                && viewState.getToothbrushState()
                    instanceof com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth);

    viewModel.onInitializedWithoutBluetooth();

    observer.assertValueAt(
        1,
        viewState ->
            viewState.getDrawable() == R.drawable.ic_toothbrush_multi_connected
                && viewState.isAskingToEnableBluetooth()
                && viewState.getToothbrushState()
                    instanceof com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth);
  }

  @Test
  public void onInitializedWithoutBluetooth_storesStateNO_BLUETOOTH() {
    viewModel.viewStateBehaviorRelay.accept(
        ToolbarToothbrushViewState.create(
            NoBluetooth.INSTANCE, R.drawable.ic_toothbrush_multi_connected));

    assertNotEquals(NO_BLUETOOH, viewModel.toolbarToothbrushState);

    viewModel.onInitializedWithoutBluetooth();

    assertEquals(NO_BLUETOOH, viewModel.toolbarToothbrushState);
  }

  /*
  ON INITIALIZED WITH LOCATION NOT READY TO SCAN
   */

  @Test
  public void onInitializedWithLocationNotReadyToScan_storesStateNO_LOCATION() {
    assertNotEquals(NO_LOCATION, viewModel.toolbarToothbrushState);

    viewModel.onLocationNotReadyToScan();

    assertEquals(NO_LOCATION, viewModel.toolbarToothbrushState);
  }

  @Test
  public void onInitializedWithLocationNotReadyToScan_emitsViewState() {
    ToolbarToothbrushViewState initialState =
        ToolbarToothbrushViewState.create(NoToothbrush.INSTANCE);

    viewModel.onLocationNotReadyToScan();

    verify(viewModel).emitViewState(initialState);
  }

  /*
  FETCH UNIQUE CONNECTION
   */

  @Test
  public void fetchUniqueConnection_emptyActiveProfileConnections_returnsNull() {
    assertNull(viewModel.fetchUniqueConnection());
  }

  @Test
  public void fetchUniqueConnection_twoActiveProfileConnections_returnsNull() {
    viewModel.activeProfileConnections.add(mock(KLTBConnection.class));
    viewModel.activeProfileConnections.add(mock(KLTBConnection.class));

    assertNull(viewModel.fetchUniqueConnection());
  }

  @Test
  public void fetchUniqueConnection_singleActiveProfileConnections_returnsElement() {
    KLTBConnection expectedConnection = mock(KLTBConnection.class);
    viewModel.activeProfileConnections.add(expectedConnection);

    assertEquals(expectedConnection, viewModel.fetchUniqueConnection());
  }

  /*
  ON TOOTHBRUSH CLICKED
   */

  @Test
  public void onToothbrushIconClicked_NO_TOOTHBRUSHES_invokesNavigateToSetupToothbrush() {
    viewModel.toolbarToothbrushState = NO_TOOTHBRUSHES;

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToSetupToothbrush();
  }

  @Test
  public void
      onToothbrushIconClicked_SINGLE_TOOTHBRUSH_CONNECTED_fetchUniqueConnectionReturnsNull_neverInvokesNavigateToToothbrush() {
    viewModel.toolbarToothbrushState = SINGLE_TOOTHBRUSH_CONNECTED;

    doReturn(null).when(viewModel).fetchUniqueConnection();

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController, never())
        .navigateToToothbrush(any(KLTBConnection.class));
  }

  @Test
  public void
      onToothbrushIconClicked_SINGLE_TOOTHBRUSH_CONNECTED_fetchUniqueConnectionReturnsConnection_invokesNavigateToToothbrush() {
    viewModel.toolbarToothbrushState = SINGLE_TOOTHBRUSH_CONNECTED;

    KLTBConnection connection = mock(KLTBConnection.class);
    doReturn(connection).when(viewModel).fetchUniqueConnection();

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToToothbrush(eq(connection));
  }

  @Test
  public void
      onToothbrushIconClicked_MULTI_TOOTHBRUSH_DISCONNECTED_invokesNavigateToMyToothbrushes() {
    viewModel.toolbarToothbrushState = MULTI_TOOTHBRUSH_DISCONNECTED;

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToMyToothbrushes();
  }

  @Test
  public void
      onToothbrushIconClicked_MULTI_TOOTHBRUSH_CONNECTING_invokesNavigateToMyToothbrushes() {
    viewModel.toolbarToothbrushState = MULTI_TOOTHBRUSH_CONNECTING;

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToMyToothbrushes();
  }

  @Test
  public void onToothbrushIconClicked_MULTI_TOOTHBRUSH_OTA_invokesNavigateToMyToothbrushes() {
    viewModel.toolbarToothbrushState = MULTI_TOOTHBRUSH_OTA_IN_PROGRESS;

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToMyToothbrushes();
  }

  @Test
  public void onToothbrushIconClicked_MULTI_TOOTHBRUSH_CONNECTED_invokesNavigateToMyToothbrushes() {
    viewModel.toolbarToothbrushState = MULTI_TOOTHBRUSH_CONNECTED;

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToMyToothbrushes();
  }

  @Test
  public void onToothbrushIconClicked_NO_LOCATION_invokesNavigateToGrantLocationPermission() {
    viewModel.toolbarToothbrushState = NO_LOCATION;

    viewModel.onToothbrushIconClicked();

    verify(mainActivityNavigatioController).navigateToGrantLocation();
  }

  /*
  LISTEN TO BLUETOOTH STATE UPDATES
   */

  @Test
  public void listenToBluetoothStateUpdates_newBluetoothState_invokesOnBluetoothStateUpdated() {
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(Observable.just(false));

    doNothing().when(viewModel).showSingleDisconnectedToothbrush();

    verify(viewModel, never()).onBluetoothStateUpdated(anyBoolean());
    doNothing().when(viewModel).onBluetoothStateUpdated(anyBoolean());

    viewModel.listenToBluetoothStateUpdates();

    verify(viewModel).onBluetoothStateUpdated(eq(false));
  }

  @Test
  public void
      listenToBluetoothStateUpdates_newBluetoothStateEmitsSameValueMultipleTimes_invokesOnBluetoothStateUpdatedOnlyOnce() {
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(Observable.just(false, false));

    doNothing().when(viewModel).showSingleDisconnectedToothbrush();

    doNothing().when(viewModel).onBluetoothStateUpdated(anyBoolean());
    verify(viewModel, never()).onBluetoothStateUpdated(anyBoolean());

    viewModel.listenToBluetoothStateUpdates();

    verify(viewModel, times(1)).onBluetoothStateUpdated(eq(false));
  }

  @Test
  public void listenToBluetoothStateUpdates_bluetoothDisposableNotDisposed_doesNothing() {
    Disposable disposable = mock(Disposable.class);
    when(disposable.isDisposed()).thenReturn(false);
    viewModel.bluetoothStateDisposable = disposable;

    verify(bluetoothUtils, never()).bluetoothStateObservable();

    viewModel.listenToBluetoothStateUpdates();

    verify(bluetoothUtils, never()).bluetoothStateObservable();
  }

  /*
  listenToToothbrushConnections
   */

  @Test
  public void
      listenToToothbrushConnections_toothbrushesDisposableNull_subscribesToToothbrushesForProfileUseCase() {
    PublishProcessor<List<KLTBConnection>> subject = PublishProcessor.create();
    when(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
        .thenReturn(subject);

    assertNull(viewModel.toothbrushesDisposable);

    viewModel.listenToToothbrushConnections();

    assertTrue(subject.hasSubscribers());
  }

  @Test
  public void
      listenToToothbrushConnections_toothbrushesDisposableDisposed_subscribesToToothbrushesForProfileUseCase() {
    PublishProcessor<List<KLTBConnection>> subject = PublishProcessor.create();
    when(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
        .thenReturn(subject);

    viewModel.toothbrushesDisposable = mock(Disposable.class);
    when(viewModel.toothbrushesDisposable.isDisposed()).thenReturn(true);

    viewModel.listenToToothbrushConnections();

    assertTrue(subject.hasSubscribers());
  }

  @Test
  public void
      listenToToothbrushConnections_toothbrushesDisposableNotDisposed_neverSubscribesToToothbrushesForProfileUseCase() {
    PublishProcessor<List<KLTBConnection>> subject = PublishProcessor.create();
    when(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
        .thenReturn(subject);

    viewModel.toothbrushesDisposable = mock(Disposable.class);
    when(viewModel.toothbrushesDisposable.isDisposed()).thenReturn(false);

    viewModel.listenToToothbrushConnections();

    assertFalse(subject.hasSubscribers());
  }

  @Test
  public void
      listenToToothbrushConnections_toothbrushesForProfileUseCaseEmitsItem_invokesOnToothbrushesUpdated() {
    PublishProcessor<List<KLTBConnection>> subject = PublishProcessor.create();
    when(toothbrushesForProfileUseCase.currentProfileToothbrushesOnceAndStream())
        .thenReturn(subject);

    doNothing().when(viewModel).onToothbrushesUpdated(anyList());

    viewModel.listenToToothbrushConnections();

    List<KLTBConnection> expectedList = Collections.emptyList();
    subject.onNext(expectedList);

    verify(viewModel).onToothbrushesUpdated(expectedList);
  }

  /*
  onToothbrushesUpdated
   */

  @Test
  public void onToothbrushesUpdated_addsConnectionsToActiveProfileConnections() {
    List<KLTBConnection> list = new ArrayList<>();
    KLTBConnection connection1 = mock(KLTBConnection.class);
    KLTBConnection connection2 = mock(KLTBConnection.class);
    list.add(connection1);
    list.add(connection2);

    assertTrue(viewModel.activeProfileConnections.isEmpty());

    doNothing().when(viewModel).registerAsStateListener(any(KLTBConnection.class));
    doNothing().when(viewModel).refreshToothbrushUI();

    viewModel.onToothbrushesUpdated(list);

    assertEquals(2, viewModel.activeProfileConnections.size());
    assertTrue(viewModel.activeProfileConnections.contains(connection1));
    assertTrue(viewModel.activeProfileConnections.contains(connection2));
  }

  @Test
  public void onToothbrushesUpdated_invokesRegisterAsStateListenerOnEachConnection() {
    List<KLTBConnection> list = new ArrayList<>();
    KLTBConnection connection1 = mock(KLTBConnection.class);
    KLTBConnection connection2 = mock(KLTBConnection.class);
    list.add(connection1);
    list.add(connection2);

    doNothing().when(viewModel).registerAsStateListener(any(KLTBConnection.class));
    doNothing().when(viewModel).refreshToothbrushUI();

    viewModel.onToothbrushesUpdated(list);

    verify(viewModel).registerAsStateListener(connection1);
    verify(viewModel).registerAsStateListener(connection2);
  }

  @Test
  public void onToothbrushesUpdated_invokesRefreshToothbrushUIOnce() {
    List<KLTBConnection> list = new ArrayList<>();

    doNothing().when(viewModel).registerAsStateListener(any(KLTBConnection.class));
    doNothing().when(viewModel).refreshToothbrushUI();

    viewModel.onToothbrushesUpdated(list);

    verify(viewModel).refreshToothbrushUI();
  }

  @Test
  public void onToothbrushesUpdated_replacesPreviousConnectionsToActiveProfileConnections() {
    List<KLTBConnection> list = new ArrayList<>();
    KLTBConnection connection1 = mock(KLTBConnection.class);
    KLTBConnection connection2 = mock(KLTBConnection.class);
    list.add(connection1);
    list.add(connection2);

    KLTBConnection previousConnection = mock(KLTBConnection.class);
    viewModel.activeProfileConnections.add(previousConnection);

    doNothing().when(viewModel).registerAsStateListener(any(KLTBConnection.class));
    doNothing().when(viewModel).refreshToothbrushUI();

    viewModel.onToothbrushesUpdated(list);

    assertEquals(2, viewModel.activeProfileConnections.size());
    assertTrue(viewModel.activeProfileConnections.contains(connection1));
    assertTrue(viewModel.activeProfileConnections.contains(connection2));
    assertFalse(viewModel.activeProfileConnections.contains(previousConnection));
  }

  /*
  LISTEN TO OTA UPDATES
   */
  @Test
  public void listenToOTAUpdates_shouldListenToConnectionOTAFalse_neverInvokesHasOTAObservable() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    doReturn(false).when(viewModel).shouldListenToConnectionOta(eq(connection));

    viewModel.listenToOtaUpdates(connection);

    verify(connection, never()).hasOTAObservable();
  }

  @Test
  public void listenToOTAUpdates_shouldListenToConnectionOTATrue_subscribesToHasOTAObservable() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    PublishSubject<Boolean> subject = PublishSubject.create();
    when(connection.hasOTAObservable()).thenReturn(subject.hide());

    doReturn(true).when(viewModel).shouldListenToConnectionOta(eq(connection));

    viewModel.listenToOtaUpdates(connection);

    assertTrue(subject.hasObservers());
  }

  @Test
  public void listenToOTAUpdates_shouldListenToConnectionOTATrue_storesMac() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    PublishSubject<Boolean> subject = PublishSubject.create();
    when(connection.hasOTAObservable()).thenReturn(subject.hide());

    doReturn(true).when(viewModel).shouldListenToConnectionOta(eq(connection));

    assertTrue(viewModel.macsBeingListenedForOta.isEmpty());

    viewModel.listenToOtaUpdates(connection);

    assertEquals(KLTBConnectionBuilder.DEFAULT_MAC, viewModel.macsBeingListenedForOta.get(0));
  }

  @Test
  public void listenToOTAUpdates_newValue_invokesRefreshToothbrushList() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    PublishSubject<Boolean> subject = PublishSubject.create();
    when(connection.hasOTAObservable()).thenReturn(subject.hide());

    doReturn(true).when(viewModel).shouldListenToConnectionOta(eq(connection));

    viewModel.listenToOtaUpdates(connection);

    doNothing().when(viewModel).refreshToothbrushUI();
    verify(viewModel, never()).refreshToothbrushUI();

    subject.onNext(true);

    verify(viewModel).refreshToothbrushUI();
  }

  @Test
  public void listenToOTAUpdates_error_removesMacFromList() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    PublishSubject<Boolean> subject = PublishSubject.create();
    when(connection.hasOTAObservable()).thenReturn(subject.hide());

    doReturn(true).when(viewModel).shouldListenToConnectionOta(eq(connection));

    viewModel.listenToOtaUpdates(connection);

    assertFalse(viewModel.macsBeingListenedForOta.isEmpty());

    subject.onError(new Throwable("Test forced error"));

    assertTrue(viewModel.macsBeingListenedForOta.isEmpty());
  }

  /*
  SHOULD LISTEN TO CONNECTION OTA
   */

  @Test
  public void shouldListenToConnectionOTA_null_returnsFalse() {
    assertFalse(viewModel.shouldListenToConnectionOta(null));
  }

  @Test
  public void shouldListenToConnectionOTA_connectionNotInOtaObservableList_returnsTrue() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    assertTrue(viewModel.shouldListenToConnectionOta(connection));
  }

  @Test
  public void shouldListenToConnectionOTA_connectionInOtaObservableList_returnsFalse() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().withDefaultMac().build();

    PublishSubject<Boolean> subject = PublishSubject.create();
    when(connection.hasOTAObservable()).thenReturn(subject.hide());

    viewModel.macsBeingListenedForOta.add(KLTBConnectionBuilder.DEFAULT_MAC);

    assertFalse(viewModel.shouldListenToConnectionOta(connection));
  }

  /*
  onKolibreeServiceNotAvailable
   */

  @Test
  public void onKolibreeServiceNotAvailable_setsToothbrushStateNO_SERVICE() {
    assertNotEquals(NO_SERVICE, viewModel.toolbarToothbrushState);

    doNothing().when(viewModel).showSingleDisconnectedToothbrush();

    viewModel.onKolibreeServiceNotAvailable();

    assertEquals(NO_SERVICE, viewModel.toolbarToothbrushState);
  }

  @Test
  public void onKolibreeServiceNotAvailable_invokesShowSingleDisconnectedToothbrush() {
    assertNotEquals(NO_SERVICE, viewModel.toolbarToothbrushState);

    doNothing().when(viewModel).showSingleDisconnectedToothbrush();

    viewModel.onKolibreeServiceNotAvailable();

    verify(viewModel).showSingleDisconnectedToothbrush();
  }

  /*
  onZeroToothbrushes
   */

  @Test
  public void onZeroToothbrushes_setsToothbrushStateNO_TOOTHBRUSHES() {
    assertNotEquals(NO_TOOTHBRUSHES, viewModel.toolbarToothbrushState);

    doNothing().when(viewModel).showSingleDisconnectedToothbrush();

    viewModel.onZeroToothbrushes();

    assertEquals(NO_TOOTHBRUSHES, viewModel.toolbarToothbrushState);
  }

  @Test
  public void onZeroToothbrushes_invokesShowSingleDisconnectedToothbrush() {
    assertNotEquals(NO_SERVICE, viewModel.toolbarToothbrushState);

    doNothing().when(viewModel).showSingleDisconnectedToothbrush();

    viewModel.onZeroToothbrushes();

    verify(viewModel).showSingleDisconnectedToothbrush();
  }

  /*
  refreshToothbrushUI
   */

  @Test
  public void refreshToothbrushUI_noBluetooth_invokesOnBluetoothNotAvailable() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);

    doNothing().when(viewModel).onBluetoothNotAvailable();

    viewModel.refreshToothbrushUI();

    verify(viewModel).onBluetoothNotAvailable();
  }

  @Test
  public void refreshToothbrushUI_noService_invokesOnKolibreeServiceNotAvailable() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    doNothing().when(viewModel).onKolibreeServiceNotAvailable();

    viewModel.refreshToothbrushUI();

    verify(viewModel).onKolibreeServiceNotAvailable();
  }

  @Test
  public void refreshToothbrushUI_zeroToothbrushes_invokesOnZeroToothbrushes() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);
    viewModel.setService(mock(KolibreeService.class));

    doNothing().when(viewModel).onZeroToothbrushes();

    viewModel.activeProfileConnections.addAll(Collections.emptyList());

    viewModel.refreshToothbrushUI();

    verify(viewModel).onZeroToothbrushes();
  }

  @Test
  public void refreshToothbrushUI_oneToothbrush_invokesOnSingleToothbrushScenario() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);
    viewModel.setService(mock(KolibreeService.class));

    doNothing().when(viewModel).onSingleToothbrush(any(KLTBConnection.class));

    KLTBConnection toothbrush = mock(KLTBConnection.class);

    viewModel.activeProfileConnections.addAll(Collections.singletonList(toothbrush));

    viewModel.refreshToothbrushUI();

    verify(viewModel).onSingleToothbrush(eq(toothbrush));
  }

  @Test
  public void refreshToothbrushUI_multipleToothbrushes_invokesOnMultiToothbrushScenario() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);
    viewModel.setService(mock(KolibreeService.class));

    doNothing().when(viewModel).onMultiToothbrush(anyListOf(KLTBConnection.class));

    List<KLTBConnection> expectedList =
        Arrays.asList(mock(KLTBConnection.class), mock(KLTBConnection.class));

    viewModel.activeProfileConnections.addAll(expectedList);

    viewModel.refreshToothbrushUI();

    verify(viewModel).onMultiToothbrush(eq(expectedList));
  }

  /*
  refreshLastSyncData
   */
  @Test
  public void refreshLastSyncData_unknownToothbrush_doesNothing() {
    assertTrue(viewModel.activeProfileConnections.isEmpty());

    viewModel.refreshLastSyncData(new StartSync("da"));

    verify(viewModel, never()).showSyncingToothbrush();
    verify(viewModel, never()).refreshToothbrushUI();
  }

  @Test
  public void refreshLastSyncData_knownToothbrush_StartSync_setsStateSYNCING_OFFLINE_BRUSHINGS() {
    @NotNull String mac = "da";

    viewModel.activeProfileConnections.add(
        KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());

    assertNotEquals(SYNCING_OFFLINE_BRUSHINGS, viewModel.toolbarToothbrushState);
    doNothing().when(viewModel).showSyncingToothbrush();

    viewModel.refreshLastSyncData(new StartSync(mac));

    assertEquals(SYNCING_OFFLINE_BRUSHINGS, viewModel.toolbarToothbrushState);
  }

  @Test
  public void refreshLastSyncData_knownToothbrush_StartSync_invokesShowSyncingToothbrush() {
    @NotNull String mac = "da";

    viewModel.activeProfileConnections.add(
        KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());

    doNothing().when(viewModel).showSyncingToothbrush();

    viewModel.refreshLastSyncData(new StartSync(mac));

    verify(viewModel).showSyncingToothbrush();
  }

  @Test
  public void refreshLastSyncData_knownToothbrush_NeverSync_invokesRefreshToothbrushUI() {
    @NotNull String mac = "da";

    viewModel.activeProfileConnections.add(
        KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());

    doNothing().when(viewModel).refreshToothbrushUI();
    when(formatter.format(any())).thenReturn("");

    viewModel.refreshLastSyncData(new NeverSync(mac));

    verify(viewModel).refreshToothbrushUI();
  }

  @Test
  public void refreshLastSyncData_knownToothbrush_LastSyncDate_invokesRefreshToothbrushUI() {
    @NotNull String mac = "da";

    viewModel.activeProfileConnections.add(
        KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());

    doNothing().when(viewModel).refreshToothbrushUI();
    when(formatter.format(any())).thenReturn("");

    viewModel.refreshLastSyncData(LastSyncDate.now(mac));

    verify(viewModel).refreshToothbrushUI();
  }

  @Test
  public void refreshLastSyncData_knownToothbrush_LastSyncDate_emitsViewStateWithLastSyncText() {
    @NotNull String mac = "da";

    viewModel.activeProfileConnections.add(
        KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());

    doNothing().when(viewModel).refreshToothbrushUI();

    LastSyncData lastSyncData = LastSyncDate.now(mac);

    String expectedText = "dadasdasda";
    when(formatter.format(lastSyncData)).thenReturn(expectedText);

    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    ToolbarToothbrushViewState originalViewState = viewModel.viewState;

    viewModel.refreshLastSyncData(lastSyncData);

    observer.assertValue(originalViewState.withLastSyncText(expectedText));
  }

  @Test
  public void refreshLastSyncData_knownToothbrush_NeverSync_emitsViewStateWithLastSyncText() {
    @NotNull String mac = "da";

    viewModel.activeProfileConnections.add(
        KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());

    doNothing().when(viewModel).refreshToothbrushUI();

    LastSyncData neverSync = new NeverSync(mac);

    String expectedText = "dadasdasda";
    when(formatter.format(neverSync)).thenReturn(expectedText);

    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    ToolbarToothbrushViewState originalViewState = viewModel.viewState;

    viewModel.refreshLastSyncData(neverSync);

    observer.assertValue(originalViewState.withLastSyncText(expectedText));
  }

  /*
  IS DISCONNECTED CONNECTION
   */
  @Test
  public void isDisconnected_null_returnsTrue() {
    assertTrue(viewModel.isDisconnected(null));
  }

  @Test
  public void isDisconnected_TERMINATED_returnsTrue() {
    assertTrue(
        viewModel.isDisconnected(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.TERMINATED)
                .build()));
  }

  @Test
  public void isDisconnected_TERMINATING_returnsTrue() {
    assertTrue(
        viewModel.isDisconnected(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.TERMINATING)
                .build()));
  }

  @Test
  public void isDisconnected_NEW_returnsFalse() {
    assertFalse(
        viewModel.isDisconnected(
            KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.NEW).build()));
  }

  @Test
  public void isDisconnected_ESTABLISHING_returnsFalse() {
    assertFalse(
        viewModel.isDisconnected(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ESTABLISHING)
                .build()));
  }

  @Test
  public void isDisconnected_ACTIVE_returnsFalse() {
    assertFalse(
        viewModel.isDisconnected(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.ACTIVE)
                .build()));
  }

  @Test
  public void isDisconnected_OTA_returnsFalse() {
    assertFalse(
        viewModel.isDisconnected(
            KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build()));
  }

  /*
  ON SINGLE TOOTHBRUSH
   */

  @Test
  public void
      onSingleToothbrush_isDisconnectedConnectionTrue_invokesShowSingleDisconnectedToothbrush() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection = mock(KLTBConnection.class);
    doReturn(true).when(viewModel).isDisconnected(eq(connection));

    viewModel.onSingleToothbrush(connection);

    verify(viewModel).showSingleDisconnectedToothbrush();
  }

  @Test
  public void
      onSingleToothbrush_isDisconnectedConnectionTrue_setsStateSINGLE_TOOTHBRUSH_DISCONNECTED() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection = mock(KLTBConnection.class);
    doReturn(true).when(viewModel).isDisconnected(eq(connection));

    assertNotEquals(SINGLE_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);

    viewModel.onSingleToothbrush(connection);

    assertEquals(SINGLE_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);
  }

  @Test
  public void
      onSingleToothbrush_isDisconnectedFalse_hasOTAUpdateAvailableTrue_invokesShowSingleOTAToothbrush() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();
    doReturn(false).when(viewModel).isDisconnected(eq(connection));

    doReturn(true).when(viewModel).hasOtaUpdateAvailable(eq(connection));

    viewModel.onSingleToothbrush(connection);

    verify(viewModel).showSingleToothbrushOtaAvailable();
  }

  @Test
  public void
      onSingleToothbrush_isDisconnectedFalse_hasOTAUpdateAvailableTrue_setsStateSINGLE_TOOTHBRUSH_OTA_AVAILABLE() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();
    doReturn(false).when(viewModel).isDisconnected(eq(connection));

    doReturn(true).when(viewModel).hasOtaUpdateAvailable(eq(connection));

    assertNotEquals(SINGLE_TOOTHBRUSH_OTA_AVAILABLE, viewModel.toolbarToothbrushState);

    viewModel.onSingleToothbrush(connection);

    assertEquals(SINGLE_TOOTHBRUSH_OTA_AVAILABLE, viewModel.toolbarToothbrushState);
  }

  @Test
  public void
      onSingleToothbrush_isDisconnectedFalse_hasOTAUpdateAvailableFalse_invokesManageSingleToothbrushConnection() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));
    viewModel.activeProfileConnections.add(KLTBConnectionBuilder.createAndroidLess().build());

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();
    doReturn(false).when(viewModel).isDisconnected(eq(connection));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(eq(connection));

    viewModel.onSingleToothbrush(connection);

    verify(viewModel).manageSingleConnectionState(eq(connection));
  }

  /*
  HAS OTA UPDATE AVAILABLE
   */

  @Test
  public void hasOTAUpdateAvailable_stateNotActive_returnsFalse() {
    KLTBConnection connection = KLTBConnectionBuilder.createAndroidLess().build();
    for (KLTBConnectionState state : KLTBConnectionState.values()) {
      if (state == KLTBConnectionState.ACTIVE) continue;

      when(connection.state().getCurrent()).thenReturn(state);

      assertFalse(viewModel.hasOtaUpdateAvailable(connection));
    }

    verify(connection, never()).getTag();
  }

  @Test
  public void hasOTAUpdateAvailable_stateActive_tagNull_returnsFalse() {
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    doNothing().when(viewModel).listenToOtaUpdates(eq(connection));

    assertFalse(viewModel.hasOtaUpdateAvailable(connection));
  }

  @Test
  public void hasOTAUpdateAvailable_stateActive_tagNotNull_returnsTrue() {
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();
    when(connection.getTag()).thenReturn(new Object());

    doNothing().when(viewModel).listenToOtaUpdates(eq(connection));

    assertTrue(viewModel.hasOtaUpdateAvailable(connection));
  }

  @Test
  public void hasOTAUpdateAvailable_stateActive_invokesListenToOTAUpdates() {
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    doNothing().when(viewModel).listenToOtaUpdates(eq(connection));

    viewModel.hasOtaUpdateAvailable(connection);

    verify(viewModel).listenToOtaUpdates(eq(connection));
  }

  /*
  ON SINGLE CONNECTING TOOTHBRUSH
   */

  @Test
  public void onSingleConnectingToothbrush_locationNotReady_invokesOnLocationNotReady() {
    when(locationStatus.isReadyToScan()).thenReturn(false);

    doNothing().when(viewModel).onLocationNotReadyToScan();

    viewModel.onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_CONNECTING);

    verify(viewModel).onLocationNotReadyToScan();
  }

  @Test
  public void
      onSingleConnectingToothbrush_invokesSetToothbrushStateWithSINGLE_TOOTHBRUSH_CONNECTING() {
    when(locationStatus.isReadyToScan()).thenReturn(true);

    doNothing().when(viewModel).showSingleConnectingToothbrush();

    int expectedState = SINGLE_TOOTHBRUSH_CONNECTING;
    assertNotEquals(expectedState, viewModel.toolbarToothbrushState);

    viewModel.onSingleConnectingToothbrush(expectedState);

    assertEquals(expectedState, viewModel.toolbarToothbrushState);
  }

  @Test
  public void onSingleConnectingToothbrush_invokesShowSingleConnectingToothbrush() {
    when(locationStatus.isReadyToScan()).thenReturn(true);

    doNothing().when(viewModel).showSingleConnectingToothbrush();

    viewModel.onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_CONNECTING);

    verify(viewModel).showSingleConnectingToothbrush();
  }

  /*
  MANAGE SINGLE CONNECTION STATE
   */

  @Test
  public void
      manageSingleConnectionState_connectionStateESTABLISHING_invokesOnSingleConnectingToothbrush() {
    doNothing().when(viewModel).showSingleConnectingToothbrush();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build();

    doNothing().when(viewModel).onSingleConnectingToothbrush(anyInt());

    viewModel.manageSingleConnectionState(connection);

    verify(viewModel).onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_CONNECTING);
  }

  @Test
  public void manageSingleConnectionState_connectionStateNEW_invokesOnSingleConnectingToothbrush() {
    doNothing().when(viewModel).showSingleConnectingToothbrush();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.NEW).build();

    doNothing().when(viewModel).onSingleConnectingToothbrush(anyInt());

    viewModel.manageSingleConnectionState(connection);

    verify(viewModel).onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_CONNECTING);
  }

  @Test
  public void manageSingleConnectionState_connectionStateOTA_invokesOnSingleConnectingToothbrush() {
    doNothing().when(viewModel).showSingleConnectingToothbrush();

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build();

    viewModel.manageSingleConnectionState(connection);

    verify(viewModel).onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS);
  }

  @Test
  public void
      manageSingleConnectionState_connectionStateACTIVE_invokesShowSingleConnectedToothbrush() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    viewModel.activeProfileConnections.add(KLTBConnectionBuilder.createAndroidLess().build());

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    viewModel.manageSingleConnectionState(connection);

    verify(viewModel).showSingleConnectedToothbrush();
  }

  @Test
  public void
      manageSingleConnectionState_connectionStateACTIVE_setsStateSINGLE_TOOTHBRUSH_CONNECTED() {
    doNothing().when(viewModel).showSingleDisconnectedToothbrush();
    viewModel.activeProfileConnections.add(KLTBConnectionBuilder.createAndroidLess().build());

    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    assertNotEquals(SINGLE_TOOTHBRUSH_CONNECTED, viewModel.toolbarToothbrushState);

    viewModel.manageSingleConnectionState(connection);

    assertEquals(SINGLE_TOOTHBRUSH_CONNECTED, viewModel.toolbarToothbrushState);
  }

  /*
  ON MULTI TOOTHBRUSH
   */

  @Test
  public void onMultiToothbrush_isDisconnectedReturnsTrue_invokesShowMultiDisconnectedToothbrush() {
    doNothing().when(viewModel).showMultiDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    doReturn(true).when(viewModel).isDisconnected(any(KLTBConnection.class));

    viewModel.onMultiToothbrush(
        Arrays.asList(mock(KLTBConnection.class), mock(KLTBConnection.class)));

    verify(viewModel).showMultiDisconnectedToothbrush();
  }

  @Test
  public void onMultiToothbrush_isDisconnectedReturnsTrue_setsStateMULTI_TOOTHBRUSH_DISCONNECTED() {
    doNothing().when(viewModel).showMultiDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    doReturn(true).when(viewModel).isDisconnected(any(KLTBConnection.class));

    assertNotEquals(MULTI_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);

    viewModel.onMultiToothbrush(
        Arrays.asList(mock(KLTBConnection.class), mock(KLTBConnection.class)));

    assertEquals(MULTI_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);
  }

  @Test
  public void
      onMultiToothbrush_isDisconnectedFalse_hasOTAUpdateTrue_invokesShowMultiOTAToothbrush() {
    doNothing().when(viewModel).showMultiOtaAvailableToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connectionWithOTA = mock(KLTBConnection.class);
    KLTBConnection connectionWithoutOTA =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(true).when(viewModel).hasOtaUpdateAvailable(eq(connectionWithOTA));

    viewModel.onMultiToothbrush(Arrays.asList(connectionWithOTA, connectionWithoutOTA));

    verify(viewModel).showMultiOtaAvailableToothbrush();
  }

  @Test
  public void
      onMultiToothbrush_isDisconnectedFalse_hasOTAUpdateTrue_setsStateMULTI_TOOTHBRUSH_DISCONNECTED() {
    doNothing().when(viewModel).showMultiOtaAvailableToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connectionWithOTA = mock(KLTBConnection.class);
    KLTBConnection connectionWithoutOTA =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(true).when(viewModel).hasOtaUpdateAvailable(eq(connectionWithOTA));

    assertNotEquals(MULTI_TOOTHBRUSH_OTA_AVAILABLE, viewModel.toolbarToothbrushState);

    viewModel.onMultiToothbrush(Arrays.asList(connectionWithOTA, connectionWithoutOTA));

    assertEquals(MULTI_TOOTHBRUSH_OTA_AVAILABLE, viewModel.toolbarToothbrushState);
  }

  @Test
  public void
      onMultiToothbrush_isDisconnectedFalse_hasOTAUpdateFalse_loadConnectionReturnsTerminatedConnection_invokesShowMultiDisconnectedToothbrush() {
    doNothing().when(viewModel).showMultiDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    verify(viewModel).showMultiDisconnectedToothbrush();
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsTerminatedConnection_setsStateMULTI_TOOTHBRUSH_DISCONNECTED() {
    doNothing().when(viewModel).showMultiDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    assertNotEquals(MULTI_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    assertEquals(MULTI_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsTERMINATINGConnection_invokesShowMultiDisconnectedToothbrush() {
    doNothing().when(viewModel).showMultiDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATING)
            .build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    verify(viewModel).showMultiDisconnectedToothbrush();
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsTERMINATINGConnection_setsStateMULTI_TOOTHBRUSH_DISCONNECTED() {
    doNothing().when(viewModel).showMultiDisconnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATING)
            .build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    assertNotEquals(MULTI_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    assertEquals(MULTI_TOOTHBRUSH_DISCONNECTED, viewModel.toolbarToothbrushState);
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsESTABLISHINGConnection_invokesOnMultiConnectingToothbrush() {
    doNothing().when(viewModel).showMultiConnectingToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATING)
            .build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    doNothing().when(viewModel).onMultiConnectingToothbrush();

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    verify(viewModel).onMultiConnectingToothbrush();
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsOTAConnection_invokesOnMultiConnectingToothbrush() {
    doNothing().when(viewModel).showMultiConnectingToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.OTA).build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.TERMINATED).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    doNothing().when(viewModel).onMultiConnectingToothbrush();

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    verify(viewModel).onMultiConnectingToothbrush();
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsAllACTIVEConnection_invokesOnMultiConnectedToothbrush() {
    doNothing().when(viewModel).showMultiConnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    doNothing().when(viewModel).onMultiConnectedToothbrush();

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    verify(viewModel).onMultiConnectedToothbrush();
  }

  @Test
  public void
      onMultiToothbrush_loadConnectionReturnsOneACTIVEOneEstablishingConnection_invokesOnMultiConnectingToothbrush() {
    doNothing().when(viewModel).showMultiConnectedToothbrush();
    doNothing().when(viewModel).refreshLastSyncData(any(KLTBConnection.class));

    KLTBConnection connection1 =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build();
    KLTBConnection connection2 =
        KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build();

    doReturn(false).when(viewModel).isDisconnected(any(KLTBConnection.class));
    doReturn(false).when(viewModel).hasOtaUpdateAvailable(any(KLTBConnection.class));

    doNothing().when(viewModel).onMultiConnectingToothbrush();

    viewModel.onMultiToothbrush(Arrays.asList(connection1, connection2));

    verify(viewModel).onMultiConnectingToothbrush();
  }

  /*
  ON MULTI CONNECTED TOOTHBRUSH
   */

  @Test
  public void onMultiConnectedToothbrush_invokesShowMultiConnectedToothbrush() {
    doNothing().when(viewModel).showMultiConnectedToothbrush();

    viewModel.onMultiConnectedToothbrush();

    verify(viewModel).showMultiConnectedToothbrush();
  }

  @Test
  public void onMultiConnectedToothbrush_setsStateMULTI_TOOTHBRUSH_CONNECTED() {
    doNothing().when(viewModel).showMultiConnectedToothbrush();

    assertNotEquals(MULTI_TOOTHBRUSH_CONNECTED, viewModel.toolbarToothbrushState);

    viewModel.onMultiConnectedToothbrush();

    verify(viewModel).showMultiConnectedToothbrush();

    assertEquals(MULTI_TOOTHBRUSH_CONNECTED, viewModel.toolbarToothbrushState);
  }

  /*
  ON MULTI CONNECTING TOOTHBRUSH
   */

  @Test
  public void onMultiConnectingToothbrush_locationNotReady_invokesOnLocationNotReadyToScan() {
    when(locationStatus.isReadyToScan()).thenReturn(false);

    doNothing().when(viewModel).onLocationNotReadyToScan();

    viewModel.onMultiConnectingToothbrush();

    verify(viewModel).onLocationNotReadyToScan();
  }

  @Test
  public void onMultiConnectingToothbrush_invokesSetToothbrushState() {
    when(locationStatus.isReadyToScan()).thenReturn(true);

    assertNotEquals(MULTI_TOOTHBRUSH_CONNECTING, viewModel.toolbarToothbrushState);

    doNothing().when(viewModel).showMultiConnectingToothbrush();

    viewModel.onMultiConnectingToothbrush();

    assertEquals(MULTI_TOOTHBRUSH_CONNECTING, viewModel.toolbarToothbrushState);
  }

  @Test
  public void onMultiConnectingToothbrush_invokesShowMultiConnectingToothbrush() {
    when(locationStatus.isReadyToScan()).thenReturn(true);

    doNothing().when(viewModel).showMultiConnectingToothbrush();

    viewModel.onMultiConnectingToothbrush();

    verify(viewModel).showMultiConnectingToothbrush();
  }

  /*
  REGISTER AS STATE LISTENER
   */
  @Test
  public void registerAsStateListener_addsConnectionToSet() {
    assertTrue(viewModel.connectionWeakSet.isEmpty());

    KLTBConnection connection = mock(KLTBConnection.class);
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connection.state()).thenReturn(connectionState);
    viewModel.registerAsStateListener(connection);

    assertEquals(1, viewModel.connectionWeakSet.size());
    assertEquals(connection, viewModel.connectionWeakSet.iterator().next().get());
  }

  @Test
  public void registerAsStateListener_registersSelfAsListener() {
    KLTBConnection connection = mock(KLTBConnection.class);
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connection.state()).thenReturn(connectionState);
    viewModel.registerAsStateListener(connection);

    verify(connectionState).register(eq(viewModel));
  }

  /*
  ON CONNECTION STATE CHANGED
   */
  @Test
  public void onConnectionStateChanged_invokeslistenToToothbrushConnections() {
    viewModel.onConnectionStateChanged(mock(KLTBConnection.class), KLTBConnectionState.TERMINATING);

    verify(viewModel).refreshToothbrushUI();
  }

  /*
  STOP LISTENING TO BLUETOOTH STATE UPDATES
   */
  @Test
  public void unregisterConnectionStateListenters_unregistersAsConnectionStateObservable() {
    KLTBConnection connection = mock(KLTBConnection.class);
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connection.state()).thenReturn(connectionState);
    viewModel.connectionWeakSet.add(new WeakReference<>(connection));

    viewModel.unregisterConnectionStateListeners();

    verify(connectionState).unregister(eq(viewModel));
  }

  @Test
  public void unregisterConnectionStateListenters_clearsNullReferencesInConnectionSet() {
    KLTBConnection connection = mock(KLTBConnection.class);
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connection.state()).thenReturn(connectionState);
    viewModel.connectionWeakSet.add(new WeakReference<>(connection));
    viewModel.connectionWeakSet.add(new WeakReference<>(null));

    assertEquals(2, viewModel.connectionWeakSet.size());

    viewModel.unregisterConnectionStateListeners();

    assertEquals(1, viewModel.connectionWeakSet.size());
  }

  /*
  ON STOP
   */
  @Test
  public void onStop_clearsDisposables() {
    doNothing().when(viewModel).unregisterConnectionStateListeners();

    Disposable disposable = mock(Disposable.class);
    viewModel.onStopDisposables.add(disposable);

    invokeOnStop();

    verify(disposable).dispose();

    assertFalse(viewModel.onStopDisposables.isDisposed());
  }

  @Test
  public void onStop_invokesUnregisterConnectionStateListeners() {
    doNothing().when(viewModel).unregisterConnectionStateListeners();

    invokeOnStop();

    verify(viewModel).unregisterConnectionStateListeners();
  }

  private void invokeOnStop() {
    viewModel.onStop(mock(LifecycleOwner.class));
  }

  /*
  ON START
   */
  @Test
  public void onStart_invokeslistenToToothbrushConnections() {
    doNothingOnStart();

    invokeOnStart();

    verify(viewModel).listenToToothbrushConnections();
  }

  @Test
  public void onStart_invokesListenToBluetoothStateUpdates() {
    doNothingOnStart();

    invokeOnStart();

    verify(viewModel).listenToBluetoothStateUpdates();
  }

  @Test
  public void onStart_invokesListenToLastSyncObservable() {
    doNothingOnStart();

    invokeOnStart();

    verify(viewModel).listenToLastSyncObservable();
  }

  @Test
  public void onStart_invokesRefreshToothbrushUI() {
    doNothingOnStart();

    invokeOnStart();

    verify(viewModel).refreshToothbrushUI();
  }

  /*
  ON SINGLE TOOTHBRUSH DRAWABLES
   */
  @Test
  public void showSingleDisconnectedToothbrush_emitsDisconnectedDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showSingleDisconnectedToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_single_disconnected);
  }

  @Test
  public void showSingleConnectingToothbrush_emitsConnectingDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showSingleConnectingToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_single_connecting);
  }

  @Test
  public void showSingleConnectedToothbrush_emitsConnectedDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showSingleConnectedToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_single_connected);
  }

  @Test
  public void showSingleToothbrushOTAAvailable_emitsSingleOTAAvailableDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showSingleToothbrushOtaAvailable();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_single_ota_available);
  }

  /*
  HAS TOOTHBRUSH WITH MAC
  */

  @Test
  public void hasToothbrushWithMac_tbWithMac_returnsTrue() {
    String mac = "mac1";
    mockAccountTbsWithMac("mac0", mac, "mac2", "mac3");

    assertTrue(viewModel.hasToothbrushWithMac(mac));
  }

  @Test
  public void hasToothbrushWithMac_noTbWithMac_returnsFalse() {
    String mac = "mac3";
    mockAccountTbsWithMac("mac0", "mac1", "mac2");

    assertFalse(viewModel.hasToothbrushWithMac(mac));
  }

  private void mockAccountTbsWithMac(String... macs) {
    List<KLTBConnection> toothbrushes = new ArrayList<>();
    for (String mac : macs) {
      toothbrushes.add(KLTBConnectionBuilder.createAndroidLess().withMac(mac).build());
    }
    viewModel.activeProfileConnections.clear();
    viewModel.activeProfileConnections.addAll(toothbrushes);
  }

  /*
  ON MULTI TOOTHBRUSH DRAWABLES
   */
  @Test
  public void showMultiDisconnectedToothbrush_emitsDisconnectedDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showMultiDisconnectedToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_multi_disconnected);
  }

  @Test
  public void showMultiConnectingToothbrush_emitsConnectingDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showMultiConnectingToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_multi_connecting);
  }

  @Test
  public void showMultiConnectedToothbrush_emitsConnectedDrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showMultiConnectedToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_multi_connected);
  }

  @Test
  public void showMultiOTAToothbrush_emitsOTADrawable() {
    TestObserver<ToolbarToothbrushViewState> observer = viewModel.viewStateObservable().test();

    observer.assertEmpty();

    viewModel.showMultiOtaAvailableToothbrush();

    observer.assertValue(
        viewState -> viewState.getDrawable() == R.drawable.ic_toothbrush_multi_ota_available);
  }

  @Test
  public void toToothbrusState_returns_appropriate_object() {
    viewModel.activeProfileConnections.add(KLTBConnectionBuilder.createAndroidLess().build());

    assertTrue(
        viewModel.toToothbrusState(NO_BLUETOOH)
            instanceof com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth);

    assertTrue(viewModel.toToothbrusState(NO_LOCATION) instanceof NoLocation);

    assertTrue(viewModel.toToothbrusState(NO_SERVICE) instanceof NoService);

    assertTrue(
        viewModel.toToothbrusState(SINGLE_TOOTHBRUSH_DISCONNECTED)
            instanceof SingleToothbrushDisconnected);

    assertTrue(
        viewModel.toToothbrusState(MULTI_TOOTHBRUSH_DISCONNECTED)
            instanceof MultiToothbrushDisconnected);

    assertTrue(
        viewModel.toToothbrusState(SINGLE_TOOTHBRUSH_CONNECTING)
            instanceof SingleToothbrushConnecting);

    assertTrue(
        viewModel.toToothbrusState(MULTI_TOOTHBRUSH_CONNECTING)
            instanceof MultiToothbrushConnecting);

    assertTrue(
        viewModel.toToothbrusState(SINGLE_TOOTHBRUSH_CONNECTED)
            instanceof SingleToothbrushConnected);

    assertTrue(
        viewModel.toToothbrusState(SINGLE_TOOTHBRUSH_OTA_AVAILABLE)
            instanceof SingleToothbrushOtaAvailable);

    assertTrue(
        viewModel.toToothbrusState(SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS)
            instanceof SingleToothbrushOtaInProgress);

    assertTrue(
        viewModel.toToothbrusState(MULTI_TOOTHBRUSH_CONNECTED) instanceof MultiToothbrushConnected);

    assertTrue(
        viewModel.toToothbrusState(MULTI_TOOTHBRUSH_OTA_IN_PROGRESS)
            instanceof MultiToothbrushOtaInProgress);
    assertTrue(
        viewModel.toToothbrusState(MULTI_TOOTHBRUSH_OTA_AVAILABLE)
            instanceof MultiToothbrushOtaAvailable);

    assertTrue(
        viewModel.toToothbrusState(SYNCING_OFFLINE_BRUSHINGS)
            instanceof SingleSyncingOfflineBrushing);

    viewModel.activeProfileConnections.add(KLTBConnectionBuilder.createAndroidLess().build());
    assertTrue(
        viewModel.toToothbrusState(SYNCING_OFFLINE_BRUSHINGS)
            instanceof MultiSyncingOfflineBrushing);

    assertTrue(viewModel.toToothbrusState(STATE_NONE) instanceof Unknown);

    assertTrue(viewModel.toToothbrusState(NO_TOOTHBRUSHES) instanceof NoToothbrushes);
  }

  @Test
  public void toothbrushesSize_returns_activeProfiles_size() {
    final int ACTIVE_TB_SIZE = 3;
    List<KLTBConnection> connections = new ArrayList<>();
    for (int i = 0; i < ACTIVE_TB_SIZE; i++) {
      connections.add(
          KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build());
    }
    viewModel.activeProfileConnections.addAll(connections);

    assertEquals(ACTIVE_TB_SIZE, viewModel.toothbrushesSize());
  }

  @Override
  public void tearDown() throws Exception {
    FailEarly.overrideDelegateWith(TestDelegate.INSTANCE);
    super.tearDown();
  }

  static class TestToolbarToothbrushViewModel extends ToolbarToothbrushViewModel {

    TestToolbarToothbrushViewModel(
        @NonNull ServiceProvider serviceProvider,
        @NonNull IKolibreeConnector kolibreeConnector,
        @NonNull ToothbrushRepository toothbrushRepository,
        @NonNull IBluetoothUtils bluetoothUtils,
        @NonNull MainActivityNavigationController mainActivityNavigationViewModel,
        @NonNull LastSyncObservable lastSyncObservable,
        @NonNull ToolbarToothbrushFormatter formatter,
        LocationStatus locationStatus,
        Lifecycle lifecycle,
        ToothbrushesForProfileUseCase toothbrushesForProfileUseCase) {
      super(
          serviceProvider,
          kolibreeConnector,
          toothbrushRepository,
          bluetoothUtils,
          mainActivityNavigationViewModel,
          lastSyncObservable,
          formatter,
          locationStatus,
          lifecycle,
          toothbrushesForProfileUseCase);
    }

    void setService(KolibreeService kolibreeService) {
      this.kolibreeService = kolibreeService;
    }
  }

  private void doNothingOnStart() {
    when(serviceProvider.connectStream()).thenReturn(Observable.just(ServiceDisconnected.INSTANCE));
    doNothing().when(viewModel).listenToToothbrushConnections();
    doNothing().when(viewModel).listenToBluetoothStateUpdates();
    doNothing().when(viewModel).listenToLastSyncObservable();
    doNothing().when(viewModel).refreshToothbrushUI();
    doNothing().when(viewModel).onKolibreeServiceDisconnected();
  }

  private void invokeOnStart() {
    viewModel.onStart(mock(LifecycleOwner.class));
  }
}
