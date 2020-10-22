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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

internal class PromotionsAndUpdatesEspressoTest : OnboardingSignUpEspressoTest() {

    @Test
    fun verifySignUpScreen_hasPromotionsAndUpdatesCheckbox() {
        goToSignUp()

        onView(withId(R.id.promotions_and_updates_checkbox)).check(
            matches(
                allOf(
                    isDisplayed(),
                    isNotChecked(),
                    isEnabled()
                )
            )
        )
        onView(withId(R.id.promotions_and_updates_textview)).check(
            matches(
                allOf(
                    isDisplayed(),
                    withText(R.string.onboarding_sign_up_receive_promotions_updates)
                )
            )
        )
    }
}
