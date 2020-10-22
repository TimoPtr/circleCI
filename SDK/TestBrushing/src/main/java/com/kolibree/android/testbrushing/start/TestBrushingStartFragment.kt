/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.withWindowInsetsOwner
import com.kolibree.android.app.mvi.brushstart.BaseBrushStartFragment
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.testbrushing.R
import com.kolibree.android.testbrushing.TestBrushingAnalytics
import com.kolibree.android.testbrushing.databinding.FragmentBrushingStartBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

@VisibleForApp
class TestBrushingStartFragment : BaseBrushStartFragment<
    NoActions,
    TestBrushingStartViewModel.Factory,
    TestBrushingStartViewModel,
    FragmentBrushingStartBinding>(), TrackableScreen {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        withWindowInsetsOwner { insets ->
            with(binding.image) {
                (layoutParams as? ConstraintLayout.LayoutParams)?.also { layoutParams ->
                    layoutParams.bottomMargin += insets.bottomNavigationBarInset()
                } ?: FailEarly.fail("LayoutParams type is not ConstraintLayout.LayoutParams")
            }
        }
        return view
    }

    override fun getViewModelClass() = TestBrushingStartViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_brushing_start

    override fun execute(action: NoActions) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = TestBrushingAnalytics.startScreen()
}
