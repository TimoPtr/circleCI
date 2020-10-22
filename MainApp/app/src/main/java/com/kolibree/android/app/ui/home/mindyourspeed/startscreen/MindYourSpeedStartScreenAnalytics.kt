/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object MindYourSpeedStartScreenAnalytics {
    fun main() = AnalyticsEvent(name = "MindYourSpeed")
    private fun startScreen() = main() + "IntroScreen"
    fun start() = send(startScreen() + "Start")
    fun cancel() = send(startScreen() + "Cancel")
}
