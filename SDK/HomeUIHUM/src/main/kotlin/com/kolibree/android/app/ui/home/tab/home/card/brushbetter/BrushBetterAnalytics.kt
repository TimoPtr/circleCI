/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object BrushBetterAnalytics {
    private fun main() = AnalyticsEvent(name = "BrushBetter")
    fun guidedBrushing() = send(main() + "GuidedBrushing")
    fun mindYourSpeed() = send(main() + "MindYourSpeed")
    fun adjustBrushingAngle() = send(main() + "AdjustBrushingAngle")
    fun testBrushing() = send(main() + "TestBrushing")
}
