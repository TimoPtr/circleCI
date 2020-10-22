/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize

internal abstract class TaskViewState(
    val visible: Boolean,
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
    private val points: Int
) : Parcelable {
    fun points(context: Context) =
        context.getString(R.string.activities_task_points, points.toString())
}

@Parcelize
internal object TestBrushingTask : TaskViewState(
    visible = true,
    title = R.string.activities_task_test_brushing_title,
    description = R.string.activities_task_test_brushing_description,
    icon = R.drawable.ic_task_test_brushing,
    points = TASK_REWARD_POINTS
)

@Parcelize
internal class TestSpeedTask(
    val isVisible: Boolean
) : TaskViewState(
    visible = isVisible,
    title = R.string.activities_task_speed_title,
    description = R.string.activities_task_speed_description,
    icon = R.drawable.ic_task_test_speed,
    points = TASK_REWARD_POINTS
)

@Parcelize
internal object TestAngleTask : TaskViewState(
    visible = false,
    title = R.string.activities_task_angle_title,
    description = R.string.activities_task_angle_description,
    icon = R.drawable.ic_task_test_angle,
    points = TASK_REWARD_POINTS
)

private const val TASK_REWARD_POINTS = 2
