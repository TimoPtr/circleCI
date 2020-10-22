/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing

import com.kolibree.android.coachplus.CoachPlusAnalytics
import com.kolibree.android.tracker.AnalyticsEvent

internal object GuidedBrushingAnalytics : CoachPlusAnalytics {

    override fun main() = AnalyticsEvent("GuidedBrushing")

    override fun quit() = pause() + "Quit"

    override fun pause() = main() + "Pause"

    override fun resume() = pause() + "Resume"

    override fun restart() = pause() + "Restart"
}
