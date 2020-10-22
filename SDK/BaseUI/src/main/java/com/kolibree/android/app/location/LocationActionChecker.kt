/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.location

import androidx.annotation.Keep
import com.kolibree.android.location.LocationAction
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.location.NoAction
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import io.reactivex.Single
import javax.inject.Inject

/**
 * Analyses if the toothbrush connections require a LocationAction
 */
@Keep
interface LocationActionChecker {
    /**
     * Returns a Single that will emit a EnableLocationAction for the KLTBConnections in ServiceProvider
     *
     * Given a KolibreeService with connections pending to be established, ask if there's any action
     * required to enable location
     *
     * <p>Expected actions
     * <ul>
     * <li>- NoLocationAction if there aren't KLTBConnections or we don't need to
     * establish a connection to any of them</li>
     * <li>- RequestPermission if there's at least one KLTBConnection we need to pair with and Location permission
     * isn't granted </li>
     * <li>- EnableLocation if there's at least one KLTBConnection we need to pair with and Location is disabled</li>
     * </ul>
     */
    fun enableLocationActionSingle(): Single<LocationAction>
}

internal class LocationActionCheckerImpl @Inject constructor(
    private val serviceProvider: ServiceProvider,
    private val locationEnabledChecker: LocationStatus
) : LocationActionChecker {
    override fun enableLocationActionSingle(): Single<LocationAction> {
        return serviceProvider.connectOnce()
            .map(this::locationActionForConnections)
    }

    private fun locationActionForConnections(service: KolibreeService): LocationAction {
        return if (service.knownConnections.any { it.state().current != KLTBConnectionState.ACTIVE }) {
            locationEnabledChecker.getLocationAction()
        } else {
            NoAction
        }
    }
}
