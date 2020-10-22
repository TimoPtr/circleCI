/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import com.kolibree.databinding.ActivityMindYourSpeedStartScreenBinding

internal class MindYourSpeedStartScreenActivity :
    BaseMVIActivity<EmptyBaseViewState,
        NoActions,
        MindYourSpeedStartScreenViewModel.Factory,
        MindYourSpeedStartScreenViewModel,
        ActivityMindYourSpeedStartScreenBinding>(),
    TrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(binding.rootContentLayout) {
                logo.setPadding(0, topStatusBarWindowInset(), 0, 0)
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    override fun getViewModelClass(): Class<MindYourSpeedStartScreenViewModel> =
        MindYourSpeedStartScreenViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_mind_your_speed_start_screen

    override fun execute(action: NoActions) {
        // no op
    }

    override fun getScreenName(): AnalyticsEvent = MindYourSpeedStartScreenAnalytics.main()

    fun providesMacAddress(): String =
        checkNotNull((intent.getParcelableExtra(EXTRA_MIND_YOUR_SPEED_PARAMS) as? MindYourSpeedStartScreenParams?)?.mac)
}

@Keep
fun startMindYourSpeedStartScreenIntent(context: Context): Intent =
    Intent(context, MindYourSpeedStartScreenActivity::class.java)
