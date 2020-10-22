/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ActivitiesViewState(
    val testBrushingTask: TaskViewState = TestBrushingTask,
    val testSpeedTask: TaskViewState,
    val testAngleTask: TaskViewState = TestAngleTask
) : BaseViewState {

    companion object {
        fun initial(testSpeedVisible: Boolean) = ActivitiesViewState(
            testSpeedTask = TestSpeedTask(isVisible = testSpeedVisible)
        )
    }
}
