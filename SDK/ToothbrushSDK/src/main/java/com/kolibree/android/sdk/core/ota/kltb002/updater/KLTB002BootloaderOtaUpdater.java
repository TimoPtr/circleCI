package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.TimberTagKt.otaTagFor;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.fromProgressiveAction;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_COMPLETED;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_INSTALLING;
import static com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEventKt.OTA_UPDATE_REBOOTING;
import static com.kolibree.android.sdk.core.ota.kltb002.updater.OtaWriterMonitorKt.monitorOtaWriteObservable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.extensions.DateExtensionsKt;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.error.DeviceNotConnectedException;
import com.kolibree.android.sdk.error.FailureReason;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executors;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import timber.log.Timber;

class KLTB002BootloaderOtaUpdater implements OtaUpdater {

  @VisibleForTesting static final int CONFIRM_UPDATE_CODE = 0x12345678;

  private static final String TAG = otaTagFor(KLTB002BootloaderOtaUpdater.class);

  private static final int SECONDS_TO_CONFIRM_UPDATE = 30;
  private static final int SECONDS_TO_REBOOT = 30;

  private final InternalKLTBConnection connection;
  private final BleDriver driver;
  private final int attempt;

  KLTB002BootloaderOtaUpdater(InternalKLTBConnection connection, BleDriver bleDriver, int attempt) {
    this.connection = connection;
    this.driver = bleDriver;
    this.attempt = attempt;
  }

  @NonNull
  @Override
  public Observable<OtaUpdateEvent> update(@NonNull OtaUpdate otaUpdate) {
    OtaStatsLogger statsLogger = createStatsLogger();

    return checkConnectionIsActive()
        .doOnComplete(() -> onOtaStart(statsLogger))
        .andThen(rebootToBootloaderObservable())
        .doOnSubscribe(ignore -> statsLogger.onBootloaderRebootStart())
        .doOnComplete(statsLogger::onBootloaderRebootCompleted)
        .doOnComplete(() -> connection.setState(KLTBConnectionState.OTA))
        .concatWith(monitorOtaWriteObservable(connection, otaWriterObservable(otaUpdate)))
        .doOnComplete(() -> Timber.tag(TAG).i("Chunks write completed"))
        .concatWith(rebootToMainAppObservable())
        .doOnComplete(() -> Timber.tag(TAG).i("Reboot to main app completed"))
        .doOnSubscribe(ignore -> statsLogger.onMainAppRebootStart())
        .concatWith(confirmUpdateObservable())
        .doOnNext(event -> Timber.tag(TAG).i("Bootloader update progress: %s", event))
        .doOnComplete(() -> Timber.tag(TAG).i("Bootloader update confirmed and completed."))
        .doOnComplete(statsLogger::onMainAppRebootCompleted)
        .doOnComplete(statsLogger::logStats)
        .doOnTerminate(this::close);
  }

  @VisibleForTesting
  OtaStatsLogger createStatsLogger() {
    return new OtaStatsLogger();
  }

  @VisibleForTesting
  void onOtaStart(OtaStatsLogger statsLogger) {
    statsLogger.onUpdateStart();

    connection.setState(KLTBConnectionState.OTA);
  }

  @VisibleForTesting
  Completable checkConnectionIsActive() {
    return Completable.create(
        e -> {
          if (connection.state().getCurrent() != KLTBConnectionState.ACTIVE) {
            DeviceNotConnectedException ex =
                new DeviceNotConnectedException(
                    "Connection must be active. Was " + connection.state().getCurrent());
            Timber.tag(TAG).e(ex, "checkConnectionIsActive failed");
            e.tryOnError(ex);
          } else {
            Timber.tag(TAG).v("Connection is active, let's proceed");
            e.onComplete();
          }
        });
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> rebootToBootloaderObservable() {
    Timber.tag(TAG).v("Rebooting to bootloader.");
    return bootloaderRebooter()
        .rebootToBootloader(connection, driver)
        .timeout(
            SECONDS_TO_REBOOT,
            SECONDS,
            getTimeControlScheduler(),
            Completable.error(new FailureReason("Reboot to bootloader timed out")))
        .startWith(Observable.just(fromAction(OTA_UPDATE_REBOOTING)));
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> otaWriterObservable(OtaUpdate otaUpdate) {
    return Observable.defer(
        () ->
            otaWriter(otaUpdate)
                .write(otaUpdate)
                .map(progress -> fromProgressiveAction(OTA_UPDATE_INSTALLING, progress)));
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> rebootToMainAppObservable() {
    return mainAppRebooter()
        .rebootToMainApp(connection, driver)
        .timeout(
            SECONDS_TO_REBOOT,
            SECONDS,
            getTimeControlScheduler(),
            Completable.error(new FailureReason("Reboot to main app timed out")))
        .startWith(Observable.just(fromAction(OTA_UPDATE_REBOOTING)));
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> confirmUpdateObservable() {
    byte[] payload = new PayloadWriter(4).writeInt32(CONFIRM_UPDATE_CODE).getBytes();
    return Observable.defer(
        () ->
            driver
                .writeOtaUpdateValidateCharacteristic(payload)
                .timeout(
                    SECONDS_TO_CONFIRM_UPDATE,
                    SECONDS,
                    getTimeControlScheduler(),
                    Completable.error(
                        new FailureReason("Confirmation of bootloader update timed out")))
                .doOnSubscribe(
                    otaUpdateEvent -> Timber.tag(TAG).d("Confirming bootloader update..."))
                .andThen(
                    Observable.just(fromAction(OTA_UPDATE_COMPLETED))
                        .doOnNext(
                            otaUpdateEvent -> Timber.tag(TAG).d("Bootloader update confirmed!."))));
  }

  @VisibleForTesting
  OtaWriter otaWriter(OtaUpdate otaUpdate) {
    return new OtaToolsFactory().createOtaWriter(connection, driver, otaUpdate, attempt);
  }

  @VisibleForTesting
  BootloaderRebooter bootloaderRebooter() {
    return new BootloaderRebooter();
  }

  @VisibleForTesting
  MainAppRebooter mainAppRebooter() {
    return new MainAppRebooter();
  }

  @VisibleForTesting
  void close() {
    maybeFlagConnectionAsActive();
  }

  @VisibleForTesting
  void maybeFlagConnectionAsActive() {
    if (connection.state().getCurrent() == KLTBConnectionState.OTA) {
      connection.setState(KLTBConnectionState.ACTIVE);
    }
  }

  @VisibleForTesting
  Scheduler getTimeControlScheduler() {
    return Schedulers.from(Executors.newSingleThreadExecutor());
  }

  @VisibleForTesting
  static class OtaStatsLogger {

    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private long updateStartTimestamp;
    private long rebootBootloaderStartTimestamp;
    private long rebootBootloaderCompletedTimestamp;
    private long rebootMainAppStartTimestamp;
    private long rebootMainAppCompletedTimestamp;

    @VisibleForTesting
    void onUpdateStart() {
      if (updateStartTimestamp == 0) {
        updateStartTimestamp = currentTime();
      }
    }

    @VisibleForTesting
    void onBootloaderRebootStart() {
      rebootBootloaderStartTimestamp = currentTime();
    }

    @VisibleForTesting
    void onBootloaderRebootCompleted() {
      rebootBootloaderCompletedTimestamp = currentTime();
    }

    @VisibleForTesting
    void onMainAppRebootStart() {
      rebootMainAppStartTimestamp = currentTime();
    }

    @VisibleForTesting
    void onMainAppRebootCompleted() {
      rebootMainAppCompletedTimestamp = currentTime();
    }

    @VisibleForTesting
    void logStats() {
      Timber.d(
          "$$$ Update started at %s, completed at %s (%s seconds)",
          printTimestamp(updateStartTimestamp),
          printTimestamp(rebootMainAppCompletedTimestamp),
          MILLISECONDS.toSeconds(rebootMainAppCompletedTimestamp - updateStartTimestamp));

      Timber.d(
          "$$$ Rebooting to bootloader took %s seconds",
          MILLISECONDS.toSeconds(
              rebootBootloaderCompletedTimestamp - rebootBootloaderStartTimestamp));

      Timber.d(
          "$$$ Rebooting to main app and confirming update took %s seconds",
          MILLISECONDS.toSeconds(rebootMainAppCompletedTimestamp - rebootMainAppStartTimestamp));
    }

    private String printTimestamp(long timestamp) {
      return TIME_FORMATTER.format(
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
    }

    private long currentTime() {
      return DateExtensionsKt.toEpochMilli(TrustedClock.getNowZonedDateTime());
    }

    public void reset() {
      updateStartTimestamp = 0;
      rebootBootloaderStartTimestamp = 0;
      rebootBootloaderCompletedTimestamp = 0;
      rebootMainAppStartTimestamp = 0;
      rebootMainAppCompletedTimestamp = 0;
    }
  }
}
