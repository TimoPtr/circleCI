/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.card

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

internal class HeadspaceMindfulMomentCardEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun cardIsNotDisplayed() {
        launchActivity()

        onView(
            allOf(
                isDescendantOfA(withId(R.id.content_recyclerview)),
                withId(R.id.headspace_mindful_moment_card)
            )
        ).check(doesNotExist())
    }
}
