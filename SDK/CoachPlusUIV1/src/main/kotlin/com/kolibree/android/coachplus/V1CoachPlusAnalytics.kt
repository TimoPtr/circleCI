/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import com.kolibree.android.tracker.AnalyticsEvent

internal object V1CoachPlusAnalytics : CoachPlusAnalytics {

    override fun main() = AnalyticsEvent("Coach+")

    override fun quit() = main() + "Quit"

    override fun pause() = main() + "Pause"

    override fun resume() = pause() + "Resume"

    override fun restart() = pause() + "Restart"
}
