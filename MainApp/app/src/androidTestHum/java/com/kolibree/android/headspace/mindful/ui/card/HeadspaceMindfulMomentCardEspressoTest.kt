/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui.card

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils
import org.junit.Test

internal class HeadspaceMindfulMomentCardEspressoTest : HomeScreenActivityEspressoTest() {
    @Test
    fun headspaceMindfulMomentCard_shouldBeVisible() {
        prepareMocks()

        launchActivity()

        HomeTabNavigationUtils.scrollToHeadspaceMindfulMomentCard()

        onView(withId(R.id.headspace_mindful_moment_card)).check(matches(isDisplayed()))

        makeScreenshot("HeadspaceMindfulMomentCard_Visible")
    }
}
