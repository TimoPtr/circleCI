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
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.app.dagger.EspressoGoogleSignInModule
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.sdkws.data.request.CreateAccountData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test

internal open class OnboardingSignUpEspressoTest : OnboardingActivityEspressoTest() {

    @Test
    fun signUpGoogle_happyPath() {
        prepareMocksForSuccessfulGoogleSignUp()

        goToSignUp()

        verifySignUpScreen()

        onView(withId(R.id.name_input_field)).perform(
            replaceText("Name"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.terms_and_conditions_checkbox)).perform(click())
        onView(withId(R.id.privacy_policy_checkbox)).perform(click())

        val fakeAction = "fake.action"
        val intent = Intent(fakeAction)

        whenever(EspressoGoogleSignInModule.wrapperMock.getSignInIntent()).thenReturn(intent)

        runAndCheckIntent(IntentMatchers.hasAction(fakeAction), intent) {
            onView(withId(R.id.google_sign_up_button)).perform(click())
        }

        // Progress view
        onView(withId(R.id.progress_background)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_view)).check(matches(isDisplayed()))

        advanceTimeBySeconds(MINIMAL_PROGRESS_DURATION.seconds)

        // Main screen should be visible by now
        onView(withId(R.id.progress_background)).check(ViewAssertions.doesNotExist())
        onView(withId(R.id.progress_view)).check(ViewAssertions.doesNotExist())
        onView(withId(R.id.dashboard_container)).check(matches(isDisplayed()))
    }

    @Test
    fun signUpEmail_happyPath() {
        goToSignUp()

        verifySignUpScreen()

        runAndCheckIntent(hasComponent(HomeScreenActivity::class.java.name)) {
            completeSignUpWithEmail()
        }
    }

    private fun verifySignUpScreen() {
        onView(withId(R.id.sign_up_container)).check(matches(isDisplayed()))
        onView(withId(R.id.header)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_header)
                )
            )
        )
        onView(withId(R.id.subtitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_subtitle)
                )
            )
        )
        onView(withId(R.id.name_input_layout)).check(
            matches(
                allOf(
                    isDisplayed(),
                    isEnabled()
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
                    isNotChecked(),
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
                    isNotChecked(),
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

        onView(withId(R.id.google_sign_up_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_google_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
        onView(withId(R.id.email_sign_up_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_email_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
    }

    protected fun goToSignUp() {
        launchActivity()
        onView(withId(R.id.no_brush_button)).perform(click())
    }

    private fun prepareMocksForSuccessfulGoogleSignUp() {
        prepareGoogleSignInWrapper()

        whenever(component().kolibreeConnector().createAccountByGoogle(any()))
            .thenReturn(Completable.complete())
    }

    private fun prepareGoogleSignInWrapper() {
        whenever(
            EspressoGoogleSignInModule.wrapperMock.maybeFillDataForAccountCreation(
                any(),
                any()
            )
        )
            .thenAnswer {
                it.getArgument(1, CreateAccountData.Builder::class.java)
                    .setGoogleId("1")
                    .setGoogleIdToken("token")
                    .setEmail("test@test.com")
                return@thenAnswer true
            }
    }
}

internal fun OnboardingActivityEspressoTest.completeSignUpWithEmail() {
    prepareMocksForSuccessfulEmailSignUp()

    onView(withId(R.id.name_input_field)).perform(
        replaceText("Name"),
        closeSoftKeyboard()
    )
    onView(withId(R.id.terms_and_conditions_checkbox)).perform(click())
    onView(withId(R.id.privacy_policy_checkbox)).perform(click())
    onView(withId(R.id.email_sign_up_button)).perform(click())

    verifyCheckEmailScreen()

    onView(withId(R.id.email_input_field)).perform(
        replaceText("email@example.com"),
        closeSoftKeyboard()
    )
    onView(withId(R.id.finish_button)).perform(click())

    // Progress view
    onView(withId(R.id.progress_background)).check(matches(isDisplayed()))
    onView(withId(R.id.progress_view)).check(matches(isDisplayed()))

    advanceTimeBySeconds(MINIMAL_PROGRESS_DURATION.seconds)
}

private fun OnboardingActivityEspressoTest.prepareMocksForSuccessfulEmailSignUp(createdAccount: AccountInternal = createAccountInternal()) {
    whenever(component().kolibreeConnector().createEmailAccount(any()))
        .thenReturn(Single.just(createdAccount))
}

private fun verifyCheckEmailScreen() {
    onView(
        allOf(
            withId(R.id.header),
            isDescendantOfA(withId(R.id.enter_email_container))
        )
    ).check(
        matches(
            allOf(
                isDisplayed(),
                withText(R.string.onboarding_enter_email_header)
            )
        )
    )
    onView(
        allOf(
            withId(R.id.subtitle),
            isDescendantOfA(withId(R.id.enter_email_container))
        )
    ).check(
        matches(
            allOf(
                isDisplayed(),
                withText(R.string.onboarding_enter_email_subtitle)
            )
        )
    )
    onView(
        allOf(
            withId(R.id.body),
            isDescendantOfA(withId(R.id.enter_email_container))
        )
    ).check(
        matches(
            allOf(
                isDisplayed(),
                withText(R.string.onboarding_enter_email_body)
            )
        )
    )
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
                withHint(R.string.onboarding_enter_email_input_hint),
                isEnabled(),
                isFocusable(),
                not(hasFocus())
            )
        )
    )
    onView(withId(R.id.finish_button)).check(
        matches(
            allOf(
                isDisplayed(),
                withText(R.string.onboarding_enter_email_finish_button),
                isClickable(),
                isFocusable()
            )
        )
    )
}
