/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.plaqless.R
import com.kolibree.android.plaqless.databinding.FragmentPlaqlessIntroBinding

internal class PlaqlessIntroFragment : BaseMVIFragment<PlaqlessIntroViewState,
    PlaqlessIntroAction,
    PlaqlessIntroViewModel.Factory,
    PlaqlessIntroViewModel,
    FragmentPlaqlessIntroBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(): PlaqlessIntroFragment {
            val fragment = PlaqlessIntroFragment()

            return fragment
        }
    }

    override fun getViewModelClass(): Class<PlaqlessIntroViewModel> = PlaqlessIntroViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_plaqless_intro

    override fun execute(action: PlaqlessIntroAction) {
        // no-op
    }
}
