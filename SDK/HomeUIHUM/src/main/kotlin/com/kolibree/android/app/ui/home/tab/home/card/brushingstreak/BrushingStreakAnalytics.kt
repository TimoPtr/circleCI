/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object BrushingStreakAnalytics {
    private fun main() = AnalyticsEvent(name = "BrushingStreak")
    private fun challenge() = main() + "Challenge"
    private fun activity() = main() + "DiscoverActivity"

    fun accept(isMultiDaysChallenge: Boolean) {
        val event = if (isMultiDaysChallenge) challenge() else activity()
        send(event + "Accept")
    }

    fun complete(isMultiDaysChallenge: Boolean) {
        val event = if (isMultiDaysChallenge) challenge() else activity()
        send(event + "Complete")
    }

    fun celebrationComplete() = send(main() + "Celebration_Complete")

    fun action() = send(activity() + "GoToGB")

    fun open() = send(main() + "Open")
}
