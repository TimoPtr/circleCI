/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.otachecker;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.jakewharton.rx.ReplayingShare;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;
import com.kolibree.android.app.ui.navigation.MainActivityNavigationController;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush;
import com.kolibree.android.toothbrushupdate.OtaChecker;
import com.kolibree.android.toothbrushupdate.OtaForConnection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

/** Created by miguelaragues on 4/5/18. */
@SuppressLint("DeobfuscatedPublicSdkClass")
public class OtaCheckerViewModel extends ViewModel implements DefaultLifecycleObserver {

  @VisibleForTesting final PublishRelay<OtaCheckerViewState> viewStateRelay = PublishRelay.create();

  private final MainActivityNavigationController mainActivityNavigationViewModel;
  private final OtaChecker otaChecker;

  @VisibleForTesting Disposable checkOtaDisposable;

  @VisibleForTesting KLTBConnection connectionMandatoryUpdate;

  @VisibleForTesting
  BehaviorRelay<Boolean> userWantsMandatoryUpdate = BehaviorRelay.createDefault(false);

  OtaCheckerViewState viewState;
  private final Observable<OtaCheckerViewState> viewStateObservable =
      viewStateRelay
          .startWith(OtaCheckerViewState.EMPTY)
          .doOnNext(newViewState -> viewState = newViewState)
          .compose(ReplayingShare.instance());

  @VisibleForTesting
  OtaCheckerViewModel(
      MainActivityNavigationController navigationController, OtaChecker otaChecker) {
    super();

    this.mainActivityNavigationViewModel = navigationController;
    this.otaChecker = otaChecker;
  }

  public Observable<OtaCheckerViewState> viewStateObservable() {
    return viewStateObservable;
  }

  @Override
  protected void onCleared() {
    super.onCleared();

    DisposableUtils.forceDispose(checkOtaDisposable);
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    if (checkOtaDisposable == null || checkOtaDisposable.isDisposed()) {
      checkOtaDisposable =
          otaChecker
              .otaForConnectionsOnce()
              .subscribeOn(Schedulers.io())
              .doFinally(() -> checkOtaDisposable = null)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(this::onOtaForConnection, Throwable::printStackTrace);
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    DisposableUtils.forceDispose(checkOtaDisposable);
  }

  @VisibleForTesting
  void onOtaForConnection(OtaForConnection otaUpdateForConnection) {
    switch (otaUpdateForConnection.getOtaUpdateType()) {
      case STANDARD:
        break;
      case MANDATORY:
        connectionMandatoryUpdate = otaUpdateForConnection.getConnection();

        onMandatoryUpdateNeeded();
        break;
      case MANDATORY_NEEDS_INTERNET:
        emitShowEnableInternet(true);
        break;
      default:
        // do nothing
    }
  }

  /**
   * Ask for firmware update if there's connectivity available. If there isn't, it waits until there
   * is and then checks
   */
  @VisibleForTesting
  void onMandatoryUpdateNeeded() {
    dismissEnableInternetDialog();

    emitMandatoryUpdateNeeded(true);

    userWantsMandatoryUpdate.accept(false);
  }

  /** Notify that the user accepted to upgrade the toothbrush. */
  public void userConfirmedMandatoryUpdate() {
    emitEmptyViewState();

    userWantsMandatoryUpdate.accept(true);

    Toothbrush toothbrush = connectionMandatoryUpdate.toothbrush();
    proceedWithOtaUpdate(toothbrush.getMac(), toothbrush.getModel());
  }

  @VisibleForTesting
  void emitEmptyViewState() {
    viewStateRelay.accept(OtaCheckerViewState.EMPTY);
  }

  @VisibleForTesting
  void emitMandatoryUpdateNeeded(boolean mandatoryUpdateNeeded) {
    viewStateRelay.accept(viewState.withMandatoryUpdateNeeded(mandatoryUpdateNeeded));
  }

  @VisibleForTesting
  void emitShowEnableInternet(boolean showEnableInternet) {
    viewStateRelay.accept(viewState.withShowEnableInternet(showEnableInternet));
  }

  @VisibleForTesting
  void proceedWithOtaUpdate(String mac, ToothbrushModel model) {
    mainActivityNavigationViewModel.navigateToOtaUpdate(mac, model);
  }

  @VisibleForTesting
  void dismissEnableInternetDialog() {
    emitShowEnableInternet(false);
  }

  @VisibleForTesting
  boolean connectionSupportsOta(KLTBConnection connection) {
    return connection.state().getCurrent() == KLTBConnectionState.ACTIVE;
  }

  public static class Factory implements ViewModelProvider.Factory {

    private final MainActivityNavigationController mainActivityNavigator;
    private final OtaChecker otaChecker;

    @Inject
    Factory(MainActivityNavigationController navigationController, OtaChecker otaChecker) {
      this.mainActivityNavigator = navigationController;
      this.otaChecker = otaChecker;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public OtaCheckerViewModel create(@NonNull Class modelClass) {
      return new OtaCheckerViewModel(mainActivityNavigator, otaChecker);
    }
  }
}
