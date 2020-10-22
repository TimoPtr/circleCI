/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object ActivitiesEventTracker {

    private fun shortTasks() = AnalyticsEvent(SHORT_TASKS)

    private fun activities() = AnalyticsEvent(ACTIVITIES)

    fun testBrushingClick() = send(shortTasks() + TEST_BRUSHING)

    fun mindYourSpeedClick() = send(shortTasks() + MIND_YOUR_SPEED)

    fun adjustBrushingAngleClick() = send(shortTasks() + ADJUST_BRUSHING_ANGLE)

    fun guidedBrushingClick() = send(activities() + GUIDED_BRUSHING)
}

private const val ACTIVITIES = "Activities"
private const val SHORT_TASKS = "ShortTasks"

private const val TEST_BRUSHING = "TestBrushing"
private const val MIND_YOUR_SPEED = "MindYourSpeed"
private const val ADJUST_BRUSHING_ANGLE = "AdjustAngle"
private const val GUIDED_BRUSHING = "GuidedBrushing"
