/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import androidx.navigation.findNavController
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.databinding.FragmentSpeedControlBrushingBinding
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.game.mvi.BaseGameFragment
import com.kolibree.android.tracker.NonTrackableScreen

internal class SpeedControlBrushingFragment : BaseGameFragment<
    SpeedControlBrushingViewState,
    SpeedControlBrushingViewModel.Factory,
    SpeedControlBrushingViewModel,
    FragmentSpeedControlBrushingBinding>(),
    NonTrackableScreen {

    override fun getViewModelClass(): Class<SpeedControlBrushingViewModel> =
        SpeedControlBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_speed_control_brushing

    override fun executeAndConsume(action: BaseGameAction): Boolean {
        if (action is OpenConfirmation) {
            openConfirmation()
            return true
        }
        return false
    }

    private fun openConfirmation() {
        activity?.findNavController(R.id.nav_host_fragment)
            ?.navigateSafe(R.id.action_brushing_to_confirmation)
    }
}
