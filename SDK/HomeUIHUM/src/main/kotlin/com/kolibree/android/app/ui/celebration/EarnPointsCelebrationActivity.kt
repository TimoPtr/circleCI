/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import android.os.Bundle
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.ActivityCelebrationBinding
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class EarnPointsCelebrationActivity : BaseMVIActivity<
    EarnPointsCelebrationViewState,
    BaseAction,
    EarnPointsCelebrationViewModel.Factory,
    EarnPointsCelebrationViewModel,
    ActivityCelebrationBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<EarnPointsCelebrationViewModel> =
        EarnPointsCelebrationViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_celebration

    override fun execute(action: BaseAction) = Unit

    override fun getScreenName(): AnalyticsEvent = EarnPointsCelebrationAnalytics.main()

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
    }

    fun extractChallenges(): List<CompleteEarnPointsChallenge> {
        return intent.getParcelableArrayListExtra(EXTRA_CHALLENGES)
            ?: error("Missing argument")
    }
}

const val EXTRA_CHALLENGES = "EXTRA_CHALLENGES"
