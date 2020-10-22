/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.mode

import android.view.View
import android.widget.AdapterView
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.databinding.FragmentModeBinding
import com.kolibree.android.glimmer.tweaker.utils.setupWithStringRes
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode

internal class ModeFragment :
    BaseMVIFragment<ModeViewState, NoActions, ModeViewModel.Factory, ModeViewModel, FragmentModeBinding>() {

    override fun onResume() {
        super.onResume()

        binding.modeSelectSpinner.setupWithStringRes(
            BrushingMode.values().map(BrushingMode::getResourceId), modeListener
        )
        setupNumberPickers()
    }

    private fun setupNumberPickers() {
        binding.segmentStrength1.setup(viewModel::onSegment1StrengthValue)
        binding.segmentStrength2.setup(viewModel::onSegment2StrengthValue)
        binding.segmentStrength3.setup(viewModel::onSegment3StrengthValue)
        binding.segmentStrength4.setup(viewModel::onSegment4StrengthValue)
        binding.segmentStrength5.setup(viewModel::onSegment5StrengthValue)
        binding.segmentStrength6.setup(viewModel::onSegment6StrengthValue)
        binding.segmentStrength7.setup(viewModel::onSegment7StrengthValue)
        binding.segmentStrength8.setup(viewModel::onSegment8StrengthValue)
        binding.segmentStrengthLast.setup(viewModel::onLastSegmentStrengthValue)
    }

    companion object {

        @JvmStatic
        fun newInstance() = ModeFragment()
    }

    private val modeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // no-op
        }

        override fun onItemSelected(spinner: AdapterView<*>, item: View, position: Int, id: Long) =
            viewModel.onModeSelected(position)
    }

    override fun getViewModelClass(): Class<ModeViewModel> = ModeViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_mode

    override fun execute(action: NoActions) {
        // no-op
    }
}
