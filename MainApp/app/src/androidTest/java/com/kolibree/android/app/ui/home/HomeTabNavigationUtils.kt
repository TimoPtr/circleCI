/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.repeatedlyUntil
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import com.kolibree.R
import org.hamcrest.CoreMatchers.allOf

object HomeTabNavigationUtils {

    fun scrollToBrushingCard() {
        scrollToCard(LAST_BRUSHING_CARD_ID)
    }

    fun scrollToBrushingStreakCard() {
        scrollToCard(BRUSHING_STREAK_CARD_ID)
    }

    fun scrollToFrequencyCard() {
        scrollToCard(FREQUENCY_CHART_CARD_ID)
    }

    fun scrollToEarningPointsCard() {
        scrollToCard(EARNING_POINTS_CARD_ID)
    }

    fun scrollToHeadspaceMindfulMomentCard() {
        scrollToCard(HEADSPACE_MINDFUL_MOMENT_CARD_ID)
    }

    fun scrollToHeadspaceTrialCard() {
        scrollToCard(HEADSPACE_TRIAL_CARD_ID)
    }

    fun scrollToBrushBetterCard() {
        scrollToCard(BRUSH_BETTER_CARD_ID)
    }

    fun scrollToRewardYourselfCard() {
        scrollToCard(REWARD_YOURSELF_CARD_ID)
    }

    fun scrollToMoreWaysToEarnPoints() {
        scrollToCard(MORE_WAYS_TO_EARN_POINTS_CARD_ID)
    }

    fun scrollToQuestionOfTheDayCard() {
        scrollToCard(QUESTION_OF_THE_DAY_CARD_ID)
    }

    fun scrollToProductSupport() {
        scrollToCard(PRODUCT_SUPPORT_CARD_ID)
    }

    fun scrollToOralCareSupport() {
        scrollToCard(ORAL_CARE_SUPPORT_CARD_ID)
    }

    fun scrollToCard(cardId: Int) {
        onView(withId(R.id.home_tab))
            .perform(ViewActions.swipeUp())

        onView(withId(R.id.content_recyclerview))
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(withId(cardId)))
    }

    fun scrollToCardAtPosition(position: Int) {
        onView(withId(R.id.home_tab))
            .perform(ViewActions.swipeUp())

        onView(withId(R.id.content_recyclerview))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    }

    fun onAppBar() =
        onView(
            allOf(
                withId(R.id.appbar),
                withParent(withId(R.id.home_tab))
            )
        )

    fun onRecyclerView() =
        onView(
            allOf(
                withId(R.id.content_recyclerview),
                withParent(withId(R.id.home_tab))
            )
        )

    /**
     * Attempts multiple small swipe up, checking after each swipe if [viewResId] is displayed
     *
     * If after 10 attempts [viewResId] is not displayed, the test will fail
     */
    fun swipeUpUntilVisible(@IdRes scrollableResId: Int, @IdRes viewResId: Int) {
        val swipeAction = GeneralSwipeAction(
            Swipe.SLOW,
            GeneralLocation.BOTTOM_CENTER,
            GeneralLocation.TOP_CENTER,
            Press.FINGER
        )

        onView(withId(scrollableResId))
            .check(matches(isDisplayed()))
            .perform(
                repeatedlyUntil(
                    swipeAction,
                    hasDescendant(allOf(withId(viewResId), isCompletelyDisplayed())),
                    10
                )
            )
    }
}

private const val EARNING_POINTS_CARD_ID = R.id.earning_points_card
private const val BRUSHING_STREAK_CARD_ID = R.id.brushing_streak_card
private const val LAST_BRUSHING_CARD_ID = R.id.last_brushing_card
private const val HEADSPACE_MINDFUL_MOMENT_CARD_ID = R.id.headspace_mindful_moment_card
private const val HEADSPACE_TRIAL_CARD_ID = R.id.headspace_trial_card
private const val BRUSH_BETTER_CARD_ID = R.id.brush_better_card
private const val FREQUENCY_CHART_CARD_ID = R.id.frequency_chart_card
private const val MORE_WAYS_TO_EARN_POINTS_CARD_ID = R.id.more_ways_to_earn_points_card
private const val QUESTION_OF_THE_DAY_CARD_ID = R.id.question_card
private const val REWARD_YOURSELF_CARD_ID = R.id.reward_yourself_card
private const val PRODUCT_SUPPORT_CARD_ID = R.id.product_support_card
private const val ORAL_CARE_SUPPORT_CARD_ID = R.id.oral_care_support_card
