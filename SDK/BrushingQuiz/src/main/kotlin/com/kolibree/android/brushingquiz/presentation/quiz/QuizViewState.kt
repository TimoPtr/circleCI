/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class QuizViewState(
    val currentQuizPosition: Int = 0,
    val showProgressBar: Boolean = false
) : BaseViewState {

    fun withCurrentQuizPosition(screenIndex: Int) = copy(currentQuizPosition = screenIndex)

    fun withShowProgressBar(showProgressBar: Boolean) = copy(showProgressBar = showProgressBar)

    companion object {
        fun initial() = QuizViewState(currentQuizPosition = 0)
    }
}
