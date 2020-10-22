package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.TimberTagKt.otaTagFor;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.commons.AvailableUpdate;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.KolibreeAndroidSdk;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.core.ota.ToothbrushUpdater;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdateFactory;
import com.kolibree.android.sdk.error.CriticalFailureException;
import com.kolibree.android.sdk.error.FailureReason;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import timber.log.Timber;

public class KLTB002ToothbrushUpdater implements ToothbrushUpdater {

  @VisibleForTesting static final int MAX_OTA_RECOVERY_ATTEMPTS = 20;
  @VisibleForTesting static final long GLOBAL_OTA_TIMEOUT_SECONDS = 60;
  @VisibleForTesting static final long RECONNECTION_TIMEOUT_SECONDS = 20;

  private static final String TAG = otaTagFor(KLTB002ToothbrushUpdater.class);
  @VisibleForTesting final AtomicInteger attemptCounter = new AtomicInteger();
  private final InternalKLTBConnection connection;
  private final IBluetoothUtils bluetoothUtils;
  private final OtaUpdateFactory otaUpdateFactory;
  private final OtaToolsFactory otaToolsFactory;
  private final BleDriver driver;
  private final ConnectionStateMonitor connectionStateMonitor;

  @VisibleForTesting
  KLTB002ToothbrushUpdater(
      InternalKLTBConnection connection,
      IBluetoothUtils bluetoothUtils,
      @NonNull BleDriver driver,
      OtaUpdateFactory otaUpdateFactory,
      OtaToolsFactory otaToolsFactory,
      ConnectionStateMonitor connectionStateMonitor) {
    this.connection = connection;
    this.bluetoothUtils = bluetoothUtils;
    this.driver = driver;
    this.otaUpdateFactory = otaUpdateFactory;
    this.otaToolsFactory = otaToolsFactory;
    this.connectionStateMonitor = connectionStateMonitor;
  }

  public static KLTB002ToothbrushUpdater create(
      @NonNull InternalKLTBConnection connection, @NonNull BleDriver driver) {
    OtaUpdateFactory otaUpdateFactory = new OtaUpdateFactory();
    OtaToolsFactory otaToolsFactory = new OtaToolsFactory();
    ConnectionStateMonitor connectionStateMonitor = new ConnectionStateMonitor(connection);

    return new KLTB002ToothbrushUpdater(
        connection,
        KolibreeAndroidSdk.getSdkComponent().bluetoothUtils(),
        driver,
        otaUpdateFactory,
        otaToolsFactory,
        connectionStateMonitor);
  }

  @NonNull
  @Override
  public Observable<OtaUpdateEvent> update(@NonNull AvailableUpdate availableUpdate) {

    Timber.tag(TAG).d("Preparing update, verification started");

    OtaUpdate otaUpdate;
    try {
      otaUpdate = otaUpdateFactory.create(availableUpdate, getModel());

      checkUpdateCompatibility(otaUpdate);

      otaUpdate.checkCRC();
    } catch (Exception e) {
      return Observable.error(e);
    }

    Timber.tag(TAG).d("Update verified, starting update");

    attemptCounter.set(0);
    return internalUpdate(otaUpdate);
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> internalUpdate(OtaUpdate otaUpdate) {
    return otaUpdater(otaUpdate, attemptCounter.get())
        .update(otaUpdate)
        .onErrorResumeNext(
            throwable -> {
              return tryToRecoverOta(otaUpdate, attemptCounter, throwable);
            })
        // This is last-resort solution for hanged OTAs
        .timeout(
            GLOBAL_OTA_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            getTimeControlScheduler(),
            Observable.error(new TimeoutException("Global OTA recovery timeout")))
        .doOnError(
            e -> {
              Timber.tag(TAG).e(e, "Error encountered during OTA, and we couldn't recover it!");
            })
        .doOnTerminate(
            () -> {
              if (connection.state().getCurrent() == KLTBConnectionState.OTA) {
                connection.setState(KLTBConnectionState.ACTIVE);
              }
            });
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> tryToRecoverOta(
      OtaUpdate otaUpdate, AtomicInteger attempt, Throwable reason) {

    int currentAttemptCounter = attempt.incrementAndGet();
    boolean retryAllowed = currentAttemptCounter < MAX_OTA_RECOVERY_ATTEMPTS;
    boolean recoverableException = !(reason instanceof CriticalFailureException);

    if (retryAllowed && recoverableException) {
      String reasonType = reason.getClass().getSimpleName();
      Timber.tag(TAG)
          .w(
              "We can recover this exception: %s(%s), attempt no. %d/%d",
              reasonType, reason.getMessage(), currentAttemptCounter, MAX_OTA_RECOVERY_ATTEMPTS);

      return waitForEnabledBluetooth()
          .andThen(Observable.defer(() -> recoverOta(otaUpdate, currentAttemptCounter)))
          .onErrorResumeNext(
              throwable -> {
                return tryToRecoverOta(otaUpdate, attempt, throwable);
              });
    } else {
      Timber.tag(TAG)
          .e(
              reason,
              "We cannot recover this exception! (retry counter: %d, recoverable exception: %b)",
              currentAttemptCounter,
              recoverableException);
      return Observable.error(reason);
    }
  }

  @VisibleForTesting
  Completable waitForEnabledBluetooth() {
    return bluetoothUtils
        .bluetoothStateObservable()
        .startWith(bluetoothUtils.isBluetoothEnabled())
        .doOnSubscribe(state -> Timber.tag(TAG).d("Waiting for new Bluetooth state"))
        .doOnNext(state -> Timber.tag(TAG).v("Bluetooth state: %b", state))
        .filter(state -> state /*== true*/)
        .take(1)
        .ignoreElements()
        .doOnComplete(() -> Timber.tag(TAG).d("Bluetooth enabled"));
  }

  @VisibleForTesting
  Observable<OtaUpdateEvent> recoverOta(OtaUpdate otaUpdate, int attempt) {
    return connection
        .reconnectCompletable()
        .timeout(
            RECONNECTION_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            getTimeControlScheduler(),
            Completable.error(
                new FailureReason("Reconnection timeout during OTA recovery attempt")))
        .doOnSubscribe(s -> Timber.tag(TAG).d("Attempting reconnect..."))
        .doOnComplete(
            () -> Timber.tag(TAG).d("Reconnect triggered, waiting for active connection..."))
        .andThen(
            connectionStateMonitor
                .waitForActiveConnection()
                .timeout(
                    RECONNECTION_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS,
                    getTimeControlScheduler(),
                    Completable.error(
                        new FailureReason("Reconnection timeout during OTA recovery attempt"))))
        .doOnComplete(() -> Timber.tag(TAG).d("Connection is active, proceeding OTA recovery..."))
        .andThen(Observable.defer(() -> otaUpdater(otaUpdate, attempt).update(otaUpdate)))
        .doOnComplete(() -> Timber.tag(TAG).d("OTA recovered"));
  }

  @NonNull
  @VisibleForTesting
  OtaUpdater otaUpdater(OtaUpdate otaUpdate, int attempt) {
    return otaToolsFactory.createOtaUpdater(connection, driver, otaUpdate, attempt);
  }

  /**
   * Check the compatibility between the update and the toothbrush
   *
   * @throws IllegalStateException if this update is not compatible
   */
  @VisibleForTesting
  void checkUpdateCompatibility(OtaUpdate otaUpdate) throws IllegalStateException {
    if (!otaUpdate.isCompatible(getModel())) {
      throw new IllegalStateException(
          "This update is not compatible with " + getModel().getCommercialName() + " toothbrushes");
    } else if (!otaUpdate.isCompatible(getFirmwareVersion())) {
      throw new IllegalStateException(
          "This update is not compatible with firmware version " + getFirmwareVersion().toString());
    }
  }

  @VisibleForTesting
  ToothbrushModel getModel() {
    return connection.toothbrush().getModel();
  }

  @VisibleForTesting
  SoftwareVersion getFirmwareVersion() {
    return connection.toothbrush().getFirmwareVersion();
  }

  @VisibleForTesting
  HardwareVersion getHardwareVersion() {
    return connection.toothbrush().getHardwareVersion();
  }

  @VisibleForTesting
  Scheduler getTimeControlScheduler() {
    return Schedulers.from(Executors.newSingleThreadExecutor());
  }

  /** Not supported */
  @Override
  public boolean isUpdateInProgress() {
    return false;
  }
}
