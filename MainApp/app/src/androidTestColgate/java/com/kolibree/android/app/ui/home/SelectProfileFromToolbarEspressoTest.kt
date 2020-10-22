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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.test.mocks.ProfileBuilder
import org.junit.Test

internal class SelectProfileFromToolbarEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun whenTapOnProfileName_profileSelectionDialogVisible() {
        val profileName = "HelloWorld"
        val profile = ProfileBuilder.create()
            .withName(profileName)
            .build()
        prepareMocks(profile = profile)

        launchActivity()

        val profileText = context().getString(R.string.home_toolbar_title_format, profileName)
        onView(withText(profileText)).perform(click())

        checkSelectProfileDialogDisplayed()
        makeScreenshot("ProfilePicker")
    }

    private fun checkSelectProfileDialogDisplayed() {
        onView(withText(R.string.select_profile_header)).check(matches(isDisplayed()))
        onView(withText(R.string.select_profile_close)).check(matches(isDisplayed()))
        onView(withText(R.string.select_profile_add_profile)).check(matches(isDisplayed()))
    }
}
