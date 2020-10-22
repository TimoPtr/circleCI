/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import com.kolibree.android.tracker.AnalyticsEvent

internal object LocationAnalytics {
    fun grantLocationPermissionMain() = AnalyticsEvent(name = "GrantLocalization")
    fun enableLocationMain() = AnalyticsEvent(name = "EnableLocalization")
    fun locationPermissionGranted() = grantLocationPermissionMain() + "Grant"
    fun locationEnabled() = enableLocationMain() + "Grant"
}
