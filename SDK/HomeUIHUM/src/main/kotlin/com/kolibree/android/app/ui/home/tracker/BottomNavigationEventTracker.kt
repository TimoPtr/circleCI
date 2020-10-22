/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tracker

import com.kolibree.android.app.ui.home.toolbar.ToolbarEventTracker
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent

internal object BottomNavigationEventTracker {

    fun dashboardVisible() = sendEvent(DASHBOARD)

    fun activitiesVisible() = sendEvent(ACTIVITIES)

    fun profileVisible() = sendEvent(PROFILE)

    fun shopVisible() = sendEvent(SHOP)

    private fun sendEvent(screenName: String) {
        ToolbarEventTracker.setCurrentPage(screenName)
        Analytics.send(AnalyticsEvent(screenName + HOME_SUFFIX))
    }
}

private const val HOME_SUFFIX = "-Home"
private const val DASHBOARD = "Dashboard"
private const val ACTIVITIES = "Activities"
private const val PROFILE = "Setting"
private const val SHOP = "Shop"
