/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing.molar

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.databinding.FragmentTestAnglesMolarBrushingBinding
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.OpenIncisorBrushing
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.TestAnglesBrushingFragment
import com.kolibree.android.app.ui.extention.afterMeasured
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.game.mvi.BaseGameAction

internal class TestAnglesMolarBrushingFragment : TestAnglesBrushingFragment<
    TestAnglesMolarBrushingViewModel.Factory,
    TestAnglesMolarBrushingViewModel,
    FragmentTestAnglesMolarBrushingBinding>() {

    override fun getViewModelClass(): Class<TestAnglesMolarBrushingViewModel> =
        TestAnglesMolarBrushingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_test_angles_molar_brushing

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.molarAnimationArea.afterMeasured {
            binding.molarAnimationRect = Rect(
                // by doing this, we will have brush at the exact center at 0 degrees
                x.toInt() - binding.brushHead.width / 2,
                y.toInt(),
                // by doing this, we will have brush at the exact center at 0 degrees
                (x + width).toInt() - binding.brushHead.width / 2,
                // by doing this, we will have brush's center at the end of the animation area
                (y + height).toInt() - binding.brushHead.height / 2
            )
        }
    }

    override fun executeAndConsume(action: BaseGameAction): Boolean = when (action) {
        is OpenIncisorBrushing -> {
            activity?.findNavController(R.id.nav_host_fragment)
                ?.navigateSafe(R.id.action_molarBrushing_to_incisorBrushing)
            true
        }
        else -> false
    }
}
