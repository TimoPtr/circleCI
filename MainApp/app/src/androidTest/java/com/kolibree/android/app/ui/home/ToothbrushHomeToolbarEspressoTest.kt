/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.BottomNavigationUtils.checkShop
import com.kolibree.android.app.ui.home.pairing.ToothbrushPairingActivity
import com.kolibree.android.test.utils.SdkBuilder
import junit.framework.TestCase.assertTrue
import org.junit.Test

@Suppress("LongMethod", "FunctionNaming")
internal class ToothbrushHomeToolbarEspressoTest : HomeScreenActivityEspressoTest() {
    @Test
    fun clickOnToothbrush_withNoToothbrushPaired_clickOnConnectToothbrush_showsPairingFlow() {
        prepareMocks()

        launchActivity()

        assertTrue(
            component().toothbrushRepository()
                .getAccountToothbrushes(SdkBuilder.DEFAULT_ACCOUNT_ID).isEmpty()
        )

        clickOnToothbrushIcon()

        try {
            Intents.init()

            val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
            val intentMatcher = hasComponent(ToothbrushPairingActivity::class.java.name)
            intending(intentMatcher)
                .respondWith(intentResult)

            onView(withId(R.id.pairing_start_connect_button))
                .check(matches(isDisplayed()))
                .perform(click())

            intended(intentMatcher)
        } finally {
            Intents.release()
        }
    }

    @Test
    fun clickOnToothbrush_withNoToothbrushPaired_clickOnShop_showsShopTab() {
        prepareMocks()

        launchActivity()

        assertTrue(
            component().toothbrushRepository()
                .getAccountToothbrushes(SdkBuilder.DEFAULT_ACCOUNT_ID).isEmpty()
        )

        clickOnToothbrushIcon()

        onView(withId(R.id.pairing_start_shop_button))
            .check(matches(isDisplayed()))
            .perform(click())

        checkShop()
    }

    private fun clickOnToothbrushIcon() {
        onView(withId(R.id.main_icon)).perform(click())
    }
}
