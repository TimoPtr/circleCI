/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap

import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.mouthmap.databinding.FragmentMouthMapBinding
import com.kolibree.android.mouthmap.dialog.BuildUpRemainsDialog
import com.kolibree.android.mouthmap.dialog.CleanScoreDialog
import com.kolibree.android.mouthmap.dialog.MissedAreasDialog
import com.kolibree.android.mouthmap.logic.BrushingResults
import javax.inject.Inject

@Keep
class MouthMapFragment : BaseMVIFragment<
    EmptyBaseViewState,
    MouthMapAction,
    MouthMapViewModel.Factory,
    MouthMapViewModel,
    FragmentMouthMapBinding
    >() {

    companion object {

        const val TAG = "MouthMapFragment"
        private const val ARGUMENT_BRUSHING_RESULT = "ARGUMENT_BRUSHING_RESULT"

        @JvmStatic
        fun newInstance(brushingResults: BrushingResults): MouthMapFragment {
            val arguments = Bundle()
            val mouthMapFragment = MouthMapFragment()
            arguments.putParcelable(ARGUMENT_BRUSHING_RESULT, brushingResults)
            mouthMapFragment.arguments = arguments
            return mouthMapFragment
        }
    }

    fun prePopulatedBrushingResults(): BrushingResults? =
        arguments?.getParcelable(ARGUMENT_BRUSHING_RESULT)

    @Inject
    internal lateinit var viewModelFactory: MouthMapViewModel.Factory

    override fun getViewModelClass() = MouthMapViewModel::class.java

    override fun getLayoutId() = R.layout.fragment_mouth_map

    override fun execute(action: MouthMapAction) {
        when (action) {
            is ToggleJawsView -> {
                binding.mouthMapJaw.toggleJawsMode()
            }
            is ShowMissedAreaDialog -> {
                MissedAreasDialog.show(childFragmentManager)
            }
            is ShowBuildUpRemainsDialog -> {
                BuildUpRemainsDialog.show(childFragmentManager)
            }
            is ShowCoverageDialog -> {
                CleanScoreDialog.show(
                    fragmentManager = childFragmentManager,
                    hasPlaqlessData = action.hasPlaqlessData,
                    missed = action.missed,
                    remains = action.remains,
                    score = action.coverage
                )
            }
        }
    }

    override fun onStop() {
        /*
        onPause call in onStop so the view still draws when the fragment is paused BUUUUT still
        visible in the pager
         */
        binding.mouthMapJaw.jawsView.pause()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        // onResume call in onStart so the view is ready when we swipe the pager
        binding.mouthMapJaw.jawsView.resume()
    }
}
