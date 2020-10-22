/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import android.os.Bundle
import android.widget.Toast
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.dialog.LostConnectionDialog
import com.kolibree.android.coachplus.CoachPlusArgumentProvider
import com.kolibree.android.coachplus.CoachPlusFactoryImpl.Companion.EXTRA_MANUAL_MODE
import com.kolibree.android.coachplus.CoachPlusFactoryImpl.Companion.INTENT_COLOR_SET
import com.kolibree.android.coachplus.R
import com.kolibree.android.coachplus.V1CoachPlusAnalytics
import com.kolibree.android.coachplus.databinding.ActivityCoachPlusBinding
import com.kolibree.android.coachplus.settings.CoachSettingsActivity
import com.kolibree.android.coachplus.ui.CoachPlusBrushingModeDialog
import com.kolibree.android.coachplus.ui.CoachPlusBrushingModeDialogListener
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.coachplus.ui.V1CoachPlusColorSet
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.game.mvi.ConnectionHandlerStateChanged
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererFactory
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import javax.inject.Inject
import timber.log.Timber

internal class CoachPlusActivity :
    BaseMVIActivity<CoachPlusViewState,
        BaseGameAction,
        CoachPlusViewModel.Factory,
        CoachPlusViewModel,
        ActivityCoachPlusBinding>(),
    TrackableScreen,
    CoachPlusBrushingModeDialogListener,
    CoachPlusArgumentProvider {

    @Inject
    internal lateinit var coachPlusRendererFactory: CoachPlusRendererFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCoachPlusRenderer()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun getViewModelClass(): Class<CoachPlusViewModel> = CoachPlusViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_coach_plus

    override fun execute(action: BaseGameAction) {
        when (action) {
            is CoachPlusActions.OpenSettings -> openSettings(action)

            is CoachPlusActions.ShowBrushingModeDialog -> showBrushingModeDialog(action)

            is CoachPlusActions.Cancel -> finishCanceled()
            is CoachPlusActions.SomethingWrong -> onSomethingWentWrong()
            is CoachPlusActions.DataSaved -> finishOk()
            is CoachPlusActions.Restarted -> resetCoachPlusRenderer()
            is ConnectionHandlerStateChanged -> updateLostConnectionDialog(action)
        }
    }

    override fun getScreenName(): AnalyticsEvent = V1CoachPlusAnalytics.main()

    override fun onBrushingModeSelected(brushingMode: BrushingMode) {
        viewModel.onBrushingModeSelected(brushingMode)
    }

    @JvmName("manualMode")
    internal fun manualMode(): Boolean = intent.getBooleanExtra(EXTRA_MANUAL_MODE, false)

    @JvmName("getColorSet")
    internal fun getColorSet(): CoachPlusColorSet =
        intent.getParcelableExtra(INTENT_COLOR_SET) ?: V1CoachPlusColorSet.createDefault(this)

    private fun initCoachPlusRenderer() {
        binding.coachPlusView.renderer =
            coachPlusRendererFactory.createCoachPlusRenderer(readOptionalModelFromIntent())
    }

    private fun finishCanceled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun finishOk() {
        setResult(RESULT_OK)
        finish()
    }

    private fun openSettings(action: CoachPlusActions.OpenSettings) {
        val intent = action.toothbrushMac?.let {
            CoachSettingsActivity.createBrushingSessionIntent(this, it)
        } ?: CoachSettingsActivity.createIntent(this)

        return startActivity(intent)
    }

    private fun onSomethingWentWrong() {
        Timber.d("onSomethingWentWrong")
        Toast.makeText(this, R.string.coach_something_wrong, Toast.LENGTH_LONG).show()
        finishCanceled()
    }

    private fun resetCoachPlusRenderer() {
        binding.coachPlusView.reset()
    }

    private fun showBrushingModeDialog(action: CoachPlusActions.ShowBrushingModeDialog) {
        CoachPlusBrushingModeDialog(
            action.brushingModes, action.currentMode
        ).showIfNotPresent(supportFragmentManager)
    }

    private fun updateLostConnectionDialog(action: ConnectionHandlerStateChanged) {
        LostConnectionDialog.update(
            supportFragmentManager,
            action.state,
            dismissCallback = { finishCanceled() }
        )
    }

    override fun provideManualMode(): Boolean = manualMode()

    override fun provideToothbrushMac(): String? = readMacFromIntent()

    override fun provideToothbrushModel(): ToothbrushModel? = readOptionalModelFromIntent()

    override fun provideColorSet(): CoachPlusColorSet = getColorSet()
}
