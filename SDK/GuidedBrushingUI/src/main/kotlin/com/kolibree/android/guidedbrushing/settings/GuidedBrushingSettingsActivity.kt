/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.guidedbrushing.R
import com.kolibree.android.guidedbrushing.databinding.ActivityGuidedBrushingSettingsBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class GuidedBrushingSettingsActivity :
    BaseMVIActivity<
        GuidedBrushingSettingsViewState,
        NoActions,
        GuidedBrushingSettingsViewModel.Factory,
        GuidedBrushingSettingsViewModel,
        ActivityGuidedBrushingSettingsBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<GuidedBrushingSettingsViewModel> =
        GuidedBrushingSettingsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_guided_brushing_settings

    override fun execute(action: NoActions) {
        // no-op
    }

    fun onMusicChosen(musicUri: Uri) {
        viewModel.onMusicChosen(musicUri)
    }

    override fun onBackPressed() {
        viewModel.onCloseClick()
    }

    override fun getScreenName(): AnalyticsEvent = GuidedBrushingSettingsAnalytics.main()
}

@Keep
fun startGuidedBrushingSettingsIntent(context: Context) {
    context.startActivity(Intent(context, GuidedBrushingSettingsActivity::class.java))
}
