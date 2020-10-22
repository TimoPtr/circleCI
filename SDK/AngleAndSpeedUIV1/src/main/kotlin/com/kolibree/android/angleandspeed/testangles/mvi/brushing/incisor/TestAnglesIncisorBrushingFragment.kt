/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing.incisor

import androidx.navigation.findNavController
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.databinding.FragmentTestAnglesIncisorBrushingBinding
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.OpenConfirmation
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.TestAnglesBrushingFragment
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.game.mvi.BaseGameAction

internal class TestAnglesIncisorBrushingFragment : TestAnglesBrushingFragment<
    TestAnglesIncisorBrushingViewModel.Factory,
    TestAnglesIncisorBrushingViewModel,
    FragmentTestAnglesIncisorBrushingBinding>() {

    override fun getViewModelClass(): Class<TestAnglesIncisorBrushingViewModel> =
        TestAnglesIncisorBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_test_angles_incisor_brushing

    override fun executeAndConsume(action: BaseGameAction): Boolean {
        return when (action) {
            is OpenConfirmation -> {
                activity?.findNavController(R.id.nav_host_fragment)
                    ?.navigateSafe(R.id.action_incisorBrushing_to_confirmation)
                true
            }
            else -> false
        }
    }
}
