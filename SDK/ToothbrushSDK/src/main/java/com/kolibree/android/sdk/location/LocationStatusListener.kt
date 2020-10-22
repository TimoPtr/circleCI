/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.location.LocationAction
import com.kolibree.android.location.LocationStatus
import io.reactivex.Observable
import javax.inject.Inject
import timber.log.Timber

/**
 * Allows clients to subscribe to LocationAction changes
 */
@Keep
interface LocationStatusListener {
    fun locationActionStream(): Observable<LocationAction>
}

internal class LocationStatusListenerImpl @Inject internal constructor(
    context: Context,
    private val locationEnabledChecker: LocationStatus
) : LocationStatusListener {
    private val context = context.applicationContext

    private val locationActionRelay = PublishRelay.create<LocationAction>()

    @VisibleForTesting
    var locationActionStream: Observable<LocationAction>? = null

    /**
     * Returns a Observable of EnableLocationAction that will emit
     * - RequestPermission if Location permission isn't granted
     * - EnableLocation if Location is disabled
     * - NoLocationAction if no action is required to use Location
     *
     * The observable will emit a new LocationAction when the location status of the phone is updated
     *
     * The observable does not terminate
     */
    override fun locationActionStream(): Observable<LocationAction> {
        var localField: Observable<LocationAction>? = locationActionStream
        if (localField == null) {
            synchronized(this) {
                localField = locationActionStream
                if (localField == null) {
                    localField = locationActionRelay.hide()
                        .doOnSubscribe { listenToLocationStatus() }
                        .doFinally { unregisterLocationStatusListener() }
                        .doFinally {
                            synchronized(this) { locationActionStream = null }
                        }
                        .publish()
                        .refCount()

                    locationActionStream = localField
                }
            }
        }

        return localField!!
    }

    private val locationIntentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

    @VisibleForTesting
    val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            locationActionRelay.accept(locationEnabledChecker.getLocationAction())
        }
    }

    private fun listenToLocationStatus() {
        Timber.w("listenToLocationStatus %s", this)
        context.registerReceiver(locationReceiver, locationIntentFilter)
    }

    private fun unregisterLocationStatusListener() {
        Timber.w("unregisterLocationStatusListener %s", this)
        try {
            context.unregisterReceiver(locationReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
    }
}
