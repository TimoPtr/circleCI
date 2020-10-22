/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.results.plaqless

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sba.R
import com.kolibree.android.sba.databinding.FragmentPlaqlessResultsBinding

internal class PlaqlessResultsFragment : BaseMVIFragment<
    BaseViewState,
    BaseAction,
    PlaqlessResultsFragmentViewModel.Factory,
    PlaqlessResultsFragmentViewModel,
    FragmentPlaqlessResultsBinding
    >() {

    override fun getViewModelClass(): Class<PlaqlessResultsFragmentViewModel> =
        PlaqlessResultsFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_plaqless_results

    override fun execute(action: BaseAction) {
        // no-op
    }
}
