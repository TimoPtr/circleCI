/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.brush_found

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentBrushFoundBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class BrushFoundFragment :
    AnimatedBottomGroupFragment<
        EmptyBaseViewState,
        BaseAction,
        BrushFoundViewModel.Factory,
        BrushFoundViewModel,
        FragmentBrushFoundBinding>(),
    TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): BrushFoundFragment = BrushFoundFragment()
    }

    override fun getViewModelClass(): Class<BrushFoundViewModel> = BrushFoundViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_brush_found

    override fun execute(action: BaseAction) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = BrushFoundAnalytics.main()

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomAnimatorGroup
}
