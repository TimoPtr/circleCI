/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

@VisibleForApp
object HomeScreenAnalytics {

    fun main() = AnalyticsEvent(name = "Home")

    fun shop() = AnalyticsEvent(name = "Shop")

    fun activities() = AnalyticsEvent(name = "Activities")

    fun profile() = AnalyticsEvent(name = "Profile")

    fun navigationEvent(fromScreenEvent: AnalyticsEvent, toScreenEvent: AnalyticsEvent) {
        send(fromScreenEvent + "To" + toScreenEvent)
    }
}
