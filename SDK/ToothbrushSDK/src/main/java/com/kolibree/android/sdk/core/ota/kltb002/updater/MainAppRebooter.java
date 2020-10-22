/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.TimberTagKt.otaTagFor;
import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.core.InternalKLTBConnection;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.error.ConnectionEstablishException;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executors;
import timber.log.Timber;

/** Created by miguelaragues on 3/5/18. */
class MainAppRebooter {

  @VisibleForTesting static final int DISCONNECTION_INTERVAL_SECONDS = 2;
  private static final String TAG = otaTagFor(MainAppRebooter.class);

  /**
   * Disconnect from the toothbrush and wait for it to be rebooted to main application
   *
   * <p>If the connection is not running bootloader, it returns a completed Completable
   *
   * <p>The reconnection is attempted until the connection is no longer in bootloader. Users of this
   * method should explicitly set a timeout
   */
  Completable rebootToMainApp(InternalKLTBConnection connection, BleDriver driver) {
    return Completable.defer(
        () -> {
          Timber.tag(TAG).d("Rebooting main app");
          if (!driver.isRunningBootloader()) {
            Timber.tag(TAG).v("Main app is already running.");
            return Completable.complete();
          }

          return disconnectCompletable(connection)
              .delay(DISCONNECTION_INTERVAL_SECONDS, SECONDS, getTimeControlScheduler())
              .doOnSubscribe(s -> Timber.tag(TAG).d("Trying to reboot the main app"))
              .andThen(establishConnectionCompletable(connection, driver))
              .repeatUntil(() -> !driver.isRunningBootloader())
              .doOnComplete(() -> Timber.tag(TAG).d("Rebooting main app completed"))
              .doOnComplete(() -> connection.setState(KLTBConnectionState.ACTIVE));
        });
  }

  @VisibleForTesting
  Completable establishConnectionCompletable(InternalKLTBConnection connection, BleDriver driver) {
    return connection
        .establishCompletable()
        .retry(
            error -> {
              if (error instanceof ConnectionEstablishException) {
                Timber.tag(TAG).d(error, "Rebooting main app encountered an error");

                connection.disconnect();
                Thread.sleep(1000L);
                return true;
              }

              return false;
            });
  }

  @VisibleForTesting
  Completable disconnectCompletable(InternalKLTBConnection connection) {
    Timber.tag(TAG).d("disconnectCompletable disconnect");
    return Completable.fromAction(connection::disconnect);
  }

  @VisibleForTesting
  Scheduler getTimeControlScheduler() {
    return Schedulers.from(Executors.newSingleThreadExecutor());
  }
}
