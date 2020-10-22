/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.insets.WindowInsetsMediator
import com.kolibree.android.app.insets.WindowInsetsOwner
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.ui.activity.getCurrentNavFragment
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityToothbrushPairingBinding
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.NonTrackableScreen
import com.kolibree.android.tracker.TrackableScreen

@VisibleForApp
class ToothbrushPairingActivity :
    BaseMVIActivity<
        ToothbrushPairingViewState,
        NoActions,
        ToothbrushPairingViewModel.Factory,
        ToothbrushPairingViewModel,
        ActivityToothbrushPairingBinding
        >(),
    NonTrackableScreen,
    WindowInsetsOwner {

    private lateinit var windowInsetMediator: WindowInsetsMediator

    override fun withWindowInsets(block: (WindowInsets) -> Unit) {
        windowInsetMediator.withWindowInsets(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindowInsetsMediator()
    }

    private fun setupWindowInsetsMediator() {
        windowInsetMediator = WindowInsetsMediator(binding.rootContentLayout)
        windowInsetMediator.withWindowInsets { insets ->
            binding.toolbar.setPadding(0, insets.topStatusBarWindowInset(), 0, 0)
            binding.progressView.setPadding(
                0,
                insets.topStatusBarWindowInset(),
                0,
                insets.bottomNavigationBarInset()
            )
        }
    }

    override fun onBackPressed() {
        when {
            // we need to check the sharedViewState in order to check
            // if any fragment from PairingFlow is displaying a progressDialog
            viewModel.getSharedViewState()?.progressVisible() == true -> { // no-op
            }
            else -> {
                Analytics.send(
                    ((getCurrentlyVisibleFragment() as? TrackableScreen)?.let {
                        it.getScreenName() + PairingAnalytics.goBack()
                    } ?: PairingAnalytics.goBack()))
                super.onBackPressed()
            }
        }
    }

    private fun getCurrentlyVisibleFragment(): Fragment? =
        getCurrentNavFragment(R.id.nav_host_fragment)

    override fun getViewModelClass(): Class<ToothbrushPairingViewModel> =
        ToothbrushPairingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_toothbrush_pairing

    override fun execute(action: NoActions) {
        // no-op
    }
}

@Keep
fun startToothbrushPairingIntent(context: Context) {
    context.startActivity(Intent(context, ToothbrushPairingActivity::class.java))
}
