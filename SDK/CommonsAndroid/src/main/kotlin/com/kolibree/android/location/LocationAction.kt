/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.location

import androidx.annotation.Keep

@Keep
sealed class LocationAction(val requestPermission: Boolean = false, val requestEnableLocation: Boolean = false) {
    val isReadyToScan = !requestEnableLocation && !requestPermission
}

/**
 * No action is required to use Location
 */
@Keep
object NoAction : LocationAction()

/**
 * Location is disabled
 */
@Keep
object EnableLocation : LocationAction(requestEnableLocation = true)

/**
 * Location permission isn't granted
 */
@Keep
@Deprecated("This will only be trigger if the location is disable",
    ReplaceWith("ConnectionPrerequisitesState.LocationPermissionNotGranted"))
object RequestPermission : LocationAction(requestPermission = true)
