/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.BrushingStreakAnalytics
import javax.inject.Inject

internal class BrushingStreakCompletionViewModel(
    initialViewState: BrushingStreakCompletionViewState
) : BaseViewModel<BrushingStreakCompletionViewState, BrushingStreakCompletionActions>(initialViewState) {

    fun onCompleteClick() {
        BrushingStreakAnalytics.celebrationComplete()
        pushAction(BrushingStreakCompletionActions.CompleteChallenge)
    }

    class Factory @Inject constructor(private val smiles: Int) :
        BaseViewModel.Factory<BrushingStreakCompletionViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BrushingStreakCompletionViewModel(BrushingStreakCompletionViewState(smiles = smiles)) as T
    }
}
