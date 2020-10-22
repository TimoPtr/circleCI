/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import com.kolibree.databinding.ActivityTestBrushingStartScreenBinding

internal class TestBrushingStartScreenActivity : BaseMVIActivity<
    TestBrushingStartScreenViewState,
    NoActions,
    TestBrushingStartScreenViewModel.Factory,
    TestBrushingStartScreenViewModel,
    ActivityTestBrushingStartScreenBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<TestBrushingStartScreenViewModel> =
        TestBrushingStartScreenViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_test_brushing_start_screen

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(binding.rootContentLayout) {
                toolbar.setPadding(0, topStatusBarWindowInset(), 0, 0)
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    override fun execute(action: NoActions) {
        // no-op
    }

    override fun getScreenName(): AnalyticsEvent = TestBrushingStartScreenAnalytics.main()

    fun providesMacAddress(): String =
        checkNotNull((intent.getParcelableExtra(EXTRA_TEST_BRUSHING_PARAMS) as? TestBrushingStartScreenParams?)?.mac)
}

@Keep
fun startTestBrushingStartScreenIntent(context: Context) =
    Intent(context, TestBrushingStartScreenActivity::class.java)
