package com.kolibree.android.app.ui.ota;

import static com.kolibree.android.TimberTagKt.otaTagFor;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.CANCELED;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.COMPLETED;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.ERROR;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.IN_PROGRESS;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewModel.UpdateStatus.NOT_STARTED;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_EXIT_CANCEL;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_EXIT_SUCCESS;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_INSTALLING;
import static com.kolibree.android.app.ui.ota.OtaUpdateViewStateKt.OTA_ACTION_REBOOTING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromError;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_BLOCKED_NOT_CHARGING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_COMPLETED;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_ERROR;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_REBOOTING;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModelProvider;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.android.app.ui.common.BaseKolibreeServiceViewModel;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCase;
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker;
import com.kolibree.android.toothbrushupdate.R;
import com.kolibree.android.tracker.Analytics;
import com.kolibree.android.translationssupport.TranslationContext;
import com.kolibree.sdkws.data.model.GruwareData;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Firmware and GRU data OTA update process view model.
 *
 * <p>We want the user to feel the upgrade process as a single operation, thus we are going to
 * constrain firmware progress from 0% to 50%, and Gru progress from 50% to 100%
 */
@VisibleForApp
public class OtaUpdateViewModel extends BaseKolibreeServiceViewModel {
  private static final String TAG = otaTagFor(OtaUpdateViewModel.class);

  private static final int MIN_BATTERY_PERCENT_FOR_SAFE_OTA = 10;
  @VisibleForTesting final ReplaySubject<KLTBConnection> connectionSubject = ReplaySubject.create();
  private final BehaviorRelay<OtaUpdateViewState> viewStateRelay = BehaviorRelay.create();
  private final Observable<OtaUpdateViewState> viewModelObservable = viewStateRelay.hide();

  private final BehaviorRelay<Boolean> canDisconnectFromServiceRelay =
      BehaviorRelay.createDefault(true);

  @VisibleForTesting CheckOtaUpdatePrerequisitesUseCase checkOtaUpdatePrerequisitesUseCase;

  private final boolean isMandatoryUpdate;
  private final String macAddress;
  private final Resources resources;
  private final OtaUpdater otaUpdater;
  @VisibleForTesting final Boolean isRechargeable;
  @VisibleForTesting final OtaUpdateViewState viewState;

  @VisibleForTesting UpdateStatus updateStatus = NOT_STARTED;

  @VisibleForTesting boolean isWaitingForConnection = false;

  @VisibleForTesting GruwareData gruwareData;
  @VisibleForTesting KLTBConnection connection;

  private CompositeDisposable disposables = new CompositeDisposable();

  public OtaUpdateViewModel(
      Resources translationRes,
      @NonNull ServiceProvider serviceProvider,
      String macAddress,
      @NonNull ToothbrushModel toothbrushModel,
      boolean mandatoryUpdate,
      CheckOtaUpdatePrerequisitesUseCase checkOtaUpdatePrerequisitesUseCase,
      OtaUpdater otaUpdater) {
    super(serviceProvider);

    this.resources = translationRes;
    this.isMandatoryUpdate = mandatoryUpdate;
    this.macAddress = macAddress;
    this.isRechargeable = toothbrushModel.isRechargeable();
    this.checkOtaUpdatePrerequisitesUseCase = checkOtaUpdatePrerequisitesUseCase;
    this.viewState = OtaUpdateViewState.init(isMandatoryUpdate, isRechargeable);
    this.otaUpdater = otaUpdater;
  }

  @Override
  protected void onCleared() {
    super.onCleared();

    disposables.dispose();
  }

  @NonNull
  public Observable<OtaUpdateViewState> viewStateObservable() {
    return viewModelObservable.startWith(initialViewSTate());
  }

  private OtaUpdateViewState initialViewSTate() {
    return viewState.empty();
  }

  @Override
  protected Observable<Boolean> canDisconnectFromService() {
    return canDisconnectFromServiceRelay;
  }

  @Override
  protected void onKolibreeServiceConnected(@NonNull KolibreeService service) {
    super.onKolibreeServiceConnected(service);

    if (updateStatus == UpdateStatus.NOT_STARTED) {
      onReceivedConnectionFromService(service.getConnection(macAddress));
    }
  }

  @VisibleForTesting
  void onReceivedConnectionFromService(KLTBConnection connection) {
    if (isValidConnection(connection)) {
      connectionSubject.onNext(connection);
      connectionSubject.onComplete();
    } else {
      viewStateRelay.accept(
          viewState.withOtaError(getString(R.string.popup_toothbrush_unavailable_message)));
    }
  }

  /**
   * Check whether OTA update has blockers
   *
   * @param connection
   * @return true - blocked; false - not blocked
   */
  @VisibleForTesting
  Single<Boolean> checkBlockersSingle(@NonNull KLTBConnection connection) {
    return checkOtaUpdatePrerequisitesUseCase
        .otaUpdateBlockersOnce(connection)
        .map(
            otaUpdateBlockers ->
                /*
                 This only catch the E2 not charging because the rest of the VM check the rest and we don't want
                 to maintain it anymore
                */
                otaUpdateBlockers.contains(OtaUpdateBlocker.NOT_CHARGING));
  }

  @VisibleForTesting
  boolean isValidConnection(KLTBConnection connection) {
    return connection != null && connection.state().getCurrent() == KLTBConnectionState.ACTIVE;
  }

  @SuppressWarnings("WeakerAccess") // used by BtTester
  public void onUserClickedActionButton() {
    if (updateStatus == NOT_STARTED) {
      onUserClickedUpgrade();
    } else if (updateStatus == COMPLETED) {
      Analytics.send(OtaUpdateAnalytics.popUpUpdateDone());
      onUserClickedBack();
    }
  }

  void onUserClickedCancel() {
    viewStateRelay.accept(viewState.withConfirmExit());
  }

  @SuppressWarnings("WeakerAccess") // used by BtTester
  public void onUserConfirmedExit() {
    updateStatus = CANCELED;

    viewStateRelay.accept(viewState.withUpdateFinished(OTA_ACTION_EXIT_CANCEL));
  }

  void onUserCanceledExit() {
    updateStatus = NOT_STARTED;

    viewStateRelay.accept(viewState.empty());
  }

  void onUserClickedBack() {
    OtaUpdateViewState newViewState = null;

    switch (updateStatus) {
      case NOT_STARTED:
      case ERROR:
        newViewState = viewState.withUpdateFinished(OTA_ACTION_EXIT_CANCEL);
        break;
      case COMPLETED:
        newViewState = viewState.withUpdateFinished(OTA_ACTION_EXIT_SUCCESS);
        break;
      default:
        break;
    }

    if (newViewState != null) {
      viewStateRelay.accept(newViewState);
    }
  }

  @VisibleForTesting
  void onUserClickedUpgrade() {
    if (shouldStartUpdate()) {
      addToDisposables(
          upgradeToothbrushOnConnectionAvailableObservable()
              .subscribe(this::publishUpdateEvent, this::publishError));
    }
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> upgradeToothbrushOnConnectionAvailableObservable() {
    return connectionSubject
        .subscribeOn(Schedulers.io())
        .doOnSubscribe(ignore -> isWaitingForConnection = true)
        .doFinally(() -> isWaitingForConnection = false)
        .switchMap(
            connection -> {
              this.connection = connection;
              return checkPrerequisiteAndStartObservable();
            });
  }

  Observable<OtaUpdateEvent> checkPrerequisiteAndStartObservable() {
    return checkBlockersSingle(connection)
        .doOnSubscribe(disposable -> publishCheckingPrerequisite())
        .doOnError(throwable -> publishCheckPrerequisiteComplete())
        .flatMapObservable(
            blocked -> {
              publishCheckPrerequisiteComplete();
              if (blocked) {
                return Observable.just(fromAction(OTA_UPDATE_BLOCKED_NOT_CHARGING));
              } else {
                return checkGruwareAndBatteryAndStartObservable();
              }
            });
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> checkGruwareAndBatteryAndStartObservable() {
    return Observable.defer(
            () -> {
              gruwareData = (GruwareData) connection.getTag();

              if (gruwareData == null) {
                return Observable.just(fromError(R.string.firmware_upgrade_ota_not_available));
              } else {
                return Observable.defer(
                        () -> {
                          if (connection.toothbrush().isRunningBootloader()) {
                            return updateToothbrushObservable();
                          } else {
                            return startUpdateIfToothbrushHasEnoughBattery();
                          }
                        })
                    .doOnSubscribe(
                        ignore -> {
                          Analytics.send(OtaUpdateAnalytics.start());
                          publishUndefinedProgress(OTA_ACTION_REBOOTING);
                        });
              }
            })
        .doOnComplete(
            () -> {
              publishSuccessfulCompletion();
            });
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> startUpdateIfToothbrushHasEnoughBattery() {
    return hasEnoughBattery(connection)
        .subscribeOn(Schedulers.io())
        .flatMapObservable(
            (Function<Boolean, ObservableSource<OtaUpdateEvent>>)
                enoughBattery -> {
                  if (enoughBattery) {
                    return updateToothbrushObservable();
                  } else {
                    return Observable.just(fromError(R.string.ota_low_battery_subtitle));
                  }
                });
  }

  // TODO move this to CheckOtaUpdatePrerequisitesUseCase
  // https://kolibree.atlassian.net/browse/KLTB002-10611
  @VisibleForTesting
  Single<Boolean> hasEnoughBattery(@NonNull KLTBConnection connection) {
    if (connection.toothbrush().battery().getUsesDiscreteLevels()) {
      return connection
          .toothbrush()
          .battery()
          .getDiscreteBatteryLevel()
          .map(discreteLevelIsEnoughForOta());
    } else {
      return connection
          .toothbrush()
          .battery()
          .getBatteryLevel()
          .map(percentageLevelIsEnoughForOta());
    }
  }

  // TODO move this to CheckOtaUpdatePrerequisitesUseCase
  // https://kolibree.atlassian.net/browse/KLTB002-10611
  @VisibleForTesting
  Function<DiscreteBatteryLevel, Boolean> discreteLevelIsEnoughForOta() {
    return level ->
        level == DiscreteBatteryLevel.BATTERY_6_MONTHS
            || level == DiscreteBatteryLevel.BATTERY_3_MONTHS
            || level == DiscreteBatteryLevel.BATTERY_FEW_WEEKS;
  }

  // TODO move this to CheckOtaUpdatePrerequisitesUseCase
  // https://kolibree.atlassian.net/browse/KLTB002-10611
  @VisibleForTesting
  Function<Integer, Boolean> percentageLevelIsEnoughForOta() {
    return level -> level > MIN_BATTERY_PERCENT_FOR_SAFE_OTA;
  }

  @VisibleForTesting
  boolean shouldStartUpdate() {
    return updateStatus == NOT_STARTED && !waitingForConnection();
  }

  private boolean waitingForConnection() {
    return isWaitingForConnection;
  }

  @VisibleForTesting
  String getString(@StringRes int stringRes) {
    return resources.getString(stringRes);
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> updateToothbrushObservable() {
    return otaUpdater.updateToothbrushObservable(
        connection,
        Completable.defer(
            () -> {
              Timber.tag(TAG).d("Status is %s", updateStatus);
              updateStatus = IN_PROGRESS;
              preventKolibreeServiceStop();
              return Completable.complete();
            }));
  }

  @VisibleForTesting
  void preventKolibreeServiceStop() {
    canDisconnectFromServiceRelay.accept(false);
  }

  @VisibleForTesting
  void allowKolibreeServiceStop() {
    canDisconnectFromServiceRelay.accept(true);
  }

  @VisibleForTesting
  void publishUpdateEvent(@NonNull OtaUpdateEvent event) {
    Timber.tag(TAG).i("New update event received: %s", event);
    switch (event.action()) {
      case OTA_UPDATE_COMPLETED:
        publishSuccessfulCompletion();
        break;
      case OTA_UPDATE_ERROR:
        @StringRes Integer errorResId = event.getErrorMessageId();
        if (errorResId == null) {
          errorResId = R.string.firmware_upgrade_error;
        }

        publishError(getString(errorResId));
        break;
      case OTA_UPDATE_INSTALLING:
        publishProgress(event.progress());
        break;
      case OTA_UPDATE_REBOOTING:
        publishRebooting();
        break;
      case OTA_UPDATE_BLOCKED_NOT_CHARGING:
        publishBlockedNotCharging();
        break;
      default:
        break;
    }
  }

  @VisibleForTesting
  void publishCheckingPrerequisite() {
    viewStateRelay.accept(viewState.checkingPrerequisite());
  }

  @VisibleForTesting
  void publishCheckPrerequisiteComplete() {
    viewStateRelay.accept(viewState.checkPrerequisiteComplete());
  }

  @VisibleForTesting
  void publishBlockedNotCharging() {
    Analytics.send(OtaUpdateAnalytics.blockedNotCharging());
    viewStateRelay.accept(viewState.blockedNotCharging());
  }

  @VisibleForTesting
  void publishProgress(int percent) {
    Timber.tag(TAG).d("OTA progress %d%%", percent);
    viewStateRelay.accept(viewState.withProgress(OTA_ACTION_INSTALLING, percent));
  }

  @VisibleForTesting
  void publishError(@NonNull Throwable error) {
    Timber.tag(TAG).e(error, "OTA process encountered issue");
    publishError(getString(R.string.firmware_upgrade_error));
  }

  void publishError(@NonNull String errorMessage) {
    Analytics.send(OtaUpdateAnalytics.failure(errorMessage));

    allowKolibreeServiceStop();

    updateStatus = ERROR;

    Timber.tag(TAG).i("Update Failed.");
    viewStateRelay.accept(viewState.withOtaError(errorMessage));
  }

  @VisibleForTesting
  void publishRebooting() {
    publishUndefinedProgress(OTA_ACTION_REBOOTING);
  }

  @VisibleForTesting
  void publishUndefinedProgress(@OtaActionId int otaActionId) {
    Timber.tag(TAG).i("Update progress, ID %d", otaActionId);
    viewStateRelay.accept(viewState.withUndefinedProgress(otaActionId));
  }

  @VisibleForTesting
  void publishSuccessfulCompletion() {
    Analytics.send(OtaUpdateAnalytics.success());

    allowKolibreeServiceStop();

    updateStatus = COMPLETED;

    if (connection != null) {
      connection.setTag(null);
    }

    Timber.tag(TAG).i("Update completed successfully.");
    viewStateRelay.accept(viewState.withUpdateCompleted());
  }

  enum UpdateStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ERROR,
    COMPLETED,
    CANCELED
  }

  @VisibleForApp
  public static class Factory implements ViewModelProvider.Factory {

    private final ServiceProvider serviceProvider;

    private final boolean isMandatoryUpdate;

    private final String toothbrushMac;
    private final ToothbrushModel toothbrushModel;
    private final Context context;
    private final CheckOtaUpdatePrerequisitesUseCase checkOtaUpdatePrerequisitesUseCase;
    private final OtaUpdater otaUpdater;

    @Inject
    Factory(
        Context context,
        ServiceProvider serviceProvider,
        String toothbrushMac,
        ToothbrushModel toothbrushModel,
        boolean isMandatoryUpdate,
        CheckOtaUpdatePrerequisitesUseCase checkOtaUpdatePrerequisitesUseCase,
        OtaUpdater otaUpdater) {
      this.serviceProvider = serviceProvider;
      this.toothbrushMac = toothbrushMac;
      this.toothbrushModel = toothbrushModel;
      this.isMandatoryUpdate = isMandatoryUpdate;
      this.context = context;
      this.checkOtaUpdatePrerequisitesUseCase = checkOtaUpdatePrerequisitesUseCase;
      this.otaUpdater = otaUpdater;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public OtaUpdateViewModel create(@NonNull Class ignored) {
      return new OtaUpdateViewModel(
          new TranslationContext(context).getResources(),
          serviceProvider,
          toothbrushMac,
          toothbrushModel,
          isMandatoryUpdate,
          checkOtaUpdatePrerequisitesUseCase,
          otaUpdater);
    }
  }
}
