/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.utils

import androidx.annotation.Keep
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

@Keep
class FakeNetworkChecker : NetworkChecker {
    private val hasConnectivityRelay = BehaviorRelay.createDefault(true)

    override fun hasConnectivity(): Boolean = hasConnectivityRelay.value!!

    fun setHasConnectivity(value: Boolean) = hasConnectivityRelay.accept(value)

    override fun connectivityStateObservable(): Observable<Boolean> = hasConnectivityRelay
}
