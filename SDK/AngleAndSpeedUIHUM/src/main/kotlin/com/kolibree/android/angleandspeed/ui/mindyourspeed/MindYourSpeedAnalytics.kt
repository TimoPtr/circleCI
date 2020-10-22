/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import com.kolibree.android.tracker.AnalyticsEvent

internal object MindYourSpeedAnalytics {

    fun main() = AnalyticsEvent(name = "MindYourSpeed")

    fun quit() = main() + "Quit"

    fun pause() = main() + "Pause"

    fun resume() = pause() + "Resume"

    fun restart() = pause() + "Restart"

    fun finishedWithSuccess() = main() + "FinishSuccess"
}
