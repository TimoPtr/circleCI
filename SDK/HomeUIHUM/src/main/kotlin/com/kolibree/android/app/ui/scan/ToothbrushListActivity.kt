/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.scan

import android.os.Bundle
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListFragment
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityToothbrushListBinding
import com.kolibree.android.tracker.NonTrackableScreen

internal class ToothbrushListActivity :
    BaseMVIActivity<ToothbrushListViewState,
        ToothbrushListActions,
        ToothbrushListViewModel.Factory,
        ToothbrushListViewModel,
        ActivityToothbrushListBinding>(),
    NonTrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanToothbrushListFragment.newInstance())
                .commit()
        }
    }

    override fun getViewModelClass(): Class<ToothbrushListViewModel> =
        ToothbrushListViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_toothbrush_list

    override fun execute(action: ToothbrushListActions) {
        // no-op
    }
}
