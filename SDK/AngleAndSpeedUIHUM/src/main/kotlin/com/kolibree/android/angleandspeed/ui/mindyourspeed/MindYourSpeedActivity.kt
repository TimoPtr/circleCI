/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.android.angleandspeed.ui.R
import com.kolibree.android.angleandspeed.ui.databinding.ActivityMindYourSpeedBinding
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.disconnection.LostConnectionDialogController
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.app.ui.activity.BaseActivity.Companion.INTENT_TOOTHBRUSH_MAC
import com.kolibree.android.app.ui.activity.BaseActivity.Companion.INTENT_TOOTHBRUSH_MODEL
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.MindYourSpeedSnapDotFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.game.mvi.ConnectionHandlerStateChanged
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject

internal class MindYourSpeedActivity :
    BaseMVIActivity<MindYourSpeedViewState,
        BaseGameAction,
        MindYourSpeedViewModel.Factory,
        MindYourSpeedViewModel,
        ActivityMindYourSpeedBinding>(),
    TrackableScreen {

    @Inject
    lateinit var lostConnectionDialogController: LostConnectionDialogController

    @Inject
    lateinit var featureToggles: FeatureToggleSet

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()
        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(rootContentLayout) {
                viewTop.layoutParams.height = topStatusBarWindowInset()
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }
        setupSpeedometer()
    }

    override fun getViewModelClass(): Class<MindYourSpeedViewModel> =
        MindYourSpeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_mind_your_speed

    override fun execute(action: BaseGameAction) {
        when (action) {
            is ConnectionHandlerStateChanged -> updateLostConnectionDialog(action.state)
        }
    }

    private fun updateLostConnectionDialog(state: LostConnectionHandler.State) {
        lostConnectionDialogController.update(state) { finish() }
    }

    override fun getScreenName(): AnalyticsEvent = MindYourSpeedAnalytics.main()

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    private fun setupSpeedometer() {
        with(binding.speedometer) {
            min = MindYouSpeedConstants.SPEEDOMETER_MIN_VALUE.toFloat()
            max = MindYouSpeedConstants.SPEEDOMETER_MAX_VALUE.toFloat()
            perfectMin = MindYouSpeedConstants.SPEEDOMETER_PERFECT_ZONE_MIN_VALUE.toFloat()
            perfectMax = MindYouSpeedConstants.SPEEDOMETER_PERFECT_ZONE_MAX_VALUE.toFloat()
            dotAnimationDuration = MindYouSpeedConstants.SPEEDOMETER_DOT_TRANSITION_DURATION.toMillis()
            snapDotToZones = featureToggles.toggleIsOn(MindYourSpeedSnapDotFeature)
        }
    }
}

@Keep
fun startMindYourSpeedIntent(
    context: Context,
    mac: String,
    model: ToothbrushModel
) = Intent(context, MindYourSpeedActivity::class.java).also { intent ->
    intent.putExtra(INTENT_TOOTHBRUSH_MODEL, model)
    intent.putExtra(INTENT_TOOTHBRUSH_MAC, mac)
}
