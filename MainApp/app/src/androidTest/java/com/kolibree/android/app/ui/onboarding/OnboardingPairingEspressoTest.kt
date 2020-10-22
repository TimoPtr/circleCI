/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withAlpha
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule
import com.kolibree.android.app.toothbrush.FlavorToothbrushModels
import com.kolibree.android.app.ui.pairing.list.CLEANUP_INTERVAL
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.mock.updateToothbrushResponseWithKolibreePro
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.MockToothbrushScanResult
import com.kolibree.android.test.mocks.toScanResult
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.android.test.utils.webViewIntentWithData
import com.kolibree.pairing.session.PairingSession
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import zendesk.support.guide.ViewArticleActivity

internal class OnboardingPairingEspressoTest : OnboardingActivityEspressoTest() {

    @Test
    @Suppress("FunctionNaming")
    fun pairing_happyPath() {
        val connection = happyPathConnect(provideASupportedToothbrushModel())

        checkSignUpDisplayed()
        makeScreenshot("Onboarding_SignUpScreen")

        completeSignUpWithEmail()

        assertConnectionAssociatedToAccount(connection)
    }

    @Test
    fun enableBluetoothScreen() {
        val bluetoothValve = PublishSubject.create<Boolean>()
        val sdkBuilder = SdkBuilder.create()
            .withBluetoothStateObservable(bluetoothValve)
            .withToothbrushScanResultRelay(BehaviorRelay.create())
            .withBluetoothEnabled(false)

        AppMocker.create()
            .withSdkBuilder(sdkBuilder)
            .prepareForMainScreen()
            .mock()

        whenever(component().checkConnectionPrerequisitesUseCase().checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.BluetoothDisabled)

        goToPairing()

        checkEnableBluetoothScreen()
        makeScreenshot("Onboarding_EnableBluetoothScreen")

        EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        onView(withText(R.string.pairing_enable_bluetooth_button_text)).perform(click())

        whenever(component().bluetoothUtils().isBluetoothEnabled).thenReturn(true)
        whenever(component().checkConnectionPrerequisitesUseCase().checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.ConnectionAllowed)

        bluetoothValve.onNext(true)

        checkWakeYourBrushScreen()
        makeScreenshot("Onboarding_WakeYourBrushScreen")

        pressBack()

        checkGetReadyScreen()
        makeScreenshot("Onboarding_GetReadyScreen")
    }

    @Test
    fun nothingHappeningScreen() {
        SdkBuilder.create()
            .withToothbrushScanResultRelay(BehaviorRelay.create())
            .withBluetoothEnabled(false)
            .build()

        mockNoTbPairingAssistant()

        goToPairing()

        checkWakeYourBrushScreen()

        checkNothingHappening()

        onView(withId(R.id.nothing_happening_button))
            .perform(click())

        checkIsBrushReady()
        makeScreenshot("Onboarding_IsBrushReadyScreen")

        checkNeedHelp()

        pressBack()

        checkNothingHappening()

        onView(withId(R.id.nothing_happening_button))
            .perform(click())

        checkIsBrushReady()

        onView(withId(R.id.connect_my_brush_button))
            .perform(click())

        checkWakeYourBrushScreen()

        pressBack()

        onView(withId(R.id.connect_brush_button)).check(matches(isDisplayed()))
    }

    @Test
    fun modelMismatchScreen() {
        happyPathConnect(provideANonSupportedToothbrushModel())

        IdlingResourceFactory.viewVisibility(
            R.id.switch_app_image,
            View.VISIBLE
        ).waitForIdle()

        checkModelMismatchScreen()
        makeScreenshot("Onboarding_ModelMismatchScreen")

        pressBack()

        checkBrushFoundScreen()
        makeScreenshot("Onboarding_BrushFoundScreen")

        onView(withId(R.id.connect_brush_button))
            .check(matches(isDisplayed()))
            .perform(click())

        advanceTimeBySeconds(2)

        IdlingResourceFactory.viewVisibility(
            R.id.switch_app_image,
            View.VISIBLE
        ).waitForIdle()

        checkModelMismatchScreen()

        checkChangeApp()

        onView(withId(R.id.continue_anyway_button))
            .check(matches(isDisplayed()))
            .perform(click())

        checkSignUpDisplayed()
    }

    @Test
    fun locationScreens() {
        AppMocker.create()
            .withSdkBuilder(SdkBuilder.create())
            .withLocationEnabled(false)
            .withLocationPermissionGranted(false)
            .prepareForMainScreen()
            .mock()

        goToPairing()

        checkGrantPermissionScreen()
        makeScreenshot("Onboarding_GrantPermissionScreen")
        whenever(component().locationStatus().shouldAskPermission()).thenReturn(false)
        whenever(component().checkConnectionPrerequisitesUseCase().checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.LocationServiceDisabled)
        onView(withId(R.id.location_button)).perform(click())

        checkEnableLocationScreen()
        makeScreenshot("Onboarding_EnableLocationScreen")
        whenever(component().locationStatus().shouldEnableLocation()).thenReturn(false)
        whenever(component().pairingAssistant().scannerObservable()).thenReturn(Observable.never())
        whenever(component().checkConnectionPrerequisitesUseCase().checkConnectionPrerequisites())
            .thenReturn(ConnectionPrerequisitesState.ConnectionAllowed)
        onView(withId(R.id.location_button)).perform(click())

        checkWakeYourBrushScreen()

        pressBack()

        checkGetReadyScreen()
    }

    @Suppress("LongMethod")
    @Test
    fun scanToothbrushListScreen_happyPath() {
        val connection = KLTBConnectionBuilder.createWithDefaultState()
            .withModel(provideASupportedToothbrushModel())
            .build()

        val scanResultRelay = BehaviorRelay.create<ToothbrushScanResult>()
        SdkBuilder.create()
            .withToothbrushScanResultRelay(scanResultRelay)
            .build()

        val scanResult = connection.toScanResult()

        mockPairingAssistant(connection, scanResult)

        mockToothbrushConfirm()

        goToPairing()

        scanResultRelay.accept(scanResult)

        onView(withId(R.id.not_right_brush_button)).perform(click())

        checkScanToothbrushList()
        makeScreenshot("Onboarding_ScanToothbrushListScreen")

        onView(allOf(withId(R.id.name), withText(scanResult.name)))
            .check(matches(isDisplayed()))
            .perform(click())

        checkSignUpDisplayed()

        completeSignUpWithEmail()

        assertConnectionAssociatedToAccount(connection)
    }

    private fun mockToothbrushConfirm() {
        val response = updateToothbrushResponseWithKolibreePro()!!
        whenever(component().kolibreeConnector().updateToothbrush(any()))
            .thenReturn(Single.just(response))
    }

    @Suppress("LongMethod")
    @Test
    fun scanToothbrushListScreen_bothPaths_thenNoResultsDialog() {
        val nonValidModelConnection = KLTBConnectionBuilder.createWithDefaultState()
            .withModel(provideANonSupportedToothbrushModel())
            .build()

        val scanResultRelay = BehaviorRelay.create<ToothbrushScanResult>()
        SdkBuilder.create()
            .withToothbrushScanResultRelay(scanResultRelay)
            .build()

        val nonValidModelScanResult = nonValidModelConnection.toScanResult()

        mockPairingAssistant(nonValidModelConnection, nonValidModelScanResult)

        goToPairing()

        scanResultRelay.accept(nonValidModelScanResult)

        onView(withId(R.id.not_right_brush_button)).perform(click())

        checkScanToothbrushList()

        onView(allOf(withId(R.id.name), withText(nonValidModelScanResult.name)))
            .check(matches(isDisplayed()))
            .perform(click())

        checkModelMismatchScreen()

        pressBack()

        advanceSecondsInScanListScreen(CLEANUP_INTERVAL.seconds)

        val validModelConnection = KLTBConnectionBuilder.createWithDefaultState()
            .withModel(provideASupportedToothbrushModel())
            .withName("Super hum")
            .build()

        val validModelScanResult = validModelConnection.toScanResult()
        mockPairingAssistant(validModelConnection, validModelScanResult)

        onView(allOf(withId(R.id.name), withText(validModelScanResult.name)))
            .check(doesNotExist())

        scanResultRelay.accept(validModelScanResult)

        IdlingResourceFactory.textViewContent(
            R.id.name,
            validModelScanResult.name
        ).waitForIdle()

        onView(allOf(withId(R.id.name), withText(validModelScanResult.name)))
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.name), withText(validModelScanResult.name)))
            .perform(click())

        checkSignUpDisplayed()

        pressBack()

        advanceSecondsInScanListScreen(SEVEN_BLUETOOTH_ADVERTISING_WINDOWS)

        IdlingResourceFactory.viewVisibility(
            R.id.no_brush_found_card,
            View.VISIBLE
        ).waitForIdle()

        checkNoBrushFound()
        makeScreenshot("Onboarding_NoBrushFoundScreen")

        onView(withText(R.string.pairing_no_brush_dialog_get_it))
            .perform(click())

        onView(withText(R.string.pairing_no_brush_dialog_get_it))
            .check(matches(not(isDisplayed())))
    }

    /*
    utils
     */

    private fun checkModelMismatchScreen() {
        onView(withText(R.string.pairing_model_mismatch_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_model_mismatch_subtitle))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_model_mismatch_switch_app))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_model_mismatch_continue_anyway))
            .check(matches(isDisplayed()))
        onView(withId(R.id.switch_app_image))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_switch_app)))
    }

    private fun happyPathConnect(toothbrushModel: ToothbrushModel = ToothbrushModel.HUM_BATTERY): KLTBConnection {
        val connection = KLTBConnectionBuilder.createWithDefaultState()
            .withModel(toothbrushModel)
            .build()

        val scanResultRelay = BehaviorRelay.create<ToothbrushScanResult>()
        SdkBuilder.create()
            .withToothbrushScanResultRelay(scanResultRelay)
            .build()

        val scanResult = MockToothbrushScanResult(
            mac = connection.toothbrush().mac,
            model = connection.toothbrush().model
        )

        mockPairingAssistant(connection, scanResult)

        mockToothbrushConfirm()

        goToPairing()

        checkWakeYourBrushScreen()

        onView(withId(R.id.connect_brush_button)).check(doesNotExist())

        scanResultRelay.accept(scanResult)

        checkBrushFoundScreen()

        onView(withId(R.id.connect_brush_button))
            .check(matches(isDisplayed()))
            .perform(click())

        advanceTimeBySeconds(2)

        return connection
    }

    private fun checkNeedHelp() {
        runAndCheckIntent(hasComponent(ViewArticleActivity::class.java.name)) {
            // Action
            onView(withId(R.id.need_help_button))
                .perform(click())
        }
    }

    private fun checkChangeApp() {
        runAndCheckIntent(
            webViewIntentWithData(
                context(),
                R.string.change_app_play_store_url
            )
        ) {
            onView(withId(R.id.change_app_button))
                .perform(click())
        }
    }

    private fun checkIsBrushReady() {
        onView(withText(R.string.pairing_is_brush_ready_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_is_brush_ready_subtitle))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_is_brush_ready_connect))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_is_brush_ready_help))
            .check(matches(isDisplayed()))
    }

    private fun checkNothingHappening() {
        advanceTimeBySeconds(10)

        IdlingResourceFactory.viewVisibility(
            R.id.nothing_happening_button,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.nothing_happening_button))
            .check(matches(withAlpha(1.0f)))
    }

    private fun checkNoBrushFound() {
        onView(withText(R.string.pairing_no_brush_dialog_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_content1))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_content2))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_content3))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_content4))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_content5))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_content6))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_no_brush_dialog_get_it))
            .check(matches(isDisplayed()))
    }

    private fun checkScanToothbrushList() {
        onView(withText(R.string.pairing_scan_toothbrush_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_scan_toothbrush_description))
            .check(matches(isDisplayed()))
    }

    private fun checkBrushFoundScreen() {
        onView(withText(R.string.pairing_brush_found_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_brush_found_subtitle))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_brush_found_connect))
            .check(matches(isDisplayed()))
        onView(withText(R.string.um_no))
            .check(matches(isDisplayed()))
    }

    private fun checkGrantPermissionScreen() {
        onView(withText(R.string.pairing_grant_location_permission_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_grant_location_permission_description))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_grant_location_permission_action))
            .check(matches(isDisplayed()))
    }

    private fun checkEnableLocationScreen() {
        onView(
            allOf(
                withId(R.id.location_title),
                withText(R.string.pairing_enable_location_title)
            )
        ).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.location_description),
                withText(R.string.pairing_enable_location_description)
            )
        ).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.location_button),
                withText(R.string.pairing_enable_location_action)
            )
        ).check(matches(isDisplayed()))
    }

    private fun checkWakeYourBrushScreen() {
        onView(withId(R.id.nothing_happening_button))
            .check(matches(not(isDisplayed())))
        onView(withText(R.string.pairing_wake_your_brush_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_wake_your_brush_subtitle))
            .check(matches(isDisplayed()))
    }

    private fun checkGetReadyScreen() {
        onView(withText(R.string.onboarding_get_ready_subtitle))
            .check(matches(isDisplayed()))
        onView(withText(R.string.onboarding_get_ready_connect_brush_button))
            .check(matches(isDisplayed()))
        onView(withText(R.string.onboarding_get_ready_no_brush_button))
            .check(matches(isDisplayed()))
    }

    private fun checkEnableBluetoothScreen() {
        onView(withText(R.string.pairing_enable_bluetooth_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_enable_bluetooth_description))
            .check(matches(isDisplayed()))
        onView(withText(R.string.pairing_enable_bluetooth_button_text))
            .check(matches(isDisplayed()))
        onView(withId(R.id.enable_bluetooth_logo))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_enable_bluetooth)))
    }

    private fun checkSignUpDisplayed() {
        IdlingResourceFactory.viewVisibility(
            R.id.sign_up_container,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.sign_up_container)).check(matches(isDisplayed()))
    }

    private fun goToPairing() {
        launchActivity()
        onView(withId(R.id.connect_brush_button)).perform(click())
    }

    private fun mockPairingAssistant(
        connection: KLTBConnection,
        result: ToothbrushScanResult = connection.toScanResult()
    ) {
        whenever(component().bluetoothUtils().isBluetoothEnabled).thenReturn(true)

        whenever(component().pairingAssistant().connectAndBlinkBlue(result))
            .thenReturn(Single.just(connection))
        whenever(component().pairingAssistant().unpair(any())).thenReturn(Completable.complete())

        val session = mock<PairingSession> {
            whenever(it.connection()).thenReturn(connection)
        }
        whenever(component().pairingAssistant().pair(result)).thenReturn(Single.just(session))

        whenever(
            component().pairingAssistant().pair(
                connection.toothbrush().mac,
                connection.toothbrush().model,
                connection.toothbrush().getName()
            )
        )
            .thenReturn(Single.just(session))
    }

    private fun mockNoTbPairingAssistant() {
        whenever(component().bluetoothUtils().isBluetoothEnabled).thenReturn(true)

        whenever(component().pairingAssistant().unpair(any())).thenReturn(Completable.complete())
    }

    /*
     * Scan List uses TrustedClock to determine if a Scan result has expired
     *
     * If we don't advance time along with the scheduler, things don't work
     */
    private fun advanceSecondsInScanListScreen(seconds: Long) {
        val now = TrustedClock.getNowZonedDateTime()
        TrustedClock.setFixedDate(now.plusSeconds(seconds))

        advanceTimeBySeconds(seconds)
    }

    private fun assertConnectionAssociatedToAccount(connection: KLTBConnection) {
        verify(component().pairingAssistant()).pair(
            connection.toothbrush().mac,
            connection.toothbrush().model,
            connection.toothbrush().getName()
        )
    }

    private fun provideASupportedToothbrushModel() =
        FlavorToothbrushModels.defaultSupportedModels().first()

    private fun provideANonSupportedToothbrushModel() =
        ToothbrushModel.values().subtract(FlavorToothbrushModels.defaultSupportedModels()).first()
}

private const val SEVEN_BLUETOOTH_ADVERTISING_WINDOWS = 35L
