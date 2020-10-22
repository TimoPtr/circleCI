/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.rewards.R
import com.kolibree.android.rewards.databinding.ActivitySmilesHistoryBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class SmilesHistoryActivity :
    BaseMVIActivity<
        SmilesHistoryViewState,
        NoActions,
        SmilesHistoryViewModel.Factory,
        SmilesHistoryViewModel,
        ActivitySmilesHistoryBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<SmilesHistoryViewModel> =
        SmilesHistoryViewModel::class.java

    override fun getLayoutId(): Int =
        R.layout.activity_smiles_history

    override fun execute(action: NoActions) {
        // no-op
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
        super.onBackPressed()
    }

    override fun getScreenName(): AnalyticsEvent = SmilesHistoryAnalytics.main()
}

@Keep
fun startSmilesHistoryScreen(context: Context) {
    SmilesHistoryAnalytics.open()
    context.startActivity(startSmilesHistoryIntent(context))
}

// Used by Espresso to be able to start the activity directly
@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun startSmilesHistoryIntent(context: Context): Intent =
    Intent(context, SmilesHistoryActivity::class.java)
