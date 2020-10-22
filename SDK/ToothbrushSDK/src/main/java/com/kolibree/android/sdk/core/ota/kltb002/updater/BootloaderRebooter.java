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
import com.kolibree.android.sdk.core.binary.PayloadWriter;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import com.kolibree.android.sdk.error.FailureReason;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executors;
import timber.log.Timber;

/** Created by miguelaragues on 27/4/18. */
class BootloaderRebooter {

  @VisibleForTesting static final int DISCONNECTION_INTERVAL_SECONDS = 2;
  @VisibleForTesting static final byte COMMAND_ID_REBOOT_TO_BOOTLOADER = 0x05;
  private static final String TAG = otaTagFor(BootloaderRebooter.class);

  /**
   * Disconnect from the toothbrush and wait for it to be rebooted in bootloader mode
   *
   * <p>If the connection is already in bootloader, it returns a completed Completable
   */
  Completable rebootToBootloader(InternalKLTBConnection connection, BleDriver driver) {
    return Completable.defer(
        () -> {
          Timber.tag(TAG).d("Rebooting bootloader");

          if (driver.isRunningBootloader()) {
            Timber.tag(TAG).v("Bootloader is already running.");
            return Completable.complete();
          }
          return Completable.defer(() -> sendRebootToBootloader(driver))
              .doOnSubscribe(s -> Timber.tag(TAG).d("Rebooting bootloader started"))
              .andThen(Completable.defer(() -> disconnectCompletable(connection)))
              .delay(DISCONNECTION_INTERVAL_SECONDS, SECONDS, getTimeControlScheduler())
              .andThen(Completable.defer(() -> establishConnectionCompletable(connection, driver)))
              .doOnComplete(() -> Timber.tag(TAG).d("Rebooting bootloader completed"));
        });
  }

  /**
   * Attempts to establish a connection to <code>connection</code> and only completes once the
   * connection has been established and it's in bootloader mode.
   *
   * <p>If after 3 connection attempts the connection is not in bootloader mode, we emit an
   * UnableToRebootToBootloaderException
   *
   * @return a Completable that'll complete once rebooted in bootloader mode, or an
   *     UnableToRebootToBootloaderException
   */
  @VisibleForTesting
  Completable establishConnectionCompletable(InternalKLTBConnection connection, BleDriver driver) {
    if (connection.state().getCurrent() == KLTBConnectionState.ACTIVE
        && connection.toothbrush().isRunningBootloader()) {
      /*
      Sometimes we are too fast reconnecting and the toothbrush is already ready when we reach this point
       */
      Timber.tag(TAG).i("connection is already active and in bootloader");
      return Completable.complete();
    }

    Timber.tag(TAG).i("Bootloader invoking establishCompletable");
    return connection
        .establishCompletable()
        .repeatUntil(repeatUntilBootloaderSupplier(connection, driver));
  }

  @VisibleForTesting
  Completable disconnectCompletable(InternalKLTBConnection connection) {
    Timber.tag(TAG).d("Disconnecting after bootloader reboot.");
    return Completable.fromAction(connection::disconnect);
  }

  @VisibleForTesting
  BooleanSupplier repeatUntilBootloaderSupplier(
      InternalKLTBConnection connection, BleDriver driver) {
    return new BooleanSupplier() {
      private static final int MAX_RETRIES = 2;
      int counter = 0;

      @Override
      public boolean getAsBoolean() throws Exception {
        if (driver.isRunningBootloader()) {
          Timber.tag(TAG).i("Bootloader is running after reboot!");
          return true;
        }

        if (counter++ == MAX_RETRIES) {
          Timber.tag(TAG).e("Max reboot retries reached!");
          throw new UnableToRebootToBootloaderException();
        }

        connection.disconnect();

        return false;
      }
    };
  }

  Completable sendRebootToBootloader(BleDriver driver) {
    byte[] rebootBootloaderPayload =
        new PayloadWriter(1).writeByte(COMMAND_ID_REBOOT_TO_BOOTLOADER).getBytes();

    /*
    There's a bug on the firmware that it reboots to bootloader before it has time to confirm it,
    so we need to ignore Timeout exceptions
     */
    Timber.tag(TAG).v("Rebooting to bootloader.");
    return driver.writeOtaUpdateStartCharacteristic(rebootBootloaderPayload).onErrorComplete();
  }

  @VisibleForTesting
  Scheduler getTimeControlScheduler() {
    return Schedulers.from(Executors.newSingleThreadExecutor());
  }

  static class UnableToRebootToBootloaderException extends FailureReason {

    private UnableToRebootToBootloaderException() {
      super("Unable to reboot to bootloader.");
    }
  }
}
