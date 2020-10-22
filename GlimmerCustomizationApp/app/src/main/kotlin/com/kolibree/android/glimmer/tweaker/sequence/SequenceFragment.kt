/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.sequence

import android.view.View
import android.widget.AdapterView
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.binding.setup
import com.kolibree.android.glimmer.databinding.FragmentSequenceBinding
import com.kolibree.android.glimmer.tweaker.mode.getResourceId
import com.kolibree.android.glimmer.tweaker.utils.setupWithStringRes
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence

internal class SequenceFragment :
    BaseMVIFragment<
        SequenceViewState,
        NoActions,
        SequenceViewModel.Factory,
        SequenceViewModel,
        FragmentSequenceBinding>() {

    private val sequenceListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // no-op
        }

        override fun onItemSelected(spinner: AdapterView<*>, item: View, position: Int, id: Long) =
            viewModel.onSequenceSelected(position)
    }

    override fun onResume() {
        super.onResume()

        binding.sequenceSelectSpinner.setupWithStringRes(
            BrushingModeSequence.values().map(BrushingModeSequence::getResourceId), sequenceListener
        )
        setupNumberPickers()
    }

    private fun setupNumberPickers() {
        binding.patternDuration1.setup(viewModel::onSequencePattern1DurationValue)
        binding.patternDuration2.setup(viewModel::onSequencePattern2DurationValue)
        binding.patternDuration3.setup(viewModel::onSequencePattern3DurationValue)
        binding.patternDuration4.setup(viewModel::onSequencePattern4DurationValue)
        binding.patternDuration5.setup(viewModel::onSequencePattern5DurationValue)
        binding.patternDuration6.setup(viewModel::onSequencePattern6DurationValue)
        binding.patternDuration7.setup(viewModel::onSequencePattern7DurationValue)
        binding.patternDuration8.setup(viewModel::onSequencePattern8DurationValue)
    }

    companion object {

        @JvmStatic
        fun newInstance() = SequenceFragment()
    }

    override fun getViewModelClass(): Class<SequenceViewModel> = SequenceViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_sequence

    override fun execute(action: NoActions) {
        // no-op
    }
}
