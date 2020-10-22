/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.kolibree.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher

object BottomNavigationUtils {

    fun navigateToDashboard() {
        bottomNavigationTo(
            R.id.bottom_navigation_home
        )
    }

    fun navigateToShop() {
        bottomNavigationTo(
            R.id.bottom_navigation_shop
        )
    }

    fun navigateToActivities() {
        bottomNavigationTo(
            R.id.bottom_navigation_activities
        )
    }

    fun navigateToProfile() {
        bottomNavigationTo(
            R.id.bottom_navigation_profile
        )
    }

    fun bottomNavigationTo(id: Int) {
        onView(
            allOf(
                withId(id),
                instanceOf(BottomNavigationItemView::class.java)
            )
        ).perform(click())
    }

    fun checkHomeSelected() {
        onMenuItem(R.id.bottom_navigation_home, R.string.bottom_navigation_home)
            .check(isDisplayedAndSelected())
    }

    fun checkActivities() {
        onMenuItem(R.id.bottom_navigation_activities, R.string.bottom_navigation_activities)
            .check(isDisplayedAndSelected())
    }

    fun checkShop() {
        onMenuItem(R.id.bottom_navigation_shop, R.string.bottom_navigation_shop)
            .check(isDisplayedAndSelected())
    }

    fun checkAccount() {
        onMenuItem(R.id.bottom_navigation_profile, HomeScreenActivityEspressoTest.PROFILE_NAME)
            .check(isDisplayedAndSelected())
    }

    private fun isDisplayedAndSelected(): ViewAssertion =
        matches(allOf(isDisplayed(), isSelected()))

    private fun onMenuItem(@IdRes id: Int, @StringRes stringId: Int): ViewInteraction {
        return onBottomNavigationView(
            allOf(
                withId(id),
                hasDescendant(withText(stringId))
            )
        )
    }

    private fun onMenuItem(@IdRes id: Int, text: String): ViewInteraction {
        return onBottomNavigationView(
            allOf(
                withId(id),
                hasDescendant(withText(text))
            )
        )
    }

    private fun onBottomNavigationView(matches: Matcher<View>): ViewInteraction {
        return onView(
            allOf(
                matches,
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.bottom_navigation))
            )
        )
    }
}
