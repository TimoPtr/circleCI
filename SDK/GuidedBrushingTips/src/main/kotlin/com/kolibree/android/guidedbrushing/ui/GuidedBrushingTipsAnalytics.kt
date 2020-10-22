package com.kolibree.android.guidedbrushing.ui

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object GuidedBrushingTipsAnalytics {
    fun main() = AnalyticsEvent(name = "GuidedBrushing_Tips")

    fun close() = send(main() + "Close")

    fun gotIt() = send(main() + "GotIt")

    fun noShowAgain() = send(main() + "NoShowAgain")
}
