/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.confirmation

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.auditor.UserStep
import com.kolibree.android.tracker.TrackableScreen
import com.kolibree.game.legacy.R
import com.kolibree.game.legacy.databinding.FragmentGameConfirmationBinding

/**
 * Generic game confirmation fragment, with test + highlight and central image.
 * Used in Speed Control and Test Angles.
 *
 * Available customizations:
 * - bottom text
 * - bottom text highlight
 * - drawable
 */
@Keep
abstract class GameConfirmationFragment : BaseMVIFragment<
    EmptyBaseViewState,
    GameConfirmationAction,
    GameConfirmationViewModel.Factory,
    GameConfirmationViewModel,
    FragmentGameConfirmationBinding
    >(), UserStep, TrackableScreen {

    override fun getViewModelClass(): Class<GameConfirmationViewModel> =
        GameConfirmationViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_game_confirmation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resourcesProvider = resourcesProvider()
    }

    override fun execute(action: GameConfirmationAction) {
        when (action) {
            is CloseFeature -> activity?.finish()
        }
    }

    protected abstract fun resourcesProvider(): GameConfirmationResourceProvider
}
