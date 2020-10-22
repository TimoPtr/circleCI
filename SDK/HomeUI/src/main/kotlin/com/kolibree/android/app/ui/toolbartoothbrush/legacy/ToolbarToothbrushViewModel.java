/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush.legacy;

import android.annotation.SuppressLint;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.kolibree.account.utils.ToothbrushesForProfileUseCase;
import com.kolibree.android.app.dagger.scopes.ActivityScope;
import com.kolibree.android.app.ui.common.BaseKolibreeServiceViewModel;
import com.kolibree.android.app.ui.navigation.MainActivityNavigationController;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiSyncingOfflineBrushing;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushConnected;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushConnecting;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushDisconnected;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushOtaAvailable;
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushOtaInProgress;
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushes;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleSyncingOfflineBrushing;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaAvailable;
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaInProgress;
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState;
import com.kolibree.android.app.ui.toolbartoothbrush.Unknown;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.formatter.ToolbarToothbrushFormatter;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.Connected;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.Connecting;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.Disconnected;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.NoBluetooth;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.NoLocation;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.NoToothbrush;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.Syncing;
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.state.ToolbarState;
import com.kolibree.android.commons.AppConfiguration;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.homeui.R;
import com.kolibree.android.location.LocationStatus;
import com.kolibree.android.offlinebrushings.sync.LastSyncData;
import com.kolibree.android.offlinebrushings.sync.LastSyncObservable;
import com.kolibree.android.offlinebrushings.sync.StartSync;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionStateListener;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.sdk.persistence.model.AccountToothbrush;
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import com.kolibree.sdkws.core.IKolibreeConnector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Controls the View State of the toothbrush widget in the toolbar
 *
 * <p>The logic is very similar to the one in ToothbrushListFragment
 *
 * <p>Created by miguelaragues on 11/12/17.
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
public class ToolbarToothbrushViewModel extends BaseKolibreeServiceViewModel
    implements ConnectionStateListener {

  static final int STATE_NONE = 0;
  static final int SINGLE_TOOTHBRUSH_DISCONNECTED = 1;
  static final int SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS = 2;
  static final int SINGLE_TOOTHBRUSH_CONNECTING = 3;
  static final int SINGLE_TOOTHBRUSH_CONNECTED = 4;
  static final int MULTI_TOOTHBRUSH_DISCONNECTED = 5;
  static final int MULTI_TOOTHBRUSH_OTA_IN_PROGRESS = 6;
  static final int MULTI_TOOTHBRUSH_CONNECTING = 7;
  static final int MULTI_TOOTHBRUSH_CONNECTED = 8;
  static final int NO_TOOTHBRUSHES = 9;
  static final int NO_SERVICE = 10;
  static final int NO_BLUETOOH = 11;
  static final int SINGLE_TOOTHBRUSH_OTA_AVAILABLE = 12;
  static final int MULTI_TOOTHBRUSH_OTA_AVAILABLE = 13;
  static final int SYNCING_OFFLINE_BRUSHINGS = 14;
  static final int NO_LOCATION = 15;

  @VisibleForTesting
  final BehaviorRelay<ToolbarToothbrushViewState> viewStateBehaviorRelay = BehaviorRelay.create();

  @VisibleForTesting final Set<WeakReference<KLTBConnection>> connectionWeakSet = new HashSet<>();
  final List<String> macsBeingListenedForOta = new ArrayList<>();
  private final ToothbrushRepository toothbrushRepository;
  private final IBluetoothUtils bluetoothUtils;
  private final IKolibreeConnector connector;
  private final MainActivityNavigationController mainActivityNavigationController;
  private final LastSyncObservable lastSyncObservable;
  private final ToolbarToothbrushFormatter formatter;
  private final LocationStatus locationStatus;
  private final ToothbrushesForProfileUseCase toothbrushesForProfileUseCase;
  @ToolbarToothbrushState @VisibleForTesting volatile int toolbarToothbrushState = STATE_NONE;

  @VisibleForTesting Disposable bluetoothStateDisposable;
  @VisibleForTesting Disposable toothbrushesDisposable = null;
  private Disposable lastSyncDisposable;
  @VisibleForTesting final CompositeDisposable onStopDisposables = new CompositeDisposable();

  @VisibleForTesting
  ToolbarToothbrushViewState viewState = ToolbarToothbrushViewState.create(NoToothbrush.INSTANCE);

  @VisibleForTesting final List<KLTBConnection> activeProfileConnections = new ArrayList<>();

  ToolbarToothbrushViewModel(
      @NonNull ServiceProvider serviceProvider,
      @NonNull IKolibreeConnector connector,
      @NonNull ToothbrushRepository toothbrushRepository,
      @NonNull IBluetoothUtils bluetoothUtils,
      @NonNull MainActivityNavigationController mainActivityNavigationController,
      @NonNull LastSyncObservable lastSyncObservable,
      @NonNull ToolbarToothbrushFormatter formatter,
      LocationStatus locationStatus,
      Lifecycle lifecycle,
      ToothbrushesForProfileUseCase toothbrushesForProfileUseCase) {
    super(serviceProvider);

    this.connector = connector;
    this.toothbrushRepository = toothbrushRepository;
    this.bluetoothUtils = bluetoothUtils;
    this.mainActivityNavigationController = mainActivityNavigationController;
    this.lastSyncObservable = lastSyncObservable;
    this.formatter = formatter;
    this.locationStatus = locationStatus;
    this.toothbrushesForProfileUseCase = toothbrushesForProfileUseCase;

    lifecycle.addObserver(this);
  }

  /**
   * An observable of ToolbarToothbrushViewState that will react to the connectivity state of the
   * paired toothbrushes, if any.
   *
   * @return a non-null Observable
   */
  @NonNull
  public Observable<ToolbarToothbrushViewState> viewStateObservable() {
    return viewStateBehaviorRelay
        .doAfterNext(
            viewStateSent -> {
              if (viewStateSent.getActionId() != ToolbarToothbrushAction.ACTION_NONE) {
                emitViewState(viewState.withActionId(ToolbarToothbrushAction.ACTION_NONE));
              }
            })
        .hide();
  }

  public void refresh() {
    refreshToothbrushUI();
  }

  @Override
  protected void onCleared() {
    super.onCleared();

    onStopDisposables.dispose();
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    super.onStart(owner);

    listenToToothbrushConnections();

    listenToBluetoothStateUpdates();

    listenToLastSyncObservable();

    refreshToothbrushUI();
  }

  @VisibleForTesting
  void listenToToothbrushConnections() {
    if (toothbrushesDisposable == null || toothbrushesDisposable.isDisposed()) {
      toothbrushesDisposable =
          toothbrushesForProfileUseCase
              .currentProfileToothbrushesOnceAndStream()
              .subscribeOn(Schedulers.io())
              .distinctUntilChanged()
              .onTerminateDetach()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(this::onToothbrushesUpdated, Timber::e);

      DisposableUtils.addSafely(onStopDisposables, toothbrushesDisposable);
    }
  }

  @VisibleForTesting
  void onToothbrushesUpdated(List<KLTBConnection> toothbrushes) {
    activeProfileConnections.clear();
    activeProfileConnections.addAll(toothbrushes);

    for (KLTBConnection connection : activeProfileConnections) {
      registerAsStateListener(connection);
    }

    refreshToothbrushUI();
  }

  @VisibleForTesting
  void listenToBluetoothStateUpdates() {
    if (bluetoothStateDisposable == null || bluetoothStateDisposable.isDisposed()) {
      bluetoothStateDisposable =
          bluetoothUtils
              .bluetoothStateObservable()
              .distinctUntilChanged()
              .subscribe(this::onBluetoothStateUpdated, Timber::e);

      DisposableUtils.addSafely(onStopDisposables, bluetoothStateDisposable);
    }
  }

  @VisibleForTesting
  void onBluetoothStateUpdated(boolean isEnabled) {
    refreshToothbrushUI();
  }

  @VisibleForTesting
  void listenToLastSyncObservable() {
    if (lastSyncDisposable == null || lastSyncDisposable.isDisposed()) {
      lastSyncDisposable =
          lastSyncObservable
              .observable()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(this::refreshLastSyncData, Timber::e);

      DisposableUtils.addSafely(onStopDisposables, lastSyncDisposable);
    }
  }

  @VisibleForTesting
  void refreshLastSyncData(LastSyncData data) {
    if (!hasToothbrushWithMac(data.getToothbrushMac())) {
      return;
    }

    if (data instanceof StartSync) {
      setToothbrushState(SYNCING_OFFLINE_BRUSHINGS);
      showSyncingToothbrush();
    } else {
      refreshToothbrushUI();
      String lastSync = formatter.format(data);
      emitViewState(viewState.withLastSyncText(lastSync));
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    super.onStop(owner);

    unregisterConnectionStateListeners();

    onStopDisposables.clear();
  }

  @VisibleForTesting
  void unregisterConnectionStateListeners() {
    synchronized (connectionWeakSet) {
      Iterator<WeakReference<KLTBConnection>> it = connectionWeakSet.iterator();
      while (it.hasNext()) {
        KLTBConnection connection = it.next().get();
        if (connection != null) {
          connection.state().unregister(this);
        } else {
          it.remove();
        }
      }
    }
  }

  void refreshLastSyncData(@Nullable KLTBConnection connection) {
    if (connection == null) {
      return;
    }

    LastSyncData data = lastSyncObservable.getLastSyncData(connection.toothbrush().getMac());
    if (hasToothbrushWithMac(data.getToothbrushMac())) {
      String lastSync = formatter.format(data);
      emitViewState(viewState.withLastSyncText(lastSync));
    }
  }

  @VisibleForTesting
  boolean hasToothbrushWithMac(@NonNull String tbMac) {
    for (KLTBConnection connection : activeProfileConnections) {
      if (connection.toothbrush().getMac().equals(tbMac)) {
        return true;
      }
    }

    return false;
  }

  @VisibleForTesting
  void emitViewState(ToolbarToothbrushViewState newViewState) {
    viewState =
        newViewState
            .withToolbarState(toToolbarState(toolbarToothbrushState))
            .withToothbrushState(toToothbrusState(toolbarToothbrushState));
    viewStateBehaviorRelay.accept(viewState);
  }

  @Override
  protected void onKolibreeServiceConnected(@NonNull KolibreeService service) {
    super.onKolibreeServiceConnected(service);

    refreshToothbrushUI();
  }

  @Override
  protected void onKolibreeServiceDisconnected() {
    super.onKolibreeServiceDisconnected();

    refreshToothbrushUI();
  }

  @VisibleForTesting
  void onInitializedWithoutBluetooth() {
    setToothbrushState(NO_BLUETOOH);

    askEnableBluetooth();
  }

  @VisibleForTesting
  void onLocationNotReadyToScan() {
    setToothbrushState(NO_LOCATION);

    // emitViewState applies the toothbrush state internally (withToolbarState)
    emitViewState(viewState);
  }

  @VisibleForTesting
  void refreshToothbrushUI() {
    if (!bluetoothUtils.isBluetoothEnabled()) {
      onBluetoothNotAvailable();
    } else if (kolibreeService() == null) {
      onKolibreeServiceNotAvailable();
    } else if (activeProfileConnections.isEmpty()) {
      onZeroToothbrushes();
    } else if (activeProfileConnections.size() == 1) {
      onSingleToothbrush(activeProfileConnections.get(0));
    } else {
      onMultiToothbrush(activeProfileConnections);
    }
  }

  @VisibleForTesting
  void onKolibreeServiceNotAvailable() {
    Timber.w("Toothbrush state NO_SERVICE");
    setToothbrushState(NO_SERVICE);

    showSingleDisconnectedToothbrush();
  }

  @VisibleForTesting
  void onBluetoothNotAvailable() {
    setToothbrushState(NO_BLUETOOH);

    showSingleDisconnectedToothbrush();

    unregisterConnectionStateListeners();
  }

  @NonNull
  @VisibleForTesting
  List<AccountToothbrush> getAccountToothbrushes() {
    return toothbrushRepository.getAccountToothbrushes(connector.getAccountId());
  }

  @VisibleForTesting
  void onZeroToothbrushes() {
    setToothbrushState(NO_TOOTHBRUSHES);

    showSingleDisconnectedToothbrush();
  }

  @VisibleForTesting
  void onSingleToothbrush(KLTBConnection connection) {
    refreshLastSyncData(connection);
    if (isDisconnected(connection)) {
      setToothbrushState(SINGLE_TOOTHBRUSH_DISCONNECTED);

      showSingleDisconnectedToothbrush();
    } else if (hasOtaUpdateAvailable(connection)) {
      setToothbrushState(SINGLE_TOOTHBRUSH_OTA_AVAILABLE);

      showSingleToothbrushOtaAvailable();
    } else {
      manageSingleConnectionState(connection);
    }
  }

  @VisibleForTesting
  boolean hasOtaUpdateAvailable(@NonNull KLTBConnection connection) {
    if (connection.state().getCurrent() != KLTBConnectionState.ACTIVE) return false;

    listenToOtaUpdates(connection);

    return connection.getTag() != null;
  }

  @VisibleForTesting
  void listenToOtaUpdates(KLTBConnection connection) {
    if (shouldListenToConnectionOta(connection)) {
      String mac = connection.toothbrush().getMac();
      addToDisposables(
          connection
              .hasOTAObservable()
              .doOnSubscribe(ignore -> macsBeingListenedForOta.add(mac))
              .doOnError(ignore -> macsBeingListenedForOta.remove(mac))
              .subscribe(ignore -> refreshToothbrushUI(), Timber::e));
    }
  }

  /**
   * Determines if we should subscribe to OTA availability for the connection.
   *
   * @return false if connection is null or we are already listening to OTA state, true otherwise
   */
  @VisibleForTesting
  boolean shouldListenToConnectionOta(KLTBConnection connection) {
    if (connection == null) {
      return false;
    }

    return !macsBeingListenedForOta.contains(connection.toothbrush().getMac());
  }

  @VisibleForTesting
  void manageSingleConnectionState(@NonNull KLTBConnection connection) {
    switch (connection.state().getCurrent()) {
      case ACTIVE:
        setToothbrushState(SINGLE_TOOTHBRUSH_CONNECTED);

        showSingleConnectedToothbrush();
        break;
      case OTA:
        onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS);
        break;
      case NEW:
      case ESTABLISHING:
      default:
        onSingleConnectingToothbrush(SINGLE_TOOTHBRUSH_CONNECTING);
        break;
    }
  }

  @VisibleForTesting
  void onSingleConnectingToothbrush(@ToolbarToothbrushState int toothbrushState) {
    if (locationStatus.isReadyToScan()) {
      setToothbrushState(toothbrushState);

      showSingleConnectingToothbrush();
    } else {
      onLocationNotReadyToScan();
    }
  }

  /** @param connections non-empty list of available toothbrushes */
  @VisibleForTesting
  void onMultiToothbrush(@NonNull List<KLTBConnection> connections) {
    boolean hasConnectingToothbrush = false, everyConnectionIsActive = true;
    for (KLTBConnection connection : connections) {
      if (!isDisconnected(connection)) {
        if (hasOtaUpdateAvailable(connection)) {
          onMultiOtaToothbrush();

          return;
        }

        switch (connection.state().getCurrent()) {
          case ACTIVE:
            // do nothing
            break;
          case TERMINATED:
          case TERMINATING:
            everyConnectionIsActive = false;
            break;
          case OTA:
          case NEW:
          case ESTABLISHING:
          default:
            everyConnectionIsActive = false;
            hasConnectingToothbrush = true;
            break;
        }
      } else {
        everyConnectionIsActive = false;
      }

      refreshLastSyncData(connection);
    }

    if (everyConnectionIsActive) {
      onMultiConnectedToothbrush();
    } else if (hasConnectingToothbrush) {
      onMultiConnectingToothbrush();
    } else {
      onMultiDisconnectedToothbrush();
    }
  }

  private void onMultiOtaToothbrush() {
    setToothbrushState(MULTI_TOOTHBRUSH_OTA_AVAILABLE);

    showMultiOtaAvailableToothbrush();
  }

  @VisibleForTesting
  void onMultiConnectedToothbrush() {
    setToothbrushState(MULTI_TOOTHBRUSH_CONNECTED);

    showMultiConnectedToothbrush();
  }

  @VisibleForTesting
  void onMultiConnectingToothbrush() {
    if (locationStatus.isReadyToScan()) {
      setToothbrushState(MULTI_TOOTHBRUSH_CONNECTING);

      showMultiConnectingToothbrush();
    } else {
      onLocationNotReadyToScan();
    }
  }

  private void onMultiDisconnectedToothbrush() {
    setToothbrushState(MULTI_TOOTHBRUSH_DISCONNECTED);

    showMultiDisconnectedToothbrush();
  }

  private void askEnableBluetooth() {
    emitViewState(viewState.withActionId(ToolbarToothbrushAction.ACTION_ASK_ENABLE_BLUETOOTH));
  }

  @VisibleForTesting
  void showNoDisconnectedToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_single_disconnected));
  }

  @VisibleForTesting
  void showSingleDisconnectedToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_single_disconnected));
  }

  @VisibleForTesting
  void showSingleConnectingToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_single_connecting));
  }

  @VisibleForTesting
  void showSingleToothbrushOtaAvailable() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_single_ota_available));
  }

  @VisibleForTesting
  void showSingleConnectedToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_single_connected));
  }

  @VisibleForTesting
  void showMultiDisconnectedToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_multi_disconnected));
  }

  @VisibleForTesting
  void showMultiConnectedToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_multi_connected));
  }

  @VisibleForTesting
  void showMultiConnectingToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_multi_connecting));
  }

  @VisibleForTesting
  void showMultiOtaAvailableToothbrush() {
    emitViewState(viewState.withIcon(R.drawable.ic_toothbrush_multi_ota_available));
  }

  @VisibleForTesting
  void showSyncingToothbrush() {
    emitViewState(viewState);
  }

  @VisibleForTesting
  void registerAsStateListener(@NonNull KLTBConnection connection) {
    synchronized (connectionWeakSet) {
      connectionWeakSet.add(new WeakReference<>(connection));
    }

    connection.state().register(this);
  }

  @VisibleForTesting
  boolean isDisconnected(@Nullable KLTBConnection connection) {
    if (connection == null) {
      return true;
    }

    KLTBConnectionState currentState = connection.state().getCurrent();

    return currentState == KLTBConnectionState.TERMINATED
        || currentState == KLTBConnectionState.TERMINATING;
  }

  @Override
  public void onConnectionStateChanged(
      @NonNull KLTBConnection connection, @NonNull KLTBConnectionState newState) {
    refreshToothbrushUI();
  }

  public void onToolbarClicked() {
    ToolbarState state = toToolbarState(toolbarToothbrushState);
    if (state instanceof Disconnected || state instanceof Connecting) {
      mainActivityNavigationController.navigateToConnectionHelpScreen();
    } else {
      onToothbrushIconClicked();
    }
  }

  public void onToothbrushIconClicked() {
    ToolbarState state = toToolbarState(toolbarToothbrushState);
    if (state instanceof NoToothbrush) {
      mainActivityNavigationController.navigateToSetupToothbrush();
    } else if (state instanceof NoBluetooth) {
      askEnableBluetooth();
    } else if (state instanceof NoLocation) {
      mainActivityNavigationController.navigateToGrantLocation();
    } else {
      navigateToAppropriateToothbrushScreen();
    }
  }

  private void navigateToAppropriateToothbrushScreen() {
    KLTBConnection connection = fetchUniqueConnection();
    if (connection == null) {
      mainActivityNavigationController.navigateToMyToothbrushes();
    } else {
      mainActivityNavigationController.navigateToToothbrush(connection);
    }
  }

  @VisibleForTesting
  @Nullable
  public KLTBConnection fetchUniqueConnection() {
    if (activeProfileConnections.size() != 1) {
      return null;
    }

    return activeProfileConnections.get(0);
  }

  private void setToothbrushState(@ToolbarToothbrushState int toolbarToothbrushState) {
    this.toolbarToothbrushState = toolbarToothbrushState;
  }

  @VisibleForTesting
  public ToothbrushConnectionState toToothbrusState(int currentToolbarToothbrushState) {
    switch (currentToolbarToothbrushState) {
      case NO_BLUETOOH:
        return new com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth(
            toothbrushesSize(), macAddress());
      case NO_LOCATION:
        return new com.kolibree.android.app.ui.toolbartoothbrush.NoLocation(
            toothbrushesSize(), macAddress());
      case NO_SERVICE:
        return new com.kolibree.android.app.ui.toolbartoothbrush.NoService(
            toothbrushesSize(), macAddress());
      case SINGLE_TOOTHBRUSH_DISCONNECTED:
        return new SingleToothbrushDisconnected(macAddress());
      case MULTI_TOOTHBRUSH_DISCONNECTED:
        return new MultiToothbrushDisconnected(macAddresses());
      case SINGLE_TOOTHBRUSH_CONNECTING:
        return new SingleToothbrushConnecting(macAddress());
      case MULTI_TOOTHBRUSH_CONNECTING:
        return new MultiToothbrushConnecting(macAddresses());
      case SINGLE_TOOTHBRUSH_CONNECTED:
        return new SingleToothbrushConnected(macAddress());
      case SINGLE_TOOTHBRUSH_OTA_AVAILABLE:
        return new SingleToothbrushOtaAvailable(macAddress());
      case SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS:
        return new SingleToothbrushOtaInProgress(macAddress());
      case MULTI_TOOTHBRUSH_CONNECTED:
        return new MultiToothbrushConnected(macAddresses());
      case MULTI_TOOTHBRUSH_OTA_IN_PROGRESS:
        return new MultiToothbrushOtaInProgress(macAddresses());
      case MULTI_TOOTHBRUSH_OTA_AVAILABLE:
        return new MultiToothbrushOtaAvailable(macAddresses());
      case SYNCING_OFFLINE_BRUSHINGS:
        if (activeProfileConnections.size() >= 2) {
          return new MultiSyncingOfflineBrushing(macAddresses());
        }
        return new SingleSyncingOfflineBrushing(macAddress());
      case STATE_NONE:
        return Unknown.INSTANCE;
      case NO_TOOTHBRUSHES:
      default:
        return new NoToothbrushes(toothbrushesSize());
    }
  }

  @VisibleForTesting
  public int toothbrushesSize() {
    return activeProfileConnections.size();
  }

  @NonNull
  private String macAddress() {
    if (!activeProfileConnections.isEmpty()) {
      return activeProfileConnections.get(0).toothbrush().getMac();
    }
    return "";
  }

  @NonNull
  private List<String> macAddresses() {
    List<String> macs = new ArrayList<>();
    for (KLTBConnection connection : activeProfileConnections) {
      macs.add(connection.toothbrush().getMac());
    }
    return macs;
  }

  @VisibleForTesting
  ToolbarState toToolbarState(@ToolbarToothbrushState int currentToolbarToothbrushState) {
    switch (currentToolbarToothbrushState) {
      case NO_BLUETOOH:
        return NoBluetooth.INSTANCE;
      case NO_LOCATION:
        return NoLocation.INSTANCE;

      case NO_SERVICE:
      case SINGLE_TOOTHBRUSH_DISCONNECTED:
      case MULTI_TOOTHBRUSH_DISCONNECTED:
        return Disconnected.INSTANCE;

      case SINGLE_TOOTHBRUSH_CONNECTING:
      case MULTI_TOOTHBRUSH_CONNECTING:
        return Connecting.INSTANCE;

      case SINGLE_TOOTHBRUSH_CONNECTED:
      case SINGLE_TOOTHBRUSH_OTA_AVAILABLE:
      case SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS:
      case MULTI_TOOTHBRUSH_CONNECTED:
      case MULTI_TOOTHBRUSH_OTA_IN_PROGRESS:
      case MULTI_TOOTHBRUSH_OTA_AVAILABLE:
        return Connected.INSTANCE;

      case SYNCING_OFFLINE_BRUSHINGS:
        return Syncing.INSTANCE;

      case STATE_NONE:
      case NO_TOOTHBRUSHES:
      default:
        return NoToothbrush.INSTANCE;
    }
  }

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    STATE_NONE,
    NO_BLUETOOH,
    NO_LOCATION,
    NO_SERVICE,
    NO_TOOTHBRUSHES,
    SINGLE_TOOTHBRUSH_DISCONNECTED,
    SINGLE_TOOTHBRUSH_CONNECTING,
    SINGLE_TOOTHBRUSH_CONNECTED,
    SINGLE_TOOTHBRUSH_OTA_AVAILABLE,
    SINGLE_TOOTHBRUSH_OTA_IN_PROGRESS,
    MULTI_TOOTHBRUSH_DISCONNECTED,
    MULTI_TOOTHBRUSH_CONNECTING,
    MULTI_TOOTHBRUSH_CONNECTED,
    MULTI_TOOTHBRUSH_OTA_AVAILABLE,
    MULTI_TOOTHBRUSH_OTA_IN_PROGRESS,
    SYNCING_OFFLINE_BRUSHINGS,
  })
  @interface ToolbarToothbrushState {}

  /*
  Special case were we set ActivityScope to a fragment's ViewModel factory

  There are multiple ToolbarToothbrushFragment and I'd rather keep a single ViewModel
   */
  @ActivityScope
  public static class Factory implements ViewModelProvider.Factory {

    private final ToothbrushRepository toothbrushRepository;
    private final IBluetoothUtils bluetoothUtils;
    private final MainActivityNavigationController mainActivityNavigationViewModel;
    private final ServiceProvider serviceProvider;
    private final IKolibreeConnector kolibreeConnector;
    private final LastSyncObservable lastSyncObservable;
    private final ToolbarToothbrushFormatter formatter;
    private final LocationStatus locationActionChecker;
    private final LifecycleOwner lifecycleOwner;
    private final ToothbrushesForProfileUseCase toothbrushesForProfileUseCase;
    private final AppConfiguration appConfiguration;

    @Inject
    Factory(
        @NonNull ServiceProvider serviceProvider,
        @NonNull IKolibreeConnector kolibreeConnector,
        @NonNull ToothbrushRepository toothbrushRepository,
        @NonNull IBluetoothUtils bluetoothUtils,
        @NonNull MainActivityNavigationController mainActivityNavigationViewModel,
        @NonNull LastSyncObservable lastSyncObservable,
        @NonNull ToolbarToothbrushFormatter formatter,
        LocationStatus locationActionChecker,
        LifecycleOwner lifecycleOwner,
        ToothbrushesForProfileUseCase toothbrushesForProfileUseCase,
        AppConfiguration appConfiguration) {
      this.serviceProvider = serviceProvider;
      this.kolibreeConnector = kolibreeConnector;
      this.toothbrushRepository = toothbrushRepository;
      this.bluetoothUtils = bluetoothUtils;
      this.mainActivityNavigationViewModel = mainActivityNavigationViewModel;
      this.lastSyncObservable = lastSyncObservable;
      this.formatter = formatter;
      this.locationActionChecker = locationActionChecker;
      this.lifecycleOwner = lifecycleOwner;
      this.toothbrushesForProfileUseCase = toothbrushesForProfileUseCase;
      this.appConfiguration = appConfiguration;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public ToolbarToothbrushViewModel create(@NonNull Class modelClass) {
      return new ToolbarToothbrushViewModel(
          serviceProvider,
          kolibreeConnector,
          toothbrushRepository,
          bluetoothUtils,
          mainActivityNavigationViewModel,
          lastSyncObservable,
          formatter,
          locationActionChecker,
          lifecycleOwner.getLifecycle(),
          toothbrushesForProfileUseCase);
    }
  }
}
