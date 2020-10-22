/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.is_brush_ready

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentIsBrushReadyBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class IsBrushReadyFragment :
    AnimatedBottomGroupFragment<
        EmptyBaseViewState,
        BaseAction,
        IsBrushReadyViewModel.Factory,
        IsBrushReadyViewModel,
        FragmentIsBrushReadyBinding>(),
    TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): IsBrushReadyFragment = IsBrushReadyFragment()
    }

    override fun getViewModelClass(): Class<IsBrushReadyViewModel> = IsBrushReadyViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_is_brush_ready

    override fun execute(action: BaseAction) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = IsBrushReadyAnalytics.main()

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomAnimatorGroup
}
