/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object EarnPointsCelebrationAnalytics {
    // TODO("This may come from different Analytics class")
    private const val wayToEarnPoints = "WaysToEarnPoints"

    fun main() = AnalyticsEvent(wayToEarnPoints + "Celebration")
    fun done() = send(main() + "Done")
}
