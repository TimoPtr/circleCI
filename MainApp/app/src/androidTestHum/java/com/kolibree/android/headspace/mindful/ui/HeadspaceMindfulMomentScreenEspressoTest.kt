/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoHeadspaceMindfulMomentModule.FakeHeadspaceMindfulMomentUseCase
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToHeadspaceMindfulMomentCard
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

internal class HeadspaceMindfulMomentScreenEspressoTest : HomeScreenActivityEspressoTest() {

    override fun setUp() {
        super.setUp()

        prepareMocks()
        launchActivity()
        scrollToHeadspaceMindfulMomentCard()
        onView(withId(R.id.headspace_mindful_moment_card)).perform(click())
    }

    @Test
    fun closeMindfulMomentScreenWithCloseButton() {
        onView(
            allOf(
                instanceOf(AppCompatImageButton::class.java),
                withParent(withId(R.id.headspace_mindful_moment_toolbar))
            )
        ).perform(click())

        onView(withId(R.id.headspace_mindful_moment_root)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun closeMindfulMomentScreenAfterCollectingSmiles() {
        onView(withId(R.id.headspace_mindful_moment_collect_smile_btn)).perform(click())

        onView(withId(R.id.headspace_mindful_moment_root)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun backgroundColorMatchesOneProvidedWithMindfulMoment() {
        val backgroundColor = Color.parseColor(
            FakeHeadspaceMindfulMomentUseCase.MINDFUL_MOMENT_BACKGROUND
        )
        onView(withId(R.id.headspace_mindful_moment_root)).check(
            matches(
                withBackgroundColor(
                    backgroundColor
                )
            )
        )
    }

    @Test
    fun quoteTextMatchesOneProvidedWithMindfulMoment() {
        onView(withId(R.id.headspace_mindful_moment_quote)).check(
            matches(
                withText(
                    FakeHeadspaceMindfulMomentUseCase.MINDFUL_MOMENT_QUOTE
                )
            )
        )
    }

    @Test
    fun quoteTextColorMatchesOneProvidedWithMindfulMoment() {
        val foregroundColor = Color.parseColor(
            FakeHeadspaceMindfulMomentUseCase.MINDFUL_MOMENT_FOREGROUND
        )
        onView(withId(R.id.headspace_mindful_moment_quote)).check(
            matches(withTextColor(foregroundColor))
        )
    }

    private fun withBackgroundColor(color: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("View should have appropriate backgroundColor: $color")
            }

            override fun matchesSafely(item: View?) =
                (item?.background as ColorDrawable).color == color
        }
    }

    private fun withTextColor(color: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("TextView should have appropriate color: $color")
            }

            override fun matchesSafely(item: View?) = (item as TextView).currentTextColor == color
        }
    }
}
