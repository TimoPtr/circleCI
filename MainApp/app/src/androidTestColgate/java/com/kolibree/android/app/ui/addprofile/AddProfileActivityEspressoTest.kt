/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test

internal class AddProfileActivityEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun addProfile_happyPath() {
        launchAddProfileActivity()
        makeScreenshot("addProfile_upperScreen")

        verifyScreen()

        onView(withId(R.id.name_input_field)).perform(
            scrollTo(),
            replaceText("Name"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.terms_and_conditions_checkbox))
            .perform(scrollTo(), click())
        onView(withId(R.id.privacy_policy_checkbox))
            .perform(scrollTo(), click())

        onView(withId(R.id.add_profile_button))
            .perform(scrollTo())
        makeScreenshot("addProfile_lowerScreen")

        onView(withId(R.id.add_profile_button))
            .perform(click())

        // Progress view
        onView(withId(R.id.progress_background)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_view)).check(matches(isDisplayed()))
    }

    private fun checkAvatar() {
        onView(withId(R.id.add_photo_image)).check(
            matches(
                allOf(
                    isDisplayed()
                )
            )
        ).perform(click())

        onView(withId(R.id.dialog_select_avatar_title))
            .check(matches(isDisplayed()))

        onView(withId(R.id.dialog_select_avatar_icon))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        pressBack()

        onView(withId(R.id.add_photo_title)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_add_a_photo)
                )
            )
        )
    }

    private fun verifyScreen() {
        onView(withId(R.id.header))
            .perform(scrollTo())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.select_profile_add_profile)
                    )
                )
            )
        checkAvatar()
        onView(withId(R.id.name_input_title)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_subtitle)
                )
            )
        )
        onView(withId(R.id.name_input_field)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withHint(R.string.onboarding_sign_up_name_hint),
                    isEnabled(),
                    isFocusable(),
                    not(hasFocus())
                )
            )
        )
        onView(withId(R.id.birthday_input_title)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_birthday_title)
                )
            )
        )
        onView(withId(R.id.birthday_input_optional)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_optional)
                )
            )
        )
        onView(withId(R.id.birthday_input_field)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withHint(R.string.add_profile_birthday_hint),
                    isEnabled(),
                    isFocusable()
                )
            )
        )

        onView(withId(R.id.add_profile_button))
            .perform(scrollTo())
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.select_profile_add_profile)
                    )
                )
            )

        onView(withId(R.id.gender_input_title)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_gender_title)
                )
            )
        )
        onView(withId(R.id.gender_input_optional)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_optional)
                )
            )
        )
        onView(withId(R.id.gender_input)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withHint(R.string.add_profile_choose_hint),
                    isEnabled(),
                    isFocusable(),
                    not(hasFocus())
                )
            )
        )

        onView(withId(R.id.handedness_input_title)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_handedness_title)
                )
            )
        )
        onView(withId(R.id.handedness_input_optional)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.add_profile_optional)
                )
            )
        )
        onView(withId(R.id.handedness_input)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withHint(R.string.add_profile_choose_hint),
                    isEnabled(),
                    isFocusable(),
                    not(hasFocus())
                )
            )
        )

        onView(withId(R.id.checkbox_subtitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_consent_header)
                )
            )
        )

        onView(withId(R.id.terms_and_conditions_checkbox)).check(
            matches(
                allOf(
                    isDisplayed(),
                    ViewMatchers.isNotChecked(),
                    isEnabled()
                )
            )
        )
        onView(withId(R.id.terms_and_conditions_part1)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_consent)
                )
            )
        )
        onView(withId(R.id.terms_and_conditions_part2)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(
                        " ${context().getString(R.string.onboarding_sign_up_consent_terms_conditions_link)} "
                    )
                )
            )
        )

        onView(withId(R.id.privacy_policy_checkbox)).check(
            matches(
                allOf(
                    isDisplayed(),
                    ViewMatchers.isNotChecked(),
                    isEnabled()
                )
            )
        )
        onView(withId(R.id.privacy_policy_part1)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_consent)
                )
            )
        )
        onView(withId(R.id.privacy_policy_part2)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(
                        " ${context().getString(R.string.onboarding_sign_up_consent_privacy_policy_link)} "
                    )
                )
            )
        )
    }

    private fun launchAddProfileActivity() {
        launchActivity()
        context().startActivity(startAddProfileIntent(context()).apply { flags = FLAG_ACTIVITY_NEW_TASK })
    }
}
