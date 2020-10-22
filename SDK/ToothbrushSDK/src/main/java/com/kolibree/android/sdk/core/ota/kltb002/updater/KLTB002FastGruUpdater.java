package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.TimberTagKt.otaTagFor;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromProgressiveAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.OtaWriterMonitorKt.monitorOtaWriteObservable;
import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.KolibreeAndroidSdk;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.KLTBConnectionProvider;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.error.DeviceNotConnectedException;
import com.kolibree.android.sdk.error.FailureReason;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

class KLTB002FastGruUpdater implements OtaUpdater {

  private static final String TAG = otaTagFor(KLTB002FastGruUpdater.class);
  private static final long DELAY_UNTIL_UPDATE_VERSION = 2;
  private static final long SECONDS_TO_CONFIRM_UPDATE = 30;

  private final FastGruWriter fastGruWriter;
  private final InternalKLTBConnection connection;
  private final BleDriver driver;
  private final KLTBConnectionProvider connectionProvider;
  private final long delayUntilUpdateVersion;

  KLTB002FastGruUpdater(
      InternalKLTBConnection connection,
      BleDriver driver,
      FastGruWriter fastGruWriter,
      KLTBConnectionProvider connectionProvider,
      long delayUntilUpdateVersion) {
    this.connection = connection;
    this.driver = driver;
    this.fastGruWriter = fastGruWriter;
    this.connectionProvider = connectionProvider;
    this.delayUntilUpdateVersion = delayUntilUpdateVersion;
  }

  static KLTB002FastGruUpdater create(
      InternalKLTBConnection connection, BleDriver driver, int attempt) {
    FastGruWriter fastGruWriter = FastGruWriter.create(driver, attempt);

    return new KLTB002FastGruUpdater(
        connection,
        driver,
        fastGruWriter,
        KolibreeAndroidSdk.getSdkComponent().kltbConnectionProvider(),
        DELAY_UNTIL_UPDATE_VERSION);
  }

  /** @param otaUpdate a valid, crc-checked OtaUpdate */
  @Override
  @NonNull
  public Observable<OtaUpdateEvent> update(@NonNull OtaUpdate otaUpdate) {
    return validateConnectionState()
        .andThen(otaWriterObservable(otaUpdate))
        .concatWith(
            Completable.defer(
                () ->
                    updateVersion(otaUpdate)
                        .delaySubscription(
                            delayUntilUpdateVersion, TimeUnit.SECONDS, getTimeControlScheduler())));
  }

  @VisibleForTesting
  Completable validateConnectionState() {
    return Completable.create(
        e -> {
          if (driver.isRunningBootloader()) {
            e.tryOnError(new IllegalStateException("Device can't be in bootloader to update GRU"));
          } else if (connection.state().getCurrent() != KLTBConnectionState.ACTIVE) {
            e.tryOnError(
                new DeviceNotConnectedException(
                    "Connection must be active. Was " + connection.state().getCurrent()));
          } else {
            connection.setState(KLTBConnectionState.OTA);
            e.onComplete();
          }
        });
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> otaWriterObservable(OtaUpdate otaUpdate) {
    return monitorOtaWriteObservable(connection, internalOtaWriterObservable(otaUpdate));
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> internalOtaWriterObservable(OtaUpdate otaUpdate) {
    return fastGruWriter
        .write(otaUpdate)
        .doOnComplete(() -> Timber.tag(TAG).i("fastGruWriter write completed"))
        .map(progress -> fromProgressiveAction(OTA_UPDATE_INSTALLING, progress))
        .doOnNext(event -> Timber.tag(TAG).i("GRU update progress: %s", event))
        .doOnComplete(() -> Timber.tag(TAG).i("GRU update confirmed and completed."));
  }

  @VisibleForTesting
  Completable updateVersion(@NonNull OtaUpdate update) {
    return connectionProvider
        .existingConnectionWithStates(
            connection.toothbrush().getMac(), acceptedStatesToUpdateVersion())
        .observeOn(Schedulers.io())
        .ignoreElement()
        .andThen(driver.reloadVersions())
        .timeout(
            SECONDS_TO_CONFIRM_UPDATE,
            SECONDS,
            getTimeControlScheduler(),
            Completable.error(new FailureReason("GRU version update timed out")))
        .doOnComplete(() -> connection.setGruDataUpdatedVersion(update.getVersion()))
        .doOnComplete(() -> Timber.tag(TAG).i("GRU version update completed."));
  }

  private List<KLTBConnectionState> acceptedStatesToUpdateVersion() {
    // for some reason, sometimes we are at active, others we stay at OTA
    return Arrays.asList(KLTBConnectionState.OTA, KLTBConnectionState.ACTIVE);
  }

  @VisibleForTesting
  Scheduler getTimeControlScheduler() {
    return Schedulers.from(Executors.newSingleThreadExecutor());
  }
}
