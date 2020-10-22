/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import androidx.annotation.IdRes
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode

internal sealed class QuizActions : BaseAction

internal data class NavigateToConfirmBrushingProgramAction(val selectedBrushingMode: BrushingMode) :
    QuizActions() {
    @IdRes
    val navigationActionId: Int = R.id.brushing_quiz_completed
}

internal object FinishBrushingProgramAction : QuizActions()
