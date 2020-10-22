/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

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
import com.kolibree.databinding.ActivityGuidedBrushingStartScreenBinding

internal class GuidedBrushingStartScreenActivity : BaseMVIActivity<
    EmptyBaseViewState,
    NoActions,
    GuidedBrushingStartScreenViewModel.Factory,
    GuidedBrushingStartScreenViewModel,
    ActivityGuidedBrushingStartScreenBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<GuidedBrushingStartScreenViewModel> =
        GuidedBrushingStartScreenViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_guided_brushing_start_screen

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(rootContentLayout) {
                logo.setPadding(0, topStatusBarWindowInset(), 0, 0)
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    override fun execute(action: NoActions) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = GuidedBrushingStartScreenAnalytics.main()

    fun providesMacAddress(): String? =
        (intent?.getParcelableExtra(EXTRA_GUIDED_BRUSHING_PARAMS) as? GuidedBrushingStartScreenParams?)?.mac
}

@Keep
fun startGuidedBrushingStartScreenIntent(context: Context) =
    Intent(context, GuidedBrushingStartScreenActivity::class.java)
