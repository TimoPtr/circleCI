/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.kolibree.R
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectScreenContract
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectScreenResult
import com.kolibree.android.angleandspeed.ui.mindyourspeed.startMindYourSpeedIntent
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.celebration.EarnPointsCelebrationScreenContract
import com.kolibree.android.app.ui.checkup.day.startDayCheckupActivityIntent
import com.kolibree.android.app.ui.checkup.results.CheckupOrigin
import com.kolibree.android.app.ui.checkup.results.CheckupResultsContract
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.app.ui.extention.showInBrowser
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenContract
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenParams
import com.kolibree.android.app.ui.home.mindyourspeed.startscreen.MindYourSpeedStartScreenContract
import com.kolibree.android.app.ui.home.mindyourspeed.startscreen.MindYourSpeedStartScreenParams
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenContract
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.Canceled
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.OpenShop
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.Success
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion.startBrushingStreakCompletionScreen
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.app.ui.home.testbrushing.TestBrushingActivityContract
import com.kolibree.android.app.ui.home.testbrushing.TestBrushingParams
import com.kolibree.android.app.ui.home.testbrushing.startscreen.TestBrushingStartScreenContract
import com.kolibree.android.app.ui.home.testbrushing.startscreen.TestBrushingStartScreenParams
import com.kolibree.android.app.ui.navigation.REQUEST_CODE_GAME
import com.kolibree.android.app.ui.ota.startOtaUpdateScreen
import com.kolibree.android.app.ui.settings.SettingsInitialAction
import com.kolibree.android.app.ui.settings.startSettingsIntent
import com.kolibree.android.app.ui.text.highlightString
import com.kolibree.android.app.ui.toothbrushsettings.ToothbrushSettingsScreenResult
import com.kolibree.android.app.ui.toothbrushsettings.ToothbrushSettingsStartScreenContract
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.guidedbrushing.GuidedBrushingFactory
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentScreenContract
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentScreenResult
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDay
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayScreenContract
import com.kolibree.android.questionoftheday.ui.QuestionOfTheDayScreenResult
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.rewards.smileshistory.startSmilesHistoryScreen
import com.kolibree.android.shop.presentation.checkout.startCheckoutActivity
import java.lang.ref.WeakReference
import javax.inject.Inject
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import zendesk.support.Support
import zendesk.support.guide.HelpCenterActivity

// TODO This class will become quite big. We should try to break it down to delegates.
@Suppress("LargeClass")
internal class HomeNavigatorViewModel(
    private val guidedBrushingFactory: GuidedBrushingFactory,
    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase
) : BaseNavigator<HomeScreenActivity>(), HumHomeNavigator {

    private lateinit var pairingStartScreenContract: ActivityResultLauncher<Unit>
    private lateinit var toothbrushSettingsStartScreenContract: ActivityResultLauncher<String>
    private lateinit var guidedBrushingStartScreenContract: ActivityResultLauncher<GuidedBrushingStartScreenParams>
    private lateinit var mindYourSpeedStartScreenContract: ActivityResultLauncher<MindYourSpeedStartScreenParams>
    private lateinit var testBrushingStartScreenContract: ActivityResultLauncher<TestBrushingStartScreenParams>
    private lateinit var testBrushingScreenContract: ActivityResultLauncher<TestBrushingParams>
    private lateinit var checkupContract: ActivityResultLauncher<CheckupOrigin>
    private lateinit var permissionContract: ActivityResultLauncher<Array<String>>
    private lateinit var locationSettingsContract: ActivityResultLauncher<Unit>
    private lateinit var questionOfTheDayContract: ActivityResultLauncher<QuestionOfTheDay>
    private lateinit var earnPointsCelebrationContract: ActivityResultLauncher<List<CompleteEarnPointsChallenge>>
    private lateinit var amazonConnectContract: ActivityResultLauncher<Unit>
    private lateinit var headspaceMindfulMomentContract: ActivityResultLauncher<HeadspaceMindfulMoment>

    private var mandatoryUpdateDialog: WeakReference<Dialog>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            REQUEST_CODE_GAME -> handleCodeGame(resultCode)
            else -> return false
        }
        return true
    }

    override fun openUrl(url: String) = withOwner {
        showInBrowser(url)
    }

    override fun showSomethingWentWrong() = showSnackbarError(R.string.something_went_wrong)

    override fun showSnackbarError(messageResId: Int) = withOwner {
        showSnackbarError(getString(messageResId))
    }

    override fun showSnackbarError(message: String) = withOwner {
        snackbar(findViewById(R.id.root_content_layout)) {
            message(message)
            duration(Snackbar.LENGTH_LONG)
            anchor(findViewById(R.id.bottom_navigation))
        }.show()
    }

    override fun navigateToCheckup() = navigateToCheckup(CheckupOrigin.HOME)

    override fun navigateToDayCheckup(forDate: OffsetDateTime) = withOwner {
        startActivity(startDayCheckupActivityIntent(this, forDate))
    }

    private fun navigateToCheckup(origin: CheckupOrigin) = withOwner {
        checkupContract.launch(origin)
    }

    override fun showStartMessageView(toothbrushName: String) {
        FailEarly.fail("This case is not supported by HUM (yet)")
    }

    override fun showSettingsScreen(withInitialAction: SettingsInitialAction?) =
        withOwner { startSettingsIntent(this, withInitialAction) }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        registerActivityResultContracts()
    }

    private fun registerActivityResultContracts() = withOwner {
        setupPairingStartScreen()
        setupGuidedBrushingStartScreen()
        setupTestBrushingStartScreen()
        setupTestBrushingScreen()
        setupToothbrushSettingsScreen()
        setupCheckupScreen()
        setupMindYourSpeedStartScreen()
        setupPermissionsScreen()
        setupQuestionOfTheDayScreen()
        setupEarnPointsCelebrationScreen()
        setupAmazonDashScreen()
        setupHeadspaceMindfulMomentScreen()
    }

    private fun HomeScreenActivity.setupCheckupScreen() {
        checkupContract = registerForActivityResult(CheckupResultsContract()) { showSmiles ->
            if (showSmiles) {
                withOwner { showSmileCounter() }
            }
        }
    }

    private fun HomeScreenActivity.setupPermissionsScreen() {
        permissionContract = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                if (permission == Manifest.permission.BLUETOOTH_ADMIN) {
                    withOwner { onBluetoothPermissionRetrieved(isGranted) }
                } else if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    withOwner { onLocationPermissionRetrieved(isGranted) }
                }
            }
        }

        locationSettingsContract = registerForActivityResult(
            ActivityContract(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ) { withOwner { onLocationSettingsClosed() } }
    }

    private fun HomeScreenActivity.setupTestBrushingStartScreen() {
        testBrushingStartScreenContract =
            registerForActivityResult(TestBrushingStartScreenContract()) { result ->
                if (result.shouldProceed && result.param != null) {
                    startTestBrushingActivity(result.param.mac, result.param.model)
                } else {
                    notifyTestBrushingFinished()
                }
            }
    }

    private fun HomeScreenActivity.setupTestBrushingScreen() {
        testBrushingScreenContract =
            registerForActivityResult(TestBrushingActivityContract()) { success ->
                if (success) {
                    navigateToCheckup(origin = CheckupOrigin.TEST_BRUSHING)
                } else {
                    notifyTestBrushingFinished()
                }
            }
    }

    private fun HomeScreenActivity.setupPairingStartScreen() {
        pairingStartScreenContract =
            registerForActivityResult(PairingStartScreenContract()) { result ->
                when (result) {
                    OpenShop -> navigateToShopTab()
                    null, Success, Canceled -> {
                        // no-op
                    }
                }
            }
    }

    private fun HomeScreenActivity.setupGuidedBrushingStartScreen() {
        guidedBrushingStartScreenContract =
            registerForActivityResult(GuidedBrushingStartScreenContract()) { result ->
                result.takeIf { result.shouldProceed }?.param?.let {
                    startGuidedBrushing(it)
                }
            }
    }

    private fun HomeScreenActivity.setupMindYourSpeedStartScreen() {
        mindYourSpeedStartScreenContract =
            registerForActivityResult(MindYourSpeedStartScreenContract()) { result ->
                result.takeIf { result.shouldProceed }?.param?.let { params ->
                    startMindYourSpeed(params)
                }
            }
    }

    private fun HomeScreenActivity.startGuidedBrushing(params: GuidedBrushingStartScreenParams) {
        val guidedBrushing = if (params.isManual) {
            guidedBrushingFactory.createManualGuidedBrushing(this)
        } else {
            guidedBrushingFactory.createConnectedGuidedBrushing(
                context = this,
                macAddress = params.mac,
                model = params.model
            )
        }
        startActivityForResult(guidedBrushing, REQUEST_CODE_GAME)
    }

    private fun HomeScreenActivity.setupToothbrushSettingsScreen() {
        toothbrushSettingsStartScreenContract =
            registerForActivityResult(ToothbrushSettingsStartScreenContract()) { result ->
                if (result == ToothbrushSettingsScreenResult.OpenShop) {
                    navigateToShopTab()
                }
            }
    }

    private fun HomeScreenActivity.setupQuestionOfTheDayScreen() {
        questionOfTheDayContract =
            registerForActivityResult(QuestionOfTheDayScreenContract()) { result ->
                if (result is QuestionOfTheDayScreenResult.Collected) {
                    userExpectsSmilesUseCase.onUserExpectsPoints(result.answerTime.toInstant())
                    showSmileCounter()
                }
            }
    }

    private fun HomeScreenActivity.setupEarnPointsCelebrationScreen() {
        earnPointsCelebrationContract =
            registerForActivityResult(EarnPointsCelebrationScreenContract()) {
                withOwner { onCelebrationScreenClosed() }
            }
    }

    private fun HomeScreenActivity.setupAmazonDashScreen() {
        amazonConnectContract =
            registerForActivityResult(AmazonDashConnectScreenContract()) { result ->
                if (result is AmazonDashConnectScreenResult.Connected) {
                    userExpectsSmilesUseCase.onUserExpectsPoints(result.sendRequestTime.toInstant())
                    showSmileCounter()
                }
            }
    }

    private fun HomeScreenActivity.setupHeadspaceMindfulMomentScreen() {
        headspaceMindfulMomentContract =
            registerForActivityResult(HeadspaceMindfulMomentScreenContract()) { result ->
                if (result is HeadspaceMindfulMomentScreenResult.Collected) {
                    userExpectsSmilesUseCase.onUserExpectsPoints(result.collectionTime.toInstant())
                    showSmileCounter()
                }
            }
    }

    override fun navigateToSetupToothbrushScreen() = withOwner {
        pairingStartScreenContract.launch()
    }

    override fun navigateToMyToothbrushesScreen() {
        FailEarly.fail("This case is not supported by HUM (yet)")
    }

    override fun navigateToToothbrushScreen(mac: String) = withOwner {
        toothbrushSettingsStartScreenContract.launch(mac)
    }

    override fun showOrphanBrushings() = withOwner {
        FailEarly.fail("This case is not supported by HUM (yet)")
    }

    override fun showCheckup(date: LocalDateTime) =
        navigateToDayCheckup(date.atOffset(TrustedClock.systemZoneOffset))

    override fun showNoToothbrushDialog() {
        withOwner {
            HomeAnalytics.noToothbrushConnected()
            snackbar(findViewById(R.id.root_content_layout)) {
                message(R.string.home_error_no_toothbrush_connected)
                duration(Snackbar.LENGTH_LONG)
                anchor(findViewById(R.id.bottom_navigation))
                action(R.string.home_error_no_toothbrush_connected_close) {
                    HomeAnalytics.noToothbrushConnectedClose()
                }
            }.show()
        }
    }

    @Suppress("LongMethod")
    override fun showMandatoryToothbrushUpdateDialog(mac: String, model: ToothbrushModel) {
        if (mandatoryUpdateDialog?.get()?.isShowing == true) {
            return
        }

        withOwner {
            alertDialog(this) {
                featureIcon {
                    drawable(R.drawable.ic_tb_mandatory_ota)
                }
                headlineText {
                    text(R.string.mandatory_ota_dialog_title)
                    gravity(Gravity.CENTER)
                }
                body(R.string.mandatory_ota_dialog_body, Gravity.CENTER)
                containedButton {
                    title(R.string.mandatory_ota_dialog_proceed)
                    action {
                        HomeAnalytics.mandatoryUpdateStart()
                        startOtaUpdateScreen(this@withOwner, true, mac, model)
                        dismiss()
                    }
                }
                textButtonTertiary {
                    title(R.string.cancel)
                    action {
                        HomeAnalytics.mandatoryUpdateCancel()
                        dismiss()
                    }
                }
            }.let { dialog ->
                mandatoryUpdateDialog = WeakReference(dialog)
                dialog.show()
            }
        }
    }

    override fun showNoModelToothbrushDialog() {
        FailEarly.fail("This case is not supported by HUM (yet)")
    }

    override fun showCoachPlus(mac: String, model: ToothbrushModel) {
        withOwner {
            guidedBrushingStartScreenContract.launch(isManual = false, mac = mac, model = model)
        }
    }

    override fun showCoachPlusWithoutStartScreen(mac: String, model: ToothbrushModel) {
        withOwner {
            val guidedBrushing =
                guidedBrushingFactory.createConnectedGuidedBrushing(this, mac, model)
            startActivityForResult(guidedBrushing, REQUEST_CODE_GAME)
        }
    }

    override fun showCoachPlusInManualMode() {
        withOwner {
            guidedBrushingStartScreenContract.launch(isManual = true)
        }
    }

    override fun showTestBrushing(mac: String, model: ToothbrushModel) {
        withOwner {
            testBrushingStartScreenContract.launch(mac = mac, model = model)
        }
    }

    private fun startTestBrushingActivity(
        mac: String,
        model: ToothbrushModel
    ) {
        withOwner {
            testBrushingScreenContract.launch(Pair(mac, model))
        }
    }

    override fun showSpeedControl(mac: String, model: ToothbrushModel) {
        withOwner {
            mindYourSpeedStartScreenContract.launch(MindYourSpeedStartScreenParams(mac, model))
        }
    }

    override fun showTestAngles(mac: String, model: ToothbrushModel) {
        withOwner {
            FailEarly.fail("This case is not supported by HUM (yet)")
        }
    }

    override fun showCoach(mac: String) {
        // no-op, HUM doesn't have legacy Coach
    }

    override fun showCoachInManualMode() {
        // no-op, HUM doesn't have legacy Coach
    }

    private fun startMindYourSpeed(params: MindYourSpeedStartScreenParams) =
        withOwner {
            startActivity(startMindYourSpeedIntent(this, params.mac, params.model))
        }

    override fun showCheckoutScreen() {
        withOwner {
            startCheckoutActivity(this)
        }
    }

    override fun navigateToEditProfile(profileId: Long) {
        FailEarly.fail("This case is not supported by HUM (yet)")
    }

    override fun finishAndNavigateToWelcomeScreen() {
        withOwner {
            FailEarly.fail("This case is not supported by HUM (yet)")
        }
    }

    @Suppress("LongMethod")
    override fun showDeleteBrushingSessionConfirmationDialog(
        deletionConfirmedCallback: () -> Unit,
        deletionCanceledCallback: () -> Unit
    ) = withOwner {
        alertDialog(this) {
            title(R.string.orphan_brushings_delete_title)
            body(R.string.orphan_brushings_delete_message)
            containedButton {
                title(R.string.um_no)
                action {
                    deletionCanceledCallback()
                    dismiss()
                }
            }
            outlinedButton {
                title(R.string.um_yes)
                action {
                    deletionConfirmedCallback()
                    dismiss()
                }
            }
        }.show()
    }

    @Suppress("LongMethod")
    override fun showLowBatteryDialog(toothbrushName: String) = withOwner {
        alertDialog(this) {
            featureIcon {
                drawable(R.drawable.ic_low_battery)
            }
            headlineText {
                text(R.string.dialog_low_battery_title)
                gravity(Gravity.CENTER)
            }
            body(
                highlightString(
                    getString(R.string.dialog_low_battery_description, toothbrushName),
                    toothbrushName
                )
            )
            containedButton {
                title(R.string.ok)
                action { dismiss() }
            }
            dismissAction {
                onLowBatteryDismissed()
            }
        }.show()
    }

    @Suppress("LongMethod")
    override fun showHeadReplacementDialog() = withOwner {
        alertDialog(this) {
            featureIcon {
                drawable(R.drawable.ic_connectivity)
            }
            headlineText {
                text(R.string.dialog_head_replacement_title)
                gravity(Gravity.CENTER)
            }
            body(getString(R.string.dialog_head_replacement_description), Gravity.CENTER)
            containedButton {
                title(R.string.dialog_head_replacement_buy_now_button)
                action {
                    navigateToShopTab()
                    dismiss()
                }
            }
            outlinedButton {
                title(R.string.dialog_head_replacement_dont_remind_button)
                action { dismiss() }
            }
        }.show()
    }

    override fun showChallengeCompletedScreen(smiles: Int) = withOwner {
        startBrushingStreakCompletionScreen(this, smiles)
    }

    override fun navigateTo(intent: Intent) = withOwner {
        startActivity(intent)
    }

    override fun navigateToShopTab() = withOwner {
        openTab(BottomNavigationTab.SHOP)
    }

    override fun openEarningPointsTermsAndConditions() {
        withOwner {
            showInBrowser(R.string.rewards_terms_url)
        }
    }

    override fun launchBluetoothPermission() {
        permissionContract.launch(arrayOf(Manifest.permission.BLUETOOTH_ADMIN))
    }

    override fun launchLocationPermission() {
        permissionContract.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    override fun navigateToLocationSettings() {
        locationSettingsContract.launch()
    }

    override fun showQuestionOfTheDay(question: QuestionOfTheDay) {
        questionOfTheDayContract.launch(question)
    }

    override fun showCelebrationScreen(challenges: List<CompleteEarnPointsChallenge>) {
        earnPointsCelebrationContract.launch(challenges)
    }

    override fun showAmazonDashConnectScreen() = withOwner {
        amazonConnectContract.launch(Unit)
    }

    override fun navigatesToSmilesHistory() {
        withOwner {
            startSmilesHistoryScreen(this)
        }
    }

    override fun showProductSupport() {
        withOwner {
            FailEarly.failInConditionMet(
                !Support.INSTANCE.isInitialized,
                "Zendesk is not initialized"
            )
            HelpCenterActivity.builder()
                .withContactUsButtonVisible(false)
                .show(this)
        }
    }

    override fun showOralCareSupport() {
        withOwner {
            showInBrowser(R.string.oral_care_support_url)
        }
    }

    override fun showMindfulMomentScreen(mindfulMoment: HeadspaceMindfulMoment) = withOwner {
        headspaceMindfulMomentContract.launch(mindfulMoment)
    }

    private fun handleCodeGame(resultCode: Int) {
        if (resultCode != Activity.RESULT_CANCELED) {
            navigateToCheckup(CheckupOrigin.GUIDED_BRUSHING)
        }
    }

    class Factory @Inject constructor(
        private val guidedBrushingFactory: GuidedBrushingFactory,
        private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeNavigatorViewModel(
                guidedBrushingFactory = guidedBrushingFactory,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase
            ) as T
    }
}

internal fun ActivityResultLauncher<Unit>.launch() = launch(Unit)
internal fun ActivityResultLauncher<GuidedBrushingStartScreenParams>.launch(
    isManual: Boolean,
    mac: String? = null,
    model: ToothbrushModel? = null
) = launch(
    GuidedBrushingStartScreenParams(isManual, mac, model)
)

internal fun ActivityResultLauncher<TestBrushingStartScreenParams>.launch(
    mac: String,
    model: ToothbrushModel
) = launch(TestBrushingStartScreenParams(mac, model))

private class ActivityContract(val action: String) : ActivityResultContract<Unit, Unit>() {
    override fun createIntent(context: Context, param: Unit?): Intent {
        return Intent(action)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // no-op
    }
}
