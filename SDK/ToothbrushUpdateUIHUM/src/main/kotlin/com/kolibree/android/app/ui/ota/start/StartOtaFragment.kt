/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.start

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.app.ui.ota.databinding.FragmentStartOtaBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class StartOtaFragment : BaseMVIFragment<
    EmptyBaseViewState,
    BaseAction,
    StartOtaViewModel.Factory,
    StartOtaViewModel,
    FragmentStartOtaBinding>(), TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): StartOtaFragment = StartOtaFragment()
    }

    override fun getViewModelClass(): Class<StartOtaViewModel> = StartOtaViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_start_ota

    override fun execute(action: BaseAction) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent =
        StartOtaAnalytics.main()
}
