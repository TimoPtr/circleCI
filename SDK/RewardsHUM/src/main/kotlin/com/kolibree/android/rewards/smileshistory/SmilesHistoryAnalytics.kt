/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object SmilesHistoryAnalytics {
    fun main() = AnalyticsEvent(name = "PointsHistory")

    fun open() = send(main() + "Open")

    fun quit() = send(main() + "Quit")
}
