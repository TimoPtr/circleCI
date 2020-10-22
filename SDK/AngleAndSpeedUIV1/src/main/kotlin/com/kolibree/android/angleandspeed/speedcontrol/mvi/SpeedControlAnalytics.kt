/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi

import androidx.annotation.Keep
import com.kolibree.android.tracker.AnalyticsEvent

@Keep
object SpeedControlAnalytics {

    private val FEATURE = AnalyticsEvent(name = "SpeedControl")

    fun introScreen() = FEATURE + "Intro"

    fun startScreen() = FEATURE + "Start"

    fun finishScreen() = FEATURE + "Finish"
}
