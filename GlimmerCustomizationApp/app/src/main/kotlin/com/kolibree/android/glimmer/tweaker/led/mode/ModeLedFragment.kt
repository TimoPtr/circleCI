/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.mode

import android.os.Bundle
import android.view.View
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.databinding.FragmentModeLedBinding

internal class ModeLedFragment :
    BaseMVIFragment<ModeLedViewState, NoActions, ModeLedViewModel.Factory, ModeLedViewModel, FragmentModeLedBinding>() {

    companion object {

        @JvmStatic
        fun newInstance() = ModeLedFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.modeLedPwm1.setup(viewModel::onPwmLed1Value)
        binding.modeLedPwm2.setup(viewModel::onPwmLed2Value)
        binding.modeLedPwm3.setup(viewModel::onPwmLed3Value)
        binding.modeLedPwm4.setup(viewModel::onPwmLed4Value)
        binding.modeLedPwm5.setup(viewModel::onPwmLed5Value)
        binding.modeLedDuration.setup(viewModel::onDurationValue)
    }

    override fun getViewModelClass(): Class<ModeLedViewModel> = ModeLedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_mode_led

    override fun execute(action: NoActions) {
        // no-op
    }
}
