/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityBrushingStreakCompletionBinding
import com.kolibree.android.tracker.NonTrackableScreen

internal class BrushingStreakCompletionActivity :
    BaseMVIActivity<
        BrushingStreakCompletionViewState,
        BrushingStreakCompletionActions,
        BrushingStreakCompletionViewModel.Factory,
        BrushingStreakCompletionViewModel, ActivityBrushingStreakCompletionBinding>(),
    NonTrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(rootContentLayout) {
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    override fun getViewModelClass(): Class<BrushingStreakCompletionViewModel> =
        BrushingStreakCompletionViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_brushing_streak_completion

    override fun execute(action: BrushingStreakCompletionActions) {
        when (action) {
            BrushingStreakCompletionActions.CompleteChallenge -> finish()
        }
    }

    fun readSmiles(): Int = intent.getIntExtra(SMILES_INTENT_EXTRA, 0)
}

@Keep
fun startBrushingStreakCompletionScreen(context: Context, smiles: Int) {
    context.startActivity(
        Intent(context, BrushingStreakCompletionActivity::class.java).apply {
            putExtra(SMILES_INTENT_EXTRA, smiles)
        }
    )
}

private const val SMILES_INTENT_EXTRA = "smiles_intent_extra"
