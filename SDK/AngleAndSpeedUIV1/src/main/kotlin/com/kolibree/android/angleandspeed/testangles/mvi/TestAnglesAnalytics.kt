/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi

import androidx.annotation.Keep
import com.kolibree.android.tracker.AnalyticsEvent

@Keep
object TestAnglesAnalytics {

    private val FEATURE = AnalyticsEvent("TestYourAngles")

    fun introScreen() = FEATURE + "Intro"

    fun startScreen() = FEATURE + "Start"

    fun finishScreen() = FEATURE + "Finish"
}
