/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.special

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.databinding.FragmentSpecialLedBinding
import com.kolibree.android.glimmer.tweaker.utils.setup
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed

internal class SpecialLedFragment :
    BaseMVIFragment<
            SpecialLedViewState,
            NoActions,
            SpecialLedViewModel.Factory,
            SpecialLedViewModel,
            FragmentSpecialLedBinding>() {

    companion object {

        @JvmStatic
        fun newInstance() = SpecialLedFragment()
    }

    private val ledListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // no-op
        }

        override fun onItemSelected(spinner: AdapterView<*>, item: View, position: Int, id: Long) =
            viewModel.onLedSelected(position)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ledSpinner.setup(SpecialLed.values().map(SpecialLed::name), ledListener)

        binding.pwm.setup(viewModel::onPwmValue)
    }

    override fun getViewModelClass(): Class<SpecialLedViewModel> = SpecialLedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_special_led

    override fun execute(action: NoActions) {
        // no-op
    }
}
