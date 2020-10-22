/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.signal

import android.os.Bundle
import android.view.View
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.databinding.FragmentLedSignalBinding

internal class LedSignalFragment :
    BaseMVIFragment<
            LedSignalViewState,
            NoActions,
            LedSignalViewModel.Factory,
            LedSignalViewModel,
            FragmentLedSignalBinding>() {

    companion object {

        @JvmStatic
        fun newInstance() = LedSignalFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.red.setup(viewModel::onRedValue)
        binding.green.setup(viewModel::onGreenValue)
        binding.blue.setup(viewModel::onBlueValue)
        binding.period.setup(viewModel::onPeriodValue)
        binding.duration.setup(viewModel::onDurationValue)
    }

    override fun getViewModelClass(): Class<LedSignalViewModel> =
        LedSignalViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_led_signal

    override fun execute(action: NoActions) {
        // no-op
    }
}
