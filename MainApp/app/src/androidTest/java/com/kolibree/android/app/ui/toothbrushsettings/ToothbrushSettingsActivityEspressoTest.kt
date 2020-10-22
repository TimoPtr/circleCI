/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.textfield.TextInputLayout
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.offlinebrushings.sync.StartSync
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.espresso_helpers.BindingRecyclerViewActions.bindedScrollTo
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.createGruwareDataFromOtaUpdateType
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import zendesk.support.guide.ViewArticleActivity

@Suppress("LargeClass")
internal class ToothbrushSettingsActivityEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun checkToothbrushSettingsScreen() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE)

        mockLastSyncObservable()

        launchToothbrushSettingsActivity()

        checkBrushHeader()
        checkBatteryLevel()

        scrollToItem(R.string.tb_settings_head_condition_reset_counter)
        checkHeadCondition()

        scrollToItem(R.string.tb_settings_identify)
        checkToothbrushName()
        checkIdentifyBrush()

        scrollToItem(R.string.tb_settings_brush_details_hardware)
        checkDetails()

        scrollToItem(R.string.tb_settings_forget_tb)
        checkHelpCenter()
        checkForgetToothbrush()

        onView(withText(R.string.tb_settings_optional_ota_title)).check(doesNotExist())
    }

    @Test
    fun checkToothbrushSettingsScreenWithOptionalOTA() {
        val gruwareData = createGruwareDataFromOtaUpdateType(context(), OtaUpdateType.STANDARD)

        prepareMocks(
            connectionBuilder = KLTBConnectionBuilder.createAndroidLess(),
            gruwareData = gruwareData
        )

        mockLastSyncObservable()

        launchToothbrushSettingsActivity()

        checkOptionalOTA()
    }

    @Test
    fun checkToothbrushSettingsScreenWithMandatoryOTA() {
        val gruwareData = createGruwareDataFromOtaUpdateType(context(), OtaUpdateType.MANDATORY)

        prepareMocks(
            connectionBuilder = KLTBConnectionBuilder.createAndroidLess().withBootloader(true),
            gruwareData = gruwareData
        )

        mockLastSyncObservable()

        launchActivity()

        // cancel ota mandatory dialog
        onView(withText(R.string.cancel)).perform(click())
        onView(withId(R.id.main_icon)).perform(click())

        checkMandatoryOTA()
    }

    private fun checkOptionalOTA() {
        onView(withText(R.string.tb_settings_optional_ota_title)).check(matches(isDisplayed()))

        // Keep the string to avoid accessing an internal class
        runAndCheckIntent(
            allOf(
                hasComponent("com.kolibree.android.app.ui.ota.OtaUpdateActivity"),
                hasExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC, KLTBConnectionBuilder.DEFAULT_MAC),
                hasExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL, KLTBConnectionBuilder.DEFAULT_MODEL),
                hasExtra("intent_is_mandatory", false)
            )
        ) {
            onView(withText(R.string.tb_settings_optional_ota_proceed))
                .check(matches(isDisplayed()))
                .perform(click())
        }
    }

    private fun checkMandatoryOTA() {
        onView(withText(R.string.tb_settings_mandatory_ota_title)).check(matches(isDisplayed()))

        // Keep the string to avoid accessing an internal class
        runAndCheckIntent(
            allOf(
                hasComponent("com.kolibree.android.app.ui.ota.OtaUpdateActivity"),
                hasExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC, KLTBConnectionBuilder.DEFAULT_MAC),
                hasExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL, KLTBConnectionBuilder.DEFAULT_MODEL),
                hasExtra("intent_is_mandatory", true)
            )
        ) {
            onView(withText(R.string.tb_settings_mandatory_ota_proceed))
                .check(matches(isDisplayed()))
                .perform(click())
        }
    }

    @Test
    fun brushConnectingAndDisconnected() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ESTABLISHING)

        mockLastSyncObservable()

        launchToothbrushSettingsActivity()

        checkToothbrushConnecting()

        EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(
            MAX_CONNECTING_DURATION_SECONDS,
            TimeUnit.SECONDS
        )

        checkToothbrushDisconnected()
    }

    @Test
    fun checkToothbrushSettingsScreenConnectNewBrush() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE)

        mockLastSyncObservable()

        launchToothbrushSettingsActivity()

        checkBrushHeader()

        checkConnectNewBrush()
    }

    private fun checkConnectNewBrush() {
        onView(withText(R.string.tb_settings_connect_new_tb)).check(matches(isDisplayed()))
            .perform(click())

        // check if dialog is visible
        onView(withId(R.id.headline_text)).check(matches(withText(KLTBConnectionBuilder.DEFAULT_NAME)))
        onView(withText(R.string.tb_settings_connect_new_tb_forget_old_one)).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(R.string.tb_settings_forget_tb)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withText(R.string.tb_settings_connect_new_tb)).check(matches(isDisplayed()))
            .perform(click())
    }

    private fun checkForgetToothbrush() {
        onView(withText(R.string.tb_settings_forget_tb))
            .check(matches(isDisplayed())).perform(click())

        // check if dialog is visible
        onView(withId(R.id.title_text)).check(matches(withText(R.string.tb_settings_forget_tb)))
        onView(withText(R.string.ok)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel))
            .check(matches(isDisplayed()))
            .perform(click())

        // check dialog hide
        onView(withText(R.string.ok)).check(doesNotExist())

        onView(withText(R.string.tb_settings_forget_tb))
            .check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.ok)).check(matches(isDisplayed()))
            .perform(click())
    }

    private fun checkToothbrushConnecting() {
        onView(withText(R.string.tb_settings_toolbar_title)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_connecting)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_connect_new_tb)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_waiting)).check(matches(isDisplayed()))
        onView(withId(R.id.tb_last_sync)).check(isGone())
        onView(withId(R.id.not_connecting)).check(isGone())
    }

    private fun checkToothbrushDisconnected() {
        onView(withText(R.string.tb_settings_toolbar_title)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_connecting)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_connect_new_tb)).check(matches(isDisplayed()))
        onView(withId(R.id.tb_last_sync)).check(isGone())

        IdlingResourceFactory.viewVisibility(
            R.id.not_connecting,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.not_connecting)).check(matches(isDisplayed()))

        runAndCheckIntent(hasComponent(ViewArticleActivity::class.java.name)) {
            onView(withId(R.id.not_connecting)).check(matches(isDisplayed()))
                .perform(click())
        }

        onView(withText(R.string.tb_settings_waiting)).check(isGone())
    }

    private fun checkHelpCenter() {
        runAndCheckIntent(hasComponent("com.kolibree.android.app.ui.settings.help.HelpActivity")) {
            onView(withText(R.string.tb_settings_help_center))
                .check(matches(isDisplayed())).perform(click())
        }
    }

    @Suppress("SpreadOperator")
    private fun scrollToItem(@StringRes itemText: Int) {
        onView(withId(R.id.tb_settings_recycler_view))
            .perform(*bindedScrollTo(hasDescendant(withText(itemText))))
    }

    private fun checkHeadCondition() {
        onView(withText(R.string.tb_settings_head_condition_header)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_head_condition_good)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_head_condition_reset_counter))
            .check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_head_condition_buy_new)).check(matches(isDisplayed()))
    }

    private fun checkToothbrushName() {
        onView(withText(R.string.tb_settings_nickname_and_user_header))
            .check(matches(isDisplayed()))
        val toothbrushName = KLTBConnectionBuilder.DEFAULT_NAME
        onView(withText(toothbrushName)).check(matches(isDisplayed()))
        onView(withText(toothbrushName)).perform(click())

        // check rename toothbrush dialog
        onView(withText(R.string.tb_settings_nickname_title)).check(matches(isDisplayed()))
        onView(withText(R.string.save_changes)).check(matches(isDisplayed()))
        val hint = context().getString(R.string.tb_settings_edit_nickname_dialog_hint)
        onView(withId(R.id.text_input_layout)).check(matches(withHint(hint)))
        onView(withText(toothbrushName)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).perform(click())
    }

    private fun checkBatteryLevel() {
        onView(withText(R.string.tb_settings_battery_level)).check(matches(isDisplayed()))
        val batteryLevel = KLTBConnectionBuilder.DEFAULT_BATTERY_LEVEL
        val percentBatteryLevel = "$batteryLevel%"
        onView(withText(percentBatteryLevel)).check(matches(isDisplayed()))
    }

    @Suppress("LongMethod")
    private fun checkDetails() {
        onView(withText(R.string.tb_settings_brush_details_header)).check(matches(isDisplayed()))

        checkDetailItem(
            R.string.tb_settings_brush_details_model,
            KLTBConnectionBuilder.DEFAULT_MODEL.commercialName
        )

        onView(withText(R.string.tb_settings_brush_details_serial)).check(matches(isDisplayed()))

        checkDetailItem(
            R.string.tb_settings_brush_details_mac,
            KLTBConnectionBuilder.DEFAULT_MAC
        )

        checkDetailItem(
            R.string.tb_settings_brush_details_firmware,
            KLTBConnectionBuilder.DEFAULT_FW_VERSION.toString()
        )

        checkDetailItem(
            R.string.tb_settings_brush_details_hardware,
            KLTBConnectionBuilder.DEFAULT_HW_VERSION.toString()
        )
    }

    private fun checkDetailItem(@StringRes title: Int, value: String) {
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(value)).check(matches(isDisplayed()))
    }

    private fun mockLastSyncObservable() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC
        whenever(component().lastSyncObservable().observable())
            .thenReturn(Observable.just(StartSync(mac)))
    }

    private fun checkBrushHeader() {
        onView(withText(R.string.tb_settings_toolbar_title)).check(matches(isDisplayed()))

        val name = KLTBConnectionBuilder.DEFAULT_NAME
        val connectedToothbrush = context().getString(R.string.tb_settings_connected, name)
        onView(withText(connectedToothbrush)).check(matches(isDisplayed()))

        val syncToday = context().getString(R.string.tb_settings_last_sync_today)
        val lastSyncDate = context().getString(R.string.tb_settings_last_sync, syncToday)
        onView(withText(lastSyncDate)).check(matches(isDisplayed()))

        onView(withText(R.string.tb_settings_connect_new_tb)).check(matches(isDisplayed()))
    }

    @Suppress("LongMethod")
    private fun checkIdentifyBrush() {
        onView(withText(R.string.tb_settings_identify)).check(matches(isDisplayed()))
        onView(withText(R.string.tb_settings_blink)).check(matches(isDisplayed()))
        onView(withId(R.id.blink_icon)).check(matches(isDisplayed()))

        whenever(component().pairingAssistant().blinkBlue(any())).thenReturn(Single.just(mock()))

        onView(withId(R.id.blink_toothbrush_view)).check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .perform(click())

        onView(withId(R.id.blink_toothbrush_view)).check(matches(not(isClickable())))

        EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(
            DELAY_BEFORE_BLINKING_SECOND,
            TimeUnit.SECONDS
        )

        IdlingResourceFactory.viewClickable(
            R.id.blink_toothbrush_view,
            true
        ).waitForIdle()

        onView(withId(R.id.blink_toothbrush_view)).check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    private fun launchToothbrushSettingsActivity() {
        launchActivity()
        onView(withId(R.id.main_icon)).perform(click())
    }

    private fun isGone(): ViewAssertion {
        return matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))
    }

    fun withHint(expected: String) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("TextView or TextInputLayout with hint '$expected'")
        }

        override fun matchesSafely(item: View?) =
            item is TextInputLayout && expected == item.hint || item is TextView && expected == item.hint
    }
}

private const val DELAY_BEFORE_BLINKING_SECOND = 10L
private const val MAX_CONNECTING_DURATION_SECONDS = 10L
