/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.checkup.base.BaseCheckupActivity
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityDayCheckupBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

/** Checkup screen (shows brushing session details such as metrics or mouth map) */
@VisibleForApp // Kept for Espresso
class CheckupResultsActivity :
    BaseCheckupActivity<
        CheckupResultsViewState,
        CheckupResultsViewModel.Factory,
        CheckupResultsViewModel,
        ActivityDayCheckupBinding>(), TrackableScreen {

    override fun getViewModelClass() = CheckupResultsViewModel::class.java

    override fun getLayoutId() = R.layout.activity_checkup_results

    fun extractOrigin() = intent.getSerializableExtra(EXTRA_ORIGIN) as CheckupOrigin

    override fun getScreenName(): AnalyticsEvent = CheckupResultsAnalytics.main(extractOrigin())
}

/**
 * Create a [Intent] to start [CheckupResultsActivity]
 *
 * @param ownerContext [Context]
 * @return [Intent]
 */
@Keep
fun startCheckupResultsActivityIntent(ownerContext: Context, checkupOrigin: CheckupOrigin) = Intent(
    ownerContext,
    CheckupResultsActivity::class.java
).apply {
    putExtra(EXTRA_ORIGIN, checkupOrigin)
}

@Keep
enum class CheckupOrigin {
    HOME,
    TEST_BRUSHING,
    GUIDED_BRUSHING
}

private const val EXTRA_ORIGIN: String = "EXTRA_ORIGIN"
