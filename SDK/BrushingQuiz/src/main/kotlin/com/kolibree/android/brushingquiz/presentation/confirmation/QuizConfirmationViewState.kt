/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class QuizConfirmationViewState(val showProgressBar: Boolean = false) :
    BaseViewState {

    fun withShowProgressBar(showProgressBar: Boolean) = copy(showProgressBar = showProgressBar)

    companion object {
        fun initial() = QuizConfirmationViewState()
    }
}
