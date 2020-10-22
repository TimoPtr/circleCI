/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState;
import com.kolibree.android.sdk.core.binary.Bitmask;
import com.kolibree.android.sdk.core.driver.KLTBDriverListener;
import com.kolibree.android.sdk.core.driver.VibratorMode;
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager;
import com.kolibree.android.sdk.error.CommandNotSupportedException;
import com.kolibree.android.sdk.error.FailureReason;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import org.jetbrains.annotations.NotNull;

/** Ara and Connect E1 driver implementation */
public class AraDriver extends KolibreeBleDriver {
  static final int CALIBRATION_DATA_SIZE = 12;

  @VisibleForTesting static final long SLOW_MODE_ADVERTISING_DELAY = 1285L;

  public AraDriver(Context context, @NonNull String mac, @NonNull KLTBDriverListener listener) {
    super(context, mac, listener);
  }

  @VisibleForTesting
  AraDriver(
      KLNordicBleManager bleManager,
      @NonNull KLTBDriverListener listener,
      @NonNull Scheduler bluetoothScheduler,
      @NonNull String mac,
      CharacteristicNotificationStreamer notificationCaster,
      @NonNull Scheduler notifyListenerScheduler) {
    super(
        bleManager, listener, bluetoothScheduler, mac, notificationCaster, notifyListenerScheduler);
  }

  @Override
  protected int calibrationDataSize() {
    return CALIBRATION_DATA_SIZE;
  }

  @Override
  ToothbrushModel toothbrushModel() {
    return ToothbrushModel.ARA;
  }

  @Override
  protected byte[] getSensorControlPayload(
      Bitmask streamingBitmask, Bitmask detectionBitmask, boolean handedness) {
    // This is a quick fix about this issue https://jira.kolibree.com/browse/KLTB002-6613
    // Where the Ara/E1 send the euler angle at 50Hz (by default) which cause issue when
    // we are in slow connection to avoid this we ask for 2Hz because we are not able to
    // disable it
    detectionBitmask.set(3, true); // Euler angle

    return new byte[] {
      streamingBitmask.get(),
      detectionBitmask.get(),
      50, // Default value
      (byte) (handedness ? 0x01 : 0x00),
      1 // Freq of euler angle
    };
  }

  @Override
  protected Completable notifyConnectionEstablishedCompletable() {
    return super.notifyConnectionEstablishedCompletable()
        .andThen(Completable.fromAction(this::forceSlowModeAdvertisingInterval));
  }

  @Override
  public boolean supportsBrushingEventsPolling() {
    return true;
  }

  // https://kolibree.atlassian.net/browse/KLTB002-8463
  @VisibleForTesting
  void forceSlowModeAdvertisingInterval() throws FailureReason {
    if (!isRunningBootloader()) {
      try {
        setDeviceParameter(
            ParameterSet.setAdvertisingIntervalsPayload(0L, SLOW_MODE_ADVERTISING_DELAY));
      } catch (Exception e) {
        throw new FailureReason(e);
      }
    }
  }

  @NonNull
  @Override
  public Completable setVibratorMode(@NonNull VibratorMode vibratorMode) {
    Completable maybeStopVibrationCompletable;

    /*
    For E1/Ara, We must send monitorCurrentBrushing before STOP_AND_HALT_RECORDING

    See https://kolibree.atlassian.net/browse/KLTB002-8388
     */
    if (vibratorMode == VibratorMode.STOP_AND_HALT_RECORDING) {
      maybeStopVibrationCompletable = monitorCurrentBrushing();
    } else {
      maybeStopVibrationCompletable = Completable.complete();
    }

    return maybeStopVibrationCompletable.andThen(super.setVibratorMode(vibratorMode));
  }

  @SuppressLint("MissingSuperCall")
  @Override
  boolean supportsReadingBootloader() {
    return false;
  }

  /*
  For E1/Ara, we must disable the multiUserMode which is enable by default

  See https://kolibree.atlassian.net/browse/KLTB002-9484
   */
  @Override
  public void disableMultiUserMode() throws FailureReason {
    super.disableMultiUserMode();
    try {
      setDeviceParameter(ParameterSet.disableMultiUserModePayload());
    } catch (Exception e) {
      throw new FailureReason(e);
    }
  }

  @NotNull
  @Override
  public Flowable<OverpressureState> overpressureStateFlowable() {
    return Flowable.error(new CommandNotSupportedException());
  }
}
