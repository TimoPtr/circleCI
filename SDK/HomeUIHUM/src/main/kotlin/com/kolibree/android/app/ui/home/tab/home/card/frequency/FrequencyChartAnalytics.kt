/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object FrequencyChartAnalytics {
    private fun main() = AnalyticsEvent(name = "FrequencyChart")
    fun previousMonth() = send(main() + "PassedMonth")
    fun nextMonth() = send(main() + "NextMonth")
    fun day() = send(main() + "Day")
}
