/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.headspace.R
import com.kolibree.android.headspace.databinding.ActivityHeadspaceMindfulMomentBinding
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentActions.OpenHeadspaceWebsite
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class HeadspaceMindfulMomentActivity : BaseMVIActivity<
    HeadspaceMindfulMomentViewState,
    HeadspaceMindfulMomentActions,
    HeadspaceMindfulMomentViewModel.Factory,
    HeadspaceMindfulMomentViewModel,
    ActivityHeadspaceMindfulMomentBinding>(), TrackableScreen {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            withWindowInsets(headspaceMindfulMomentRoot) {
                headspaceMindfulMomentToolbar.setPadding(0, topStatusBarWindowInset(), 0, 0)
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
    }

    override fun getViewModelClass(): Class<HeadspaceMindfulMomentViewModel> =
        HeadspaceMindfulMomentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_headspace_mindful_moment

    override fun execute(action: HeadspaceMindfulMomentActions) {
        when (action) {
            is OpenHeadspaceWebsite -> showInBrowser(R.string.headspace_share_url)
        }
    }

    override fun getScreenName(): AnalyticsEvent = HeadspaceMindfulMomentAnalytics.main()

    fun extractMindfulMoment(): HeadspaceMindfulMoment =
        intent.getParcelableExtra(EXTRA_MINDFUL_MOMENT_DETAILS) ?: error("Missing argument")
}

internal const val EXTRA_COLLECTED_TIME = "EXTRA_COLLECTED_TIME"
private const val EXTRA_MINDFUL_MOMENT_DETAILS = "EXTRA_MINDFUL_MOMENT_DETAILS"

internal fun createHeadspaceMindfulMomentIntent(
    context: Context,
    mindfulMoment: HeadspaceMindfulMoment
) = Intent(context, HeadspaceMindfulMomentActivity::class.java).apply {
    putExtra(EXTRA_MINDFUL_MOMENT_DETAILS, mindfulMoment)
}
