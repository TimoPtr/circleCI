/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityPairingStartScreenBinding
import com.kolibree.android.tracker.NonTrackableScreen

internal class PairingStartScreenActivity :
    BaseMVIActivity<EmptyBaseViewState,
        NoActions,
        PairingStartScreenViewModel.Factory,
        PairingStartScreenViewModel,
        ActivityPairingStartScreenBinding>(),
    NonTrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(rootContentLayout) {
                viewTop.setPadding(0, topStatusBarWindowInset(), 0, 0)
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    override fun getViewModelClass(): Class<PairingStartScreenViewModel> =
        PairingStartScreenViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_pairing_start_screen

    override fun execute(action: NoActions) {
        // no-op
    }
}

internal fun pairingStartScreenIntent(context: Context) =
    Intent(context, PairingStartScreenActivity::class.java)
