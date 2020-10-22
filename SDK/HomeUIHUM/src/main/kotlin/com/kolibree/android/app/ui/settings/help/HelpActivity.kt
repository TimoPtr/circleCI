/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.help

import android.content.Context
import android.content.Intent
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityHelpBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class HelpActivity :
    BaseMVIActivity<
        EmptyBaseViewState,
        NoActions,
        HelpViewModel.Factory,
        HelpViewModel,
        ActivityHelpBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<HelpViewModel> =
        HelpViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_help

    override fun getScreenName(): AnalyticsEvent = HelpAnalytics.main()

    override fun execute(action: NoActions) {
        // no-op
    }
}

@Suppress("SdkPublicExtensionMethodWithoutKeep")
@VisibleForApp
fun startHelpActivity(context: Context) {
    context.startActivity(Intent(context, HelpActivity::class.java))
}
