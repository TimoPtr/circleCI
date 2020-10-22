/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.tracker.AnalyticsEvent

@VisibleForApp
object TestBrushingAnalytics {

    private fun main() = AnalyticsEvent("TestBrushing")

    fun startScreen() = main() + "Start"

    fun ongoingBrushingScreen() = main() + "OngoingBrushing"

    fun pauseScreen() = main() + "Pause"

    fun quit() = main() + "Quit"

    fun finishedWithSuccess() = main() + "FinishSuccess"

    fun notFinished() = main() + "NotFinished"
}
