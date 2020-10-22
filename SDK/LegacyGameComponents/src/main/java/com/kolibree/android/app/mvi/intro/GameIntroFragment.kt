/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.intro

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.auditor.UserStep
import com.kolibree.android.tracker.TrackableScreen
import com.kolibree.game.legacy.R
import com.kolibree.game.legacy.databinding.FragmentGameIntroBinding

/**
 * Generic game intro fragment, with header, body, animated GIF and start button.
 * Used in Speed Control and Test Angles.
 *
 * Available customizations:
 * - strings for header, body & button
 * - animated GIF
 * - handling button press
 */
@Keep
abstract class GameIntroFragment : BaseMVIFragment<
    EmptyBaseViewState,
    GameIntroAction,
    GameIntroViewModel.Factory,
    GameIntroViewModel,
    FragmentGameIntroBinding
    >(), UserStep, TrackableScreen {

    override fun getViewModelClass(): Class<GameIntroViewModel> =
        GameIntroViewModel::class.java

    override fun getLayoutId(): Int =
        R.layout.fragment_game_intro

    override fun execute(action: GameIntroAction) {
        when (action) {
            is OpenBrushScreen -> openBrushScreen()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resourcesProvider = resourcesProvider()
    }

    protected abstract fun resourcesProvider(): GameIntroResourceProvider

    protected abstract fun openBrushScreen()
}
