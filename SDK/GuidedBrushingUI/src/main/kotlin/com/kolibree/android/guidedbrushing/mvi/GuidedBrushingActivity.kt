/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.mvi

import android.os.Bundle
import android.widget.Toast
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.disconnection.LostConnectionDialogController
import com.kolibree.android.app.insets.bottomNavigationBarInset
import com.kolibree.android.app.insets.topStatusBarWindowInset
import com.kolibree.android.app.insets.withWindowInsets
import com.kolibree.android.coachplus.CoachPlusArgumentProvider
import com.kolibree.android.coachplus.mvi.CoachPlusActions
import com.kolibree.android.coachplus.mvi.CoachPlusViewModel
import com.kolibree.android.coachplus.mvi.CoachPlusViewState
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.game.mvi.ConnectionHandlerStateChanged
import com.kolibree.android.guidedbrushing.BR
import com.kolibree.android.guidedbrushing.GuidedBrushingAnalytics
import com.kolibree.android.guidedbrushing.GuidedBrushingFactoryImpl.Companion.EXTRA_MANUAL_MODE
import com.kolibree.android.guidedbrushing.GuidedBrushingFactoryImpl.Companion.INTENT_COLOR_SET
import com.kolibree.android.guidedbrushing.R
import com.kolibree.android.guidedbrushing.databinding.ActivityGuidedBrushingBinding
import com.kolibree.android.guidedbrushing.timer.GuidedBrushingTimerViewModel
import com.kolibree.android.guidedbrushing.ui.GuidedBrushingColorSet
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject
import timber.log.Timber

internal class GuidedBrushingActivity :
    BaseMVIActivity<CoachPlusViewState,
        BaseGameAction,
        CoachPlusViewModel.Factory,
        CoachPlusViewModel,
        ActivityGuidedBrushingBinding>(),
    TrackableScreen, CoachPlusArgumentProvider {

    @Inject
    lateinit var lostConnectionDialogController: LostConnectionDialogController

    @Inject
    internal lateinit var viewModelFactory: GuidedBrushingTimerViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()

        super.onCreate(savedInstanceState)
        with(binding) {
            withWindowInsets(rootContentLayout) {
                viewTop.layoutParams.height = topStatusBarWindowInset()
                viewBottom.layoutParams.height = bottomNavigationBarInset()
            }
        }

        initTimerViewModel()

        if (manualMode()) {
            // Hack to let the old coach + impl work as before
            viewModel.onManualStart()
        }
    }

    private fun initTimerViewModel() {
        with(viewModelFactory.createAndBindToLifecycle(this, GuidedBrushingTimerViewModel::class.java)) {
            binding.setVariable(BR.timerViewModel, this)
            bindStreams(viewModel.isPlayingStream, viewModel.restartStream)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun getViewModelClass(): Class<CoachPlusViewModel> = CoachPlusViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_guided_brushing

    override fun execute(action: BaseGameAction) {
        when (action) {
            is CoachPlusActions.Cancel -> finishCanceled()
            is CoachPlusActions.SomethingWrong -> onSomethingWentWrong()
            is CoachPlusActions.DataSaved -> finishOk()
            is CoachPlusActions.Restarted -> resetCoachPlusRenderer()
            is ConnectionHandlerStateChanged -> updateLostConnectionDialog(action)
        }
    }

    override fun getScreenName(): AnalyticsEvent = GuidedBrushingAnalytics.main()

    @JvmName("manualMode")
    internal fun manualMode(): Boolean = intent.getBooleanExtra(EXTRA_MANUAL_MODE, false)

    private fun finishCanceled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun finishOk() {
        setResult(RESULT_OK)
        finish()
    }

    private fun onSomethingWentWrong() {
        Timber.d("onSomethingWentWrong")
        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        finishCanceled()
    }

    private fun resetCoachPlusRenderer() {
        binding.coachPlusView.reset()
    }

    private fun updateLostConnectionDialog(action: ConnectionHandlerStateChanged) {
        lostConnectionDialogController.update(action.state) {
            finish()
        }
    }

    override fun provideManualMode(): Boolean = manualMode()

    override fun provideToothbrushMac(): String? = readMacFromIntent()

    override fun provideToothbrushModel(): ToothbrushModel? = readOptionalModelFromIntent()

    override fun provideColorSet(): CoachPlusColorSet =
        intent.getParcelableExtra(INTENT_COLOR_SET) ?: GuidedBrushingColorSet.createDefault(this)
}
