/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import org.junit.Test

internal class SelectProfileFromSettingsEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun settingsScreen_doesNotContainSelectProfileSection() {
        prepareMocks()

        launchSettingsActivity()

        checkSelectProfileSectionNotDisplayed()
    }

    private fun checkSelectProfileSectionNotDisplayed() {
        onView(withText(R.string.settings_select_profile_title)).check(doesNotExist())
        onView(withText(R.string.select_profile_add_profile)).check(doesNotExist())
    }

    private fun launchSettingsActivity() {
        launchActivity()

        bottomNavigationTo(BottomNavigationTab.PROFILE)
        clickSettingsButton()
    }

    private fun clickSettingsButton() {
        onView(ViewMatchers.withId(R.id.profile_settings_button)).perform(click())
    }
}
