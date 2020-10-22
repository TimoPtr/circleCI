/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object EarningPointsCardAnalytics {
    fun open() = send(AnalyticsEvent("EarningPoints_Information"))
    fun close() = send(AnalyticsEvent("EarningPoints_Information_close"))
}
