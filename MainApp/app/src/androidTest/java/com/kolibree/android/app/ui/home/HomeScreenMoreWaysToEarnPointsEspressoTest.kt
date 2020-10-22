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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToMoreWaysToEarnPoints
import com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints.MoreWaysToEarnPointsCardItemResourceProviderImpl
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.test.assertions.RecyclerViewItemCountAssertion
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withRecyclerView
import java.util.Locale
import org.hamcrest.Matcher
import org.junit.Test

private const val POINTS_PER_CARD = 100

internal class HomeScreenMoreWaysToEarnPointsEspressoTest : HomeScreenActivityEspressoTest() {

    private val resourceProvider = MoreWaysToEarnPointsCardItemResourceProviderImpl()

    @Test
    fun onlySupportedMoreWaysToEarnPointsCardAreDisplayed() {
        prepareMocks(
            earnPointsChallenges = EarnPointsChallenge.Id.values()
                .map { EarnPointsChallenge(it, POINTS_PER_CARD) }
        )

        launchActivity()
        scrollToMoreWaysToEarnPoints()

        val supportedChallenges = listOf(
            EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE // ,
            // TODO uncomment in https://kolibree.atlassian.net/browse/KLTB002-11699
            // EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS
        )

        0.until(supportedChallenges.size).forEach { index ->
            val challengeId = supportedChallenges[index]
            scrollToMoreWaysToEarnPointsCard(challengeId.ordinal)
            checkInterfaceForCard(
                index,
                EarnPointsChallenge(challengeId, POINTS_PER_CARD)
            )
        }
    }

    @Test
    fun allMoreWaysToEarnPointsCardAreDisplayed() {
        prepareMocks(
            earnPointsChallenges = EarnPointsChallenge.Id.values()
                .map { EarnPointsChallenge(it, POINTS_PER_CARD) },
            showAllEarnPointsCards = true
        )

        launchActivity()
        scrollToMoreWaysToEarnPoints()

        onView(withId(R.id.more_ways_to_earn_points_card_title)).check(matches(isDisplayed()))
        onView(withId(R.id.more_ways_to_earn_points_card_items)).check(matches(isDisplayed()))
        onView(withId(R.id.more_ways_to_earn_points_card_items)).check(
            RecyclerViewItemCountAssertion.withItemCount(EarnPointsChallenge.Id.values().size)
        )

        makeScreenshot(
            activity.findViewById(R.id.more_ways_to_earn_points_card),
            "HomeTab_MoreWaysToEarnPointsCard"
        )

        EarnPointsChallenge.Id.values().forEach { challengeId ->
            scrollToMoreWaysToEarnPointsCard(challengeId.ordinal)
            checkInterfaceForCard(
                challengeId.ordinal,
                EarnPointsChallenge(challengeId, POINTS_PER_CARD)
            )
            when (challengeId) {
                EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE -> checkCompleteYourProfile()
                EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS,
                EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS,
                EarnPointsChallenge.Id.TURN_ON_BRUSHING_REMINDERS -> checkReminder(challengeId)
                EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW -> checkWeeklyReview()
                EarnPointsChallenge.Id.AMAZON_DASH -> checkAmazonDash()
                else -> {
                    /* no-op for now */
                }
            }
        }
    }

    private fun checkCompleteYourProfile() {
        clickOnChallengeCard(EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE)

        onView(withId(R.id.settings_recycler_view)).check(matches(isDisplayed()))
        onView(withBrusherHeaderText()).check(matches(isDisplayed()))

        pressBack()
    }

    private fun checkReminder(challengeId: EarnPointsChallenge.Id) {
        clickOnChallengeCard(challengeId)

        onView(withId(R.id.notifications_scroll_view)).check(matches(isDisplayed()))

        pressBack()
    }

    private fun checkWeeklyReview() {
        clickOnChallengeCard(EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW)

        onView(withId(R.id.settings_recycler_view)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_weekly_digest_title)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_weekly_digest_description)).check(matches(isDisplayed()))

        pressBack()
    }

    private fun checkAmazonDash() {
        clickOnChallengeCard(EarnPointsChallenge.Id.AMAZON_DASH)

        onView(withId(R.id.amazon_dash_connect)).check(matches(isDisplayed()))

        pressBack()
    }

    private fun scrollToMoreWaysToEarnPointsCard(position: Int) {
        onView(withId(R.id.more_ways_to_earn_points_card_items))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
    }

    private fun checkInterfaceForCard(position: Int, card: EarnPointsChallenge) {
        onView(
            withRecyclerView(R.id.more_ways_to_earn_points_card_items)
                .atPositionOnView(position, R.id.more_ways_to_earn_points)
        ).check(matches(withText("+$POINTS_PER_CARD pts")))
        onView(
            withRecyclerView(R.id.more_ways_to_earn_points_card_items)
                .atPositionOnView(position, R.id.more_ways_to_earn_points_item_icon)
        ).check(matches(withDrawable(resourceProvider.getIcon(card.id))))
        onView(
            withRecyclerView(R.id.more_ways_to_earn_points_card_items)
                .atPositionOnView(position, R.id.more_ways_to_earn_points_item_title)
        ).check(matches(withText(resourceProvider.getHeader(card.id))))
        onView(
            withRecyclerView(R.id.more_ways_to_earn_points_card_items)
                .atPositionOnView(position, R.id.more_ways_to_earn_points_item_body)
        ).check(matches(withText(resourceProvider.getBody(card.id))))
    }

    private fun clickOnChallengeCard(challengeId: EarnPointsChallenge.Id) {
        onView(
            withRecyclerView(R.id.more_ways_to_earn_points_card_items)
                .atPositionOnView(challengeId.ordinal, R.id.more_ways_to_earn_points)
        ).perform(click())
    }

    private fun withBrusherHeaderText(): Matcher<View> = withText(
        String.format(
            Locale.getDefault(),
            context().getString(R.string.settings_section_brushing_details_title),
            component().kolibreeConnector().currentProfile!!.firstName
        )
    )
}
