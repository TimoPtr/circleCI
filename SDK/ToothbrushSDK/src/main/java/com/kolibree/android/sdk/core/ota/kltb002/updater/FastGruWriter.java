/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executors;

/** Created by miguelaragues on 27/4/18. */
class FastGruWriter extends BaseFastOtaWriter {

  @VisibleForTesting static final byte COMMAND_ID_FAST_GRU_UPDATE = 0x03;

  static FastGruWriter create(BleDriver driver, int attempt) {
    Scheduler singleScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    return new FastGruWriter(driver, singleScheduler, attempt);
  }

  @VisibleForTesting
  FastGruWriter(BleDriver driver, Scheduler singleScheduler, int attempt) {
    super(driver, singleScheduler, attempt);
  }

  @Override
  protected Observable<Integer> validateUpdatePreconditionsObservable() {
    return Observable.empty();
  }

  @VisibleForTesting
  @Override
  protected byte getStartOTACommandId() {
    return COMMAND_ID_FAST_GRU_UPDATE;
  }
}
