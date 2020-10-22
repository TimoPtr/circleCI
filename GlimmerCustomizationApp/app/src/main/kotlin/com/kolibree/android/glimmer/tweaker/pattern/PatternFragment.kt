/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.pattern

import android.view.View
import android.widget.AdapterView
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.databinding.FragmentPatternBinding
import com.kolibree.android.glimmer.tweaker.utils.setup
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern

internal class PatternFragment :
    BaseMVIFragment<PatternViewState,
        NoActions,
        PatternViewModel.Factory,
        PatternViewModel,
        FragmentPatternBinding>() {

    companion object {

        @JvmStatic
        fun newInstance() = PatternFragment()
    }

    private val patternListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // no-op
        }

        override fun onItemSelected(spinner: AdapterView<*>, item: View, position: Int, id: Long) =
            viewModel.onPatternSelected(position)
    }

    override fun getViewModelClass(): Class<PatternViewModel> = PatternViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_pattern

    override fun execute(action: NoActions) {
        // no-op
    }

    override fun onResume() {
        super.onResume()

        binding.patternSelectSpinner.setup(
            BrushingModePattern.values().map(BrushingModePattern::name), patternListener
        )
        setupNumberPickers()
    }

    private fun setupNumberPickers() {
        binding.dutyStrength1Picker.setup(viewModel::onDutyStrength1Value)
        binding.dutyStrength10Picker.setup(viewModel::onDutyStrength10Value)
        binding.refMinimalDutyPicker.setup(viewModel::onMinimalDutyCycleHalfPercentValue)
        binding.oscillationPeriodPicker.setup(viewModel::onOscillatingPeriodTenthSecondValue)
        binding.param1Picker.setup(viewModel::onOscillationParam1Value)
        binding.param2Picker.setup(viewModel::onOscillationParam2Value)
        binding.param3Picker.setup(viewModel::onOscillationParam3Value)
        binding.patternMotorFsPicker.setup(viewModel::onPatternFrequency)
    }
}
