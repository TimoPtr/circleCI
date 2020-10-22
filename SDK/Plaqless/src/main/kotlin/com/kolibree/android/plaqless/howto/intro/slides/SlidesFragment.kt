/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slides

import android.os.Bundle
import android.view.View
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.plaqless.R
import com.kolibree.android.plaqless.databinding.FragmentSlidesBinding
import com.rd.animation.type.AnimationType

internal class SlidesFragment :
    BaseMVIFragment<SlidesViewState,
        SlidesAction,
        SlidesViewModel.Factory,
        SlidesViewModel,
        FragmentSlidesBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(): SlidesFragment {
            val fragment = SlidesFragment()

            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager?.let {
            binding.slidesPager.adapter = SlidesPagerAdapter(it)
        }
        binding.pageIndicatorView.setAnimationType(AnimationType.SLIDE)
    }

    override fun getViewModelClass(): Class<SlidesViewModel> = SlidesViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_slides

    override fun execute(action: SlidesAction) {
        // no-op
    }
}
