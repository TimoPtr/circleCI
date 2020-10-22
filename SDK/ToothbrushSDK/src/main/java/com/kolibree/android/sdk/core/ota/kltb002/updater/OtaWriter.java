/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater;

import androidx.annotation.NonNull;
import com.kolibree.android.sdk.core.ota.kltb002.updates.OtaUpdate;
import io.reactivex.Observable;

/** Created by miguelaragues on 27/4/18. */
interface OtaWriter {

  /**
   * Writes the OTA update
   *
   * @param otaUpdate an instance containing the information needed to write the update
   * @return an Observable that emits the progress in percent value
   */
  @NonNull
  Observable<Integer> write(@NonNull OtaUpdate otaUpdate);
}
