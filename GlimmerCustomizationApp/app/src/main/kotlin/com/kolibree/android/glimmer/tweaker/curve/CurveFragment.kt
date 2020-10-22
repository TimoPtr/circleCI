/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.curve

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.binding.setupAsSlope
import com.kolibree.android.glimmer.databinding.FragmentCurveBinding
import com.kolibree.android.glimmer.tweaker.utils.setup
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings

internal class CurveFragment :
    BaseMVIFragment<
        CurveViewState,
        NoActions,
        CurveViewModel.Factory,
        CurveViewModel,
        FragmentCurveBinding>(),
    OnItemSelectedListener {

    override fun onResume() {
        super.onResume()

        binding.curveSelectSpinner
            .setup(BrushingModeCurve.values().map(BrushingModeCurve::name), this)
        setupNumberPickers()
        setupSlopeNumberPickers()
    }

    override fun onNothingSelected(spinner: AdapterView<*>) {
        // no-op
    }

    override fun onItemSelected(spinner: AdapterView<*>, item: View, position: Int, id: Long) =
        viewModel.onCurveSelected(position)

    private fun setupNumberPickers() {
        binding.curveVoltagePicker.setup(
            maxValue = BrushingModeCurveSettings.MAX_REFERENCE_VOLTAGE,
            valueListener = viewModel::onReferenceVoltageValue
        )

        binding.curveDividerPicker.setup(
            maxValue = BrushingModeCurveSettings.MAX_DIVIDER,
            valueListener = viewModel::onDividerValue
        )
    }

    private fun setupSlopeNumberPickers() = binding.apply {
        viewModel?.let { vm ->
            curveSlope10.curveSlopePicker.setupAsSlope(vm::onSlope10Value)
            curveSlope20.curveSlopePicker.setupAsSlope(vm::onSlope20Value)
            curveSlope30.curveSlopePicker.setupAsSlope(vm::onSlope30Value)
            curveSlope40.curveSlopePicker.setupAsSlope(vm::onSlope40Value)
            curveSlope50.curveSlopePicker.setupAsSlope(vm::onSlope50Value)
            curveSlope60.curveSlopePicker.setupAsSlope(vm::onSlope60Value)
            curveSlope70.curveSlopePicker.setupAsSlope(vm::onSlope70Value)
            curveSlope80.curveSlopePicker.setupAsSlope(vm::onSlope80Value)
            curveSlope90.curveSlopePicker.setupAsSlope(vm::onSlope90Value)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = CurveFragment()
    }

    override fun getViewModelClass(): Class<CurveViewModel> = CurveViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_curve

    override fun execute(action: NoActions) {
        // no-op
    }
}
