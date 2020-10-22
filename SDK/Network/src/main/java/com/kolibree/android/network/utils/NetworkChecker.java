/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.network.utils;

import androidx.annotation.Keep;
import io.reactivex.Observable;

/** Created by miguelaragues on 22/12/17. */
@Keep
public interface NetworkChecker {

  boolean hasConnectivity();

  /** @return an Observable with the latest known connectivity state */
  Observable<Boolean> connectivityStateObservable();
}
