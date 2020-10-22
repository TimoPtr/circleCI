/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.model_mismatch

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentModelMismatchBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class ModelMismatchFragment :
    AnimatedBottomGroupFragment<
        EmptyBaseViewState,
        BaseAction,
        ModelMismatchViewModel.Factory,
        ModelMismatchViewModel,
        FragmentModelMismatchBinding>(),
    TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): ModelMismatchFragment = ModelMismatchFragment()
    }

    override fun getViewModelClass(): Class<ModelMismatchViewModel> = ModelMismatchViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_model_mismatch

    override fun execute(action: BaseAction) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = ModelMismatchAnalytics.main()

    override fun animatedBottomGroup(): AnimatorGroup? = binding.bottomAnimatorGroup
}
