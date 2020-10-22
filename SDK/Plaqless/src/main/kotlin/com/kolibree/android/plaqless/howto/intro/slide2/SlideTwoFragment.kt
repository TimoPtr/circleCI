/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide2

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.plaqless.R
import com.kolibree.android.plaqless.databinding.FragmentSlideTwoBinding
import com.kolibree.android.tracker.NonTrackableScreen

internal class SlideTwoFragment :
    BaseMVIFragment<SlideTwoViewState,
        SlideTwoAction,
        SlideTwoViewModel.Factory,
        SlideTwoViewModel,
        FragmentSlideTwoBinding>(),
    NonTrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): SlideTwoFragment {
            val fragment = SlideTwoFragment()

            return fragment
        }
    }

    override fun getViewModelClass(): Class<SlideTwoViewModel> = SlideTwoViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_slide_two

    override fun execute(action: SlideTwoAction) {
        // No-op
    }
}
