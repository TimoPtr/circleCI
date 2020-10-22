/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import android.content.Intent
import android.widget.Button
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kolibree.R
import com.kolibree.android.app.AppConfigurationImpl
import com.kolibree.android.app.dagger.EspressoSystemNotificationsEnabledModule
import com.kolibree.android.app.ui.home.BottomNavigationTab
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.test.espresso_helpers.BindingRecyclerViewActions.bindedScrollTo
import com.kolibree.android.test.espresso_helpers.CustomActions.clickBottomCenter
import com.kolibree.android.test.espresso_helpers.CustomActions.clickTopCenter
import com.kolibree.android.test.scrollToBottom
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.android.test.utils.webViewIntentWithData
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.junit.Ignore
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import zendesk.support.guide.HelpCenterActivity

@Suppress("LargeClass")
internal class SettingsActivityEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    @Suppress("LongMethod")
    fun checkSettingsScreen() {
        prepareMocks()

        launchSettingsActivity()

        onView(withId(R.id.toolbar_name)).check(matches(withText(R.string.settings_screen_title)))
        onView(withId(R.id.toolbar_name)).check(matches(isDisplayed()))
        makeScreenshot("SettingsScreen")
        checkBirthDate()
        checkEmail()
        checkWeeklyDigest()
        if (AppConfigurationImpl.allowDisablingDataSharing) {
            checkShareYourData()
        }
        checkBrushingTime()
        checkNotifications()
        checkGetMyData()
        checkAmazonDrs(false)
        checkAdminSettingsItem()
        checkAboutItem()
        checkHelpItem()
        checkRateOurAppItem()
        checkTermsAndConditionsItem()
        checkPrivacyPolicyItem()
        checkDeleteAccountItem()
        checkLogoutItem()
    }

    @Test
    fun checkAmazonDrsUnlink() {
        prepareMocks(amazonDrsEnabled = true)

        launchSettingsActivity()

        checkAmazonDrs(true)
    }

    @Test
    fun clickOnRateTheApp_opensPlayStoreOnPackageName() {
        prepareMocks()

        launchSettingsActivity()

        scrollToSettingsItem(R.string.settings_rate_our_app)

        val expectedUrl = "https://play.google.com/store/apps/details?id=${context().packageName}"
        runAndCheckIntent(webViewIntentWithData(expectedUrl)) {
            onView(withText(R.string.settings_rate_our_app)).perform(click())
        }
    }

    /*
    Utils
     */

    private fun checkAmazonDrs(isEnabled: Boolean) {
        scrollToSettingsItem(R.string.settings_link_amazon_dash_title)

        if (isEnabled) {
            onView(withText(R.string.settings_amazon_account_linked)).check(matches(isDisplayed()))
            onView(withText(R.string.settings_link)).check(matches(not(isDisplayed())))
        } else {
            onView(withText(R.string.settings_link_amazon_account)).check(matches(isDisplayed()))

            runAndCheckIntent(hasComponent("com.kolibree.android.amazondash.ui.connect.AmazonDashConnectActivity")) {
                onView(withText(R.string.settings_link)).check(matches(isDisplayed()))
                    .perform(click())
            }
        }
    }

    private fun checkShareYourData() {
        scrollToSettingsItem(R.string.settings_share_your_data_title)

        onView(withText(R.string.settings_share_your_data_title)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_share_your_data_description)).check(matches(isDisplayed()))

        runAndCheckIntent(hasData(context().getString(R.string.privacy_url))) {
            onView(withText(R.string.settings_share_your_data_learn_more))
                .check(matches(isDisplayed())).perform(click())
        }
    }

    private fun checkGetMyData() {
        scrollToSettingsItem(R.string.settings_get_my_data)
        onView(withText(R.string.settings_get_my_data))
            .check(matches(isDisplayed()))
    }

    private fun checkNotifications() {
        scrollToSettingsItem(R.string.settings_notifications_item)
        onView(withText(R.string.settings_notifications_item))
            .check(matches(isDisplayed()))
    }

    private fun checkWeeklyDigest() {
        scrollToSettingsItem(R.string.settings_weekly_digest_title)
        onView(withText(R.string.settings_weekly_digest_title)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_weekly_digest_description)).check(matches(isDisplayed()))
    }

    private fun checkEmail() {
        scrollToSettingsItem(R.string.email)
        onView(withText(R.string.email)).check(matches(isDisplayed()))
        onView(withText(EMAIL)).check(matches(isDisplayed())).check(matches(not(isClickable())))
    }

    private fun checkBirthDate(expectedString: String = context().getString(R.string.unknown)) {
        onView(withText(R.string.settings_born_header)).check(matches(isDisplayed()))
        onView(allOf(hasSibling(withText(R.string.settings_born_header)), withText(expectedString)))
            .check(matches(isDisplayed()))
    }

    private fun checkBrushingTime() {
        scrollToSettingsItem(R.string.settings_brushing_time)
        onView(withText(R.string.settings_brushing_time)).check(matches(isDisplayed()))
        onView(withText("2 minutes")).check(matches(isDisplayed()))

        onView(withText(R.string.settings_brushing_time)).perform(click())
        incrementSeconds()
        onView(withText(R.string.save_changes)).perform(click())

        onView(withText("2 minutes 05 seconds")).check(matches(isDisplayed()))
    }

    private fun checkLogoutItem() {
        scrollToEnd()
        onView(withText(R.string.settings_item_logout)).perform(click())

        // check if dialog is visible
        onView(withId(R.id.title_text)).check(matches(withText(R.string.settings_logout_title)))
        onView(withText(R.string.settings_logout_message)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).check(matches(isDisplayed()))

        runAndCheckIntent(hasComponent(OnboardingActivity::class.java.name)) {
            whenever(component().kolibreeConnector().logout()).thenReturn(Completable.complete())
            onView(allOf(instanceOf(Button::class.java), withText(R.string.settings_logout_title)))
                .perform(click())
        }
    }

    private fun checkDeleteAccountItem() {
        scrollToEnd()
        onView(withText(R.string.settings_item_delete_account)).perform(click())

        // check if dialog is visible
        onView(withId(R.id.title_text)).check(matches(withText(R.string.settings_delete_account)))
        onView(withText(R.string.settings_delete_account_popup_message)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_delete_account_popup_yes)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).perform(click())
    }

    private fun checkAdminSettingsItem() {
        val description = context().getString(R.string.settings_item_admin_settings)
        onView(withText(description.toUpperCase())).check(matches(isDisplayed()))
    }

    private fun checkAboutItem() {
        scrollToSettingsItem(R.string.settings_item_about)
        onView(withText(R.string.settings_item_about)).check(matches(isDisplayed()))
    }

    private fun checkHelpItem() {
        scrollToSettingsItem(R.string.settings_help_title)
        onView(withText(R.string.settings_help_title)).check(matches(isDisplayed()))
    }

    private fun checkRateOurAppItem() {
        scrollToSettingsItem(R.string.settings_rate_our_app)
        onView(withText(R.string.settings_rate_our_app)).check(matches(isDisplayed()))
    }

    private fun checkTermsAndConditionsItem() {
        scrollToSettingsItem(R.string.settings_terms_of_use_terms)
        onView(withText(R.string.settings_terms_of_use_terms)).check(matches(isDisplayed()))

        runAndCheckIntent(webViewIntentWithData(context(), R.string.terms_url)) {
            onView(withText(R.string.settings_terms_of_use_terms)).check(matches(isDisplayed()))
                .perform(click())
        }
    }

    private fun checkPrivacyPolicyItem() {
        scrollToSettingsItem(R.string.settings_terms_of_use_policy)
        onView(withText(R.string.settings_terms_of_use_policy)).check(matches(isDisplayed()))

        runAndCheckIntent(webViewIntentWithData(context(), R.string.privacy_url)) {
            onView(withText(R.string.settings_terms_of_use_policy)).check(matches(isDisplayed()))
                .perform(click())
        }
    }

    private fun scrollToEnd() {
        onView(withId(R.id.settings_recycler_view)).perform(scrollToBottom())
    }

    @Test
    fun checkGenderItem() {
        prepareMocks()
        launchSettingsActivity()

        // gender in profile
        val defaultGenderRes = R.string.settings_profile_gender_male

        // initialized with profile info
        onView(withText(R.string.settings_profile_information_gender_hint)).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(defaultGenderRes)).check(matches(isDisplayed()))

        checkGenderDialog()

        // check cancel change
        onView(withText(R.string.settings_profile_gender_female)).perform(click())
        onView(withText(R.string.cancel)).perform(click())
        onView(withText(defaultGenderRes)).check(matches(isDisplayed()))

        // check changes
        checkChangeGender(R.string.settings_profile_gender_female)
        checkChangeGender(R.string.gender_prefer_not_to_answer)
        checkChangeGender(R.string.settings_profile_gender_male)
    }

    private fun checkChangeGender(newGenderRes: Int) {
        checkGenderDialog()
        onView(withText(newGenderRes)).perform(click())
        onView(withText(R.string.save_changes)).perform(click())
        onView(
            allOf(
                withText(newGenderRes),
                hasSibling(withText(R.string.settings_profile_information_gender_hint))
            )
        )
            .check(matches(isDisplayed()))
    }

    private fun checkGenderDialog() {
        onView(withText(R.string.settings_profile_information_gender_hint)).perform(click())

        onView(withText(R.string.settings_profile_information_gender_hint)).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(R.string.settings_profile_gender_male)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_profile_gender_female)).check(matches(isDisplayed()))
        onView(withText(R.string.gender_prefer_not_to_answer)).check(matches(isDisplayed()))
        onView(withText(R.string.save_changes)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).check(matches(isDisplayed()))
    }

    @Test
    fun checkHandednessItem() {
        prepareMocks()
        launchSettingsActivity()

        // handedness in profile
        val defaultHandednessRes = R.string.settings_profile_handedness_left

        // initialized with profile info
        onView(withText(R.string.settings_profile_information_handedness_hint)).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(defaultHandednessRes)).check(matches(isDisplayed()))

        checkHandednessDialog()

        // check cancel change
        onView(withText(R.string.settings_profile_handedness_right)).perform(click())
        onView(withText(R.string.cancel)).perform(click())
        onView(withText(defaultHandednessRes)).check(matches(isDisplayed()))

        // check changes
        checkChangeHandedness(R.string.settings_profile_handedness_right)
        checkChangeHandedness(R.string.settings_profile_handedness_left)
    }

    private fun checkChangeHandedness(newHandednessRes: Int) {
        checkHandednessDialog()
        onView(withText(newHandednessRes)).perform(click())
        onView(withText(R.string.save_changes)).perform(click())
        onView(withText(newHandednessRes)).check(matches(isDisplayed()))
    }

    private fun checkHandednessDialog() {
        onView(withText(R.string.settings_profile_information_handedness_hint)).perform(click())

        onView(withText(R.string.settings_profile_information_handedness_hint)).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(R.string.settings_profile_handedness_left)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_profile_handedness_right)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_profile_handedness_right)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).check(matches(isDisplayed()))
    }

    @Test
    fun startAndCheckAboutScreen() {
        prepareMocks()
        launchSettingsActivity()
        launchAboutActivity()
        checkAboutScreen()
    }

    private fun checkAboutScreen() {
        onView(withText(R.string.settings_about_screen_description)).check(matches(isDisplayed()))
        checkIsDisplayed(R.string.settings_about_screen_facebook)
        checkIsDisplayed(R.string.settings_about_screen_instagram)
        checkIsDisplayed(R.string.settings_about_screen_website)
        checkIsDisplayed(R.string.settings_about_screen_app_version)
        checkIsDisplayed(R.string.settings_about_screen_account_id)
        runAndCheckIntent(hasComponent(OssLicensesMenuActivity::class.java.name)) {
            checkIsDisplayed(R.string.settings_about_screen_licenses)
            onView(withText(R.string.settings_about_screen_licenses))
                .perform(click())
        }
    }

    @Test
    fun startAndCheckHelpScreen() {
        prepareMocks()
        launchSettingsActivity()
        launchHelpActivity()
        checkHelpScreen()
    }

    private fun checkHelpScreen() {
        onView(withText(R.string.help_description)).check(matches(isDisplayed()))

        runAndCheckIntent(hasComponent(HelpCenterActivity::class.java.name)) {
            onView(withText(R.string.help_center)).check(matches(isDisplayed()))
                .perform(click())
        }

        runAndCheckIntent(webViewIntentWithData(context(), R.string.contact_us_url)) {
            onView(withText(R.string.contact_us)).check(matches(isDisplayed()))
                .perform(click())
        }
    }

    @Test
    fun startAndCheckNotificationsScreen() {
        prepareMocks(isNewsletterSubscribed = true)
        launchSettingsActivity()
        launchNotificationsActivity()
        checkNotificationsScreen()
    }

    private fun checkNotificationsScreen() {
        onView(withText(R.string.notifications_toolbar_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_brush_reminder_header))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_brush_reminder_item))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_brush_reminder_description))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_mailing_list_header))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_mailing_list_item))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_mailing_list_description))
            .check(matches(isDisplayed()))
    }

    @Test
    fun startAndCheckNotificationsScreen_whenNotificationsDisabled() {
        prepareMocks()

        val mock = EspressoSystemNotificationsEnabledModule.systemNotificationsEnabledMock
        whenever(mock.areNotificationsEnabled()).thenReturn(false)

        launchSettingsActivity()
        launchNotificationsActivity()

        onView(withText(R.string.notifications_brush_reminder_item))
            .check(matches(isNotChecked()))
        onView(withText(R.string.notifications_brush_reminder_item))
            .perform(click())
        checkNotificationDisabledDialog()
    }

    private fun checkNotificationDisabledDialog() {
        onView(withText(R.string.notifications_disabled_dialog_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_disabled_go_to_settings))
            .check(matches(isDisplayed()))
        onView(withText(R.string.notifications_disabled_cancel))
            .check(matches(isDisplayed()))
    }

    @Test
    fun checkEditFirstName() {
        prepareMocks()
        launchSettingsActivity()
        checkBrusherDetailsSection()
        checkAndClickFirstName()
        checkEditFirstNameDialogIsDisplayed()
        checkSaveIsEnabled()
        onView(withText(PROFILE_NAME))
            .perform(replaceText(""))
        checkSaveIsDisabled()
    }

    @Test
    @Suppress("MagicNumber")
    fun checkEditBirthDate() {
        prepareMocks(birthDate = LocalDate.of(2000, 11, 18))
        launchSettingsActivity()
        checkBirthDate("Nov 2000")

        clickEditBirthDate()
        dialogShowsDate("Nov", "2000")

        incrementMonth()
        dialogShowsDate("Dec", "2000")

        incrementMonth()
        dialogShowsDate("Jan", "2000")

        incrementYear()
        dialogShowsDate("Jan", "2001")

        onSaveButton().perform(click())

        checkBirthDate("Jan 2001")
    }

    private fun clickEditBirthDate() {
        onView(withText(R.string.settings_born_header))
            .perform(click())
    }

    private fun incrementYear(count: Int = 1) {
        incrementPicker(R.id.picker_minor, count)
    }

    private fun incrementMonth(count: Int = 1) {
        incrementPicker(R.id.picker_major, count)
    }

    private fun dialogShowsDate(month: String, year: String) {
        onView(withParent(withId(R.id.picker_major)))
            .check(matches(withText(month)))
        onView(withParent(withId(R.id.picker_minor)))
            .check(matches(withText(year)))
    }

    private fun checkBrusherDetailsSection() {
        onView(withText("PETER'S BRUSHER DETAILS"))
            .check(isVisible())
    }

    private fun checkAndClickFirstName() {
        onView(withText(R.string.settings_profile_information_name))
            .check(isVisible())
            .perform(click())
    }

    private fun checkEditFirstNameDialogIsDisplayed() {
        onView(withText("First Name"))
            .check(isVisible())
        onView(withId(R.id.text_input_layout))
            .check(isVisible())
        onView(withText("Peter"))
            .check(isVisible())
        onSaveButton()
            .check(isVisible())
        onView(withText("Cancel"))
            .check(isVisible())
    }

    private fun onSaveButton() =
        onView(withText(R.string.save_changes))

    private fun checkSaveIsEnabled() {
        onSaveButton()
            .check(matches(isEnabled()))
    }

    private fun checkSaveIsDisabled() {
        onSaveButton()
            .check(matches(not(isEnabled())))
    }

    private fun checkIsDisplayed(@StringRes title: Int) {
        onView(withText(title)).perform(scrollTo())
        onView(withText(title)).check(isVisible())
    }

    private fun launchAboutActivity() {
        clickOnSettingsItem(R.string.settings_item_about)
    }

    private fun launchHelpActivity() {
        clickOnSettingsItem(R.string.settings_help_title)
    }

    private fun launchNotificationsActivity() {
        clickOnSettingsItem(R.string.settings_notifications_item)
    }

    private fun launchSettingsActivity() {
        launchActivity()

        bottomNavigationTo(BottomNavigationTab.PROFILE)
        clickSettingsButton()
    }

    private fun clickSettingsButton() {
        onView(withId(R.id.profile_settings_button)).perform(click())
    }

    private fun isVisible(): ViewAssertion {
        return matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
    }

    @Test
    fun startAndCheckGuidedBrushingSettingsScreen() {
        prepareMocks()
        launchSettingsActivity()
        launchGuidedBrushingSettingsScreen()
        checkGuidedBrushingSettingsScreen()
    }

    private fun checkGuidedBrushingSettingsScreen() {
        onView(withText(R.string.gb_settings_toolbar_title))
            .check(matches(isDisplayed()))

        onView(withText(R.string.gb_settings_header2)).check(matches(isDisplayed()))
        onView(withText(R.string.gb_settings_music)).check(matches(isDisplayed()))
        onView(withText(R.string.gb_settings_transition_sound)).check(matches(isDisplayed()))

        onView(withId(R.id.gb_settings_choose_music)).check(matches(not(isDisplayed())))
        onView(withText(R.string.gb_settings_music)).perform(click())
        onView(withId(R.id.gb_settings_choose_music)).check(matches(isDisplayed()))

        runAndCheckIntent(hasAction(Intent.ACTION_OPEN_DOCUMENT)) {
            onView(withId(R.id.gb_settings_choose_music)).perform(click())
        }
    }

    private fun launchGuidedBrushingSettingsScreen() {
        clickOnSettingsItem(R.string.settings_item_guided_brushing)
    }

    @Test
    fun checkGetMyDataDialog() {
        prepareMocks()
        launchSettingsActivity()

        clickOnSettingsItem(R.string.settings_get_my_data)

        onView(withText(R.string.settings_get_my_data_popup_title)).check(matches(isDisplayed()))
        onView(
            withText(
                context().getString(
                    R.string.settings_get_my_data_popup_message,
                    EMAIL
                )
            )
        ).check(matches(isDisplayed()))
        onView(withText(R.string.ok)).check(matches(isDisplayed()))
    }

    private fun clickOnSettingsItem(@StringRes text: Int) {
        scrollToSettingsItem(text)

        onView(withText(text)).perform(click())
    }

    private fun scrollToSettingsItem(@StringRes text: Int) {
        onView(withId(R.id.settings_recycler_view))
            .perform(*bindedScrollTo(hasDescendant(withText(text))))
    }

    @Test
    @Suppress("MagicNumber", "LongMethod")
    fun checkBrushingTimeSecondsPickerBehaviour() {
        prepareMocks(brushingTime = 120)
        launchSettingsActivity()

        onView(withText(R.string.settings_brushing_time))
            .perform(click())

        showsDuration(120)

        decrementSeconds()
        showsDuration(120)

        incrementSeconds()
        showsDuration(125)

        incrementSeconds(10)
        showsDuration(175)

        incrementMinutes(2)
        showsDuration(295)

        incrementSeconds()
        showsDuration(300)

        incrementSeconds()
        showsDuration(300)
    }

    @Test
    @Suppress("MagicNumber", "LongMethod")
    @Ignore("Failing on anbox")
    fun checkBrushingTimeMinutesPickerBehaviour() {
        prepareMocks(brushingTime = 150)
        launchSettingsActivity()

        onView(withText(R.string.settings_brushing_time))
            .perform(click())

        showsDuration(150)

        decrementMinutes()
        showsDuration(150)

        incrementMinutes()
        showsDuration(210)

        incrementMinutes()
        showsDuration(270)

        incrementMinutes()
        showsDuration(300)

        incrementMinutes()
        showsDuration(300)

        decrementSeconds()
        decrementMinutes(2)
        showsDuration(175)

        decrementMinutes()
        showsDuration(175)
    }

    private fun showsDuration(durationValue: Int) {
        val duration = Duration.ofSeconds(durationValue.toLong())
        val minutes = duration.toMinutes()
        val seconds = duration.minusMinutes(minutes).seconds
        onView(withParent(withId(R.id.picker_major)))
            .check(matches(withText(minutes.toString())))
        onView(withParent(withId(R.id.picker_minor)))
            .check(matches(withText("%02d".format(seconds))))
    }

    private fun decrementSeconds(count: Int = 1) {
        decrementPicker(R.id.picker_minor, count)
    }

    private fun decrementMinutes(count: Int = 1) {
        decrementPicker(R.id.picker_major, count)
    }

    @Suppress("SpreadOperator")
    private fun decrementPicker(@IdRes idRes: Int, count: Int = 1) {
        val actions = Array<ViewAction>(count) { clickTopCenter() }
        onView(withId(idRes))
            .perform(*actions)
    }

    private fun incrementSeconds(count: Int = 1) {
        incrementPicker(R.id.picker_minor, count)
    }

    private fun incrementMinutes(count: Int = 1) {
        incrementPicker(R.id.picker_major, count)
    }

    @Suppress("SpreadOperator")
    private fun incrementPicker(@IdRes idRes: Int, count: Int = 1) {
        val actions = Array<ViewAction>(count) { clickBottomCenter() }
        onView(withId(idRes))
            .perform(*actions)
    }
}
