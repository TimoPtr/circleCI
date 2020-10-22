/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsetsOwner
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentWakeYourBrushBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class WakeYourBrushFragment :
    BaseMVIFragment<
        WakeYourBrushViewState,
        BaseAction,
        WakeYourBrushViewModel.Factory,
        WakeYourBrushViewModel,
        FragmentWakeYourBrushBinding>(),
    TrackableScreen {

    companion object {
        fun newInstance(): WakeYourBrushFragment = WakeYourBrushFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        withWindowInsetsOwner { insets ->
            view?.setPadding(0, insets.topStatusBarWindowInset(), 0, insets.bottomNavigationBarInset())
        }
        return view
    }

    override fun getViewModelClass(): Class<WakeYourBrushViewModel> =
        WakeYourBrushViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_wake_your_brush

    override fun execute(action: BaseAction) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = WakeYourBrushAnalytics.main()
}
