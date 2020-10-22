/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.inprogress

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.app.ui.ota.databinding.FragmentInProgressOtaBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class InProgressOtaFragment : BaseMVIFragment<
    InProgressOtaViewState,
    InProgressOtaActions,
    InProgressOtaViewModel.Factory,
    InProgressOtaViewModel,
    FragmentInProgressOtaBinding>(), TrackableScreen {

    companion object {
        @JvmStatic
        fun newInstance(): InProgressOtaFragment =
            InProgressOtaFragment()
    }

    override fun getViewModelClass(): Class<InProgressOtaViewModel> =
        InProgressOtaViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_in_progress_ota

    override fun execute(action: InProgressOtaActions) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent =
        InProgressOtaAnalytics.main()
}
