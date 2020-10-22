/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoGoogleSignInModule
import com.kolibree.android.test.utils.runAndCheckIntent
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test

internal class OnboardingLoginEspressoTest : OnboardingActivityEspressoTest() {

    @Test
    fun login_displaysAllNeededControls() {
        goToLogin()

        onView(withId(R.id.login_container)).check(matches(isDisplayed()))
        onView(withId(R.id.header)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_login_title)
                )
            )
        )
        onView(withId(R.id.subtitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_login_subtitle)
                )
            )
        )
        onView(withId(R.id.body)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_login_body)
                )
            )
        )
        onView(withId(R.id.or)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_login_or_divider)
                )
            )
        )
        onView(withId(R.id.or_divider)).check(matches(isDisplayed()))
        onView(withId(R.id.email_input_layout)).check(
            matches(
                allOf(
                    isDisplayed(),
                    isEnabled()
                )
            )
        )
        onView(withId(R.id.email_input_field)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withHint(R.string.onboarding_login_input_hint),
                    isEnabled(),
                    isFocusable(),
                    not(hasFocus())
                )
            )
        )
        onView(withId(R.id.google_login_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_login_google_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
        onView(withId(R.id.email_login_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_login_email_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
        makeScreenshot("Onboarding_LoginScreen")
    }

    @Test
    fun googleSignIn_happyPath() {
        prepareMocksForSuccessfulGoogleLogin()

        goToLogin()

        val fakeAction = "fake.action"
        val intent = Intent(fakeAction)

        whenever(EspressoGoogleSignInModule.wrapperMock.getSignInIntent()).thenReturn(intent)

        runAndCheckIntent(hasAction(fakeAction), intent) {
            onView(withId(R.id.google_login_button)).perform(click())
        }

        // Progress view
        onView(withId(R.id.progress_background)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_view)).check(matches(isDisplayed()))

        advanceTimeBySeconds(MINIMAL_PROGRESS_DURATION.seconds)

        // Main screen should be visible by now
        onView(withId(R.id.progress_background)).check(doesNotExist())
        onView(withId(R.id.progress_view)).check(doesNotExist())
        onView(withId(R.id.dashboard_container)).check(matches(isDisplayed()))
    }

    @Test
    fun emailSignIn_happyPath() {
        val correctEmail = "example@email.com"
        prepareMocksForSuccessfulEmailLogin(correctEmail)

        goToLogin()

        onView(withId(R.id.email_input_field)).perform(
            replaceText(correctEmail),
            closeSoftKeyboard()
        )

        onView(withId(R.id.email_login_button)).perform(click())

        // Progress view
        onView(withId(R.id.progress_background)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_view)).check(matches(isDisplayed()))

        advanceTimeBySeconds(MINIMAL_PROGRESS_DURATION.seconds)

        onView(withId(R.id.progress_background)).check(matches(not(isDisplayed())))
        onView(withId(R.id.progress_view)).check(matches(not(isDisplayed())))

        verifyCheckEmailScreen()

        // TODO FIX THAT
        // emulateMagicLinkClick()
        //
        // IdlingResourceHelper.waitForIdlingResource(
        //     ViewVisibilityIdlingResource.withVisibilityIdlingResource(
        //         R.id.progress_view,
        //         View.VISIBLE
        //     )
        // ).andThen {
        //     onView(withId(R.id.progress_background)).check(matches(isDisplayed()))
        //     onView(withId(R.id.progress_view)).check(matches(isDisplayed()))
        // }
        //
        // advanceTimeBySeconds(MINIMAL_PROGRESS_DURATION.seconds)
        //
        // // Main screen should be visible by now
        // onView(withId(R.id.progress_background)).check(doesNotExist())
        // onView(withId(R.id.progress_view)).check(doesNotExist())
        // onView(withId(R.id.dashboard_container)).check(matches(isDisplayed()))
    }

    private fun verifyCheckEmailScreen() {
        onView(
            allOf(
                withId(R.id.header),
                isDescendantOfA(withId(R.id.email_check_container))
            )
        ).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_check_email_header)
                )
            )
        )

        onView(
            allOf(
                withId(R.id.subtitle),
                isDescendantOfA(withId(R.id.email_check_container))
            )
        ).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_check_email_subtitle)
                )
            )
        )

        onView(
            allOf(
                withId(R.id.body),
                isDescendantOfA(withId(R.id.email_check_container))
            )
        ).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_check_email_body)
                )
            )
        )
    }

    private fun goToLogin() {
        launchActivity()
        onView(withId(R.id.sign_in_button)).perform(click())
    }
}
