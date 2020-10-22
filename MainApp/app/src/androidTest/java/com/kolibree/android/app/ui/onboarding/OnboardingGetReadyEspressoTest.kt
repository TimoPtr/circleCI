/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

internal class OnboardingGetReadyEspressoTest : OnboardingActivityEspressoTest() {

    @Test
    fun getReady_displaysAllNeededControls() {
        launchActivity()

        onView(withId(R.id.get_ready_container)).check(matches(isDisplayed()))
        onView(withId(R.id.header)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_get_ready_header)
                )
            )
        )
        onView(withId(R.id.subtitle)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_get_ready_subtitle)
                )
            )
        )
        onView(withId(R.id.sign_in_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_get_ready_sign_in_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
        onView(withId(R.id.connect_brush_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_get_ready_connect_brush_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
        onView(withId(R.id.no_brush_button)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_get_ready_no_brush_button),
                    isClickable(),
                    isFocusable()
                )
            )
        )
    }
}
