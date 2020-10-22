/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.amazondash

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.app.dagger.FakeAmazonDashApi
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToMoreWaysToEarnPoints
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.AMAZON_DASH
import com.kolibree.android.test.espresso_helpers.CustomMatchers
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import io.reactivex.Single
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.Response

internal class AmazonDashEspressoTest : HomeScreenActivityEspressoTest() {

    private val amazonDashApi: FakeAmazonDashApi
        get() = component().amazonDashApi()

    override fun setUp() {
        super.setUp()
        prepareMocks(
            earnPointsChallenges = EarnPointsChallenge.Id.values()
                .map { EarnPointsChallenge(it, 0) },
            showAllEarnPointsCards = true
        )
    }

    @Test
    fun amazonDashConnectScreenIsDisplayedAfterClickingOnCard() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        pressBack()
        checkAmazonDash(isVisible = false)
    }

    @Test
    fun amazonDashConnectClosesAfterClickingDismissButton() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        onView(withId(R.id.connect_dismiss_button)).perform(click())
        checkAmazonDash(isVisible = false)
    }

    @Test
    fun amazonDashConnectDisplaysLoadingWhenSendingToken() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        val (redirectUrl, state) = fakeAlexaFlow()

        val responseUri = Uri.parse("$redirectUrl?code=mock_token&state=$state")
        val responseIntent = Intent(Intent.ACTION_VIEW, responseUri)
            .setPackage(context().packageName)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        amazonDashApi.mockTokenResponse(Single.never())

        context().startActivity(responseIntent)

        IdlingResourceFactory.viewVisibility(
            R.id.connect_progress,
            View.VISIBLE
        ).waitForIdle()

        checkAmazonDash(isVisible = true, isLoading = true)
    }

    @Test
    fun amazonDashConnectDisplaysLoadingWhenWaitingForLinks() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        amazonDashApi.mockLinksResponse(Single.never())
        onView(withId(R.id.connect_confirm_button)).perform(click())

        IdlingResourceFactory.viewVisibility(
            R.id.connect_progress,
            View.VISIBLE
        ).waitForIdle()

        checkAmazonDash(isVisible = true, isLoading = true)
    }

    @Test
    fun amazonDashConnectDisplaysErrorWheLinksNotAvailable() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        amazonDashApi.mockLinksResponse(Single.error(IllegalStateException("test")))
        onView(withId(R.id.connect_confirm_button)).perform(click())

        IdlingResourceFactory.viewVisibility(
            R.id.snackbar_text,
            View.VISIBLE
        ).waitForIdle()

        checkAmazonDash(isVisible = true, isSuccess = false)

        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.amazon_dash_connect_error_unknown)))
    }

    @Test
    fun amazonDashConnectDisplaysSuccessAfterSuccessfulAlexaFlow() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        val (redirectUrl, state) = fakeAlexaFlow()

        val responseUri = Uri.parse("$redirectUrl?code=mock_token&state=$state")
        val responseIntent = Intent(Intent.ACTION_VIEW, responseUri)
            .setPackage(context().packageName)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        amazonDashApi.mockTokenResponse(Response.success(null))

        context().startActivity(responseIntent)

        IdlingResourceFactory.viewVisibility(
            R.id.connect_congratulations,
            View.VISIBLE
        ).waitForIdle()

        checkAmazonDash(isVisible = true, isSuccess = true)

        onView(withId(R.id.connect_confirm_button)).perform(click())
        checkAmazonDash(isVisible = false)
    }

    @Test
    fun amazonDashConnectDisplaysErrorWhenStateIsWrong() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        val (redirectUrl, _) = fakeAlexaFlow()

        val responseUri = Uri.parse("$redirectUrl?code=mock_token&state=wrong_state")
        val responseIntent = Intent(Intent.ACTION_VIEW, responseUri)
            .setPackage(context().packageName)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context().startActivity(responseIntent)

        checkAmazonDash(isVisible = true, isSuccess = false)
        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.amazon_dash_connect_error_invalid_link)))
    }

    @Test
    fun amazonDashConnectDisplaysErrorWhenSomethingGoesWrong() {
        navigateToAmazonDashConnect()
        checkAmazonDash(isVisible = true)

        val (redirectUrl, state) = fakeAlexaFlow()

        val responseUri = Uri.parse("$redirectUrl?code=mock_token&state=$state")
        val responseIntent = Intent(Intent.ACTION_VIEW, responseUri)
            .setPackage(context().packageName)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        amazonDashApi.mockTokenResponse(
            Response.error(
                HTTP_INTERNAL_ERROR,
                ResponseBody.create(null, "")
            )
        )

        context().startActivity(responseIntent)

        IdlingResourceFactory.viewVisibility(
            R.id.snackbar_text,
            View.VISIBLE
        ).waitForIdle()

        checkAmazonDash(isVisible = true, isSuccess = false)

        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.amazon_dash_connect_error_unknown)))
    }

    private fun navigateToAmazonDashConnect() {
        launchActivity()
        scrollToMoreWaysToEarnPoints()
        scrollToAmazonDashCard()
        clickOnAmazonDashCard()
    }

    private fun scrollToAmazonDashCard() {
        onView(withId(R.id.more_ways_to_earn_points_card_items))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(AMAZON_DASH.ordinal))
    }

    private fun clickOnAmazonDashCard() {
        onView(
            CustomMatchers
                .withRecyclerView(R.id.more_ways_to_earn_points_card_items)
                .atPositionOnView(AMAZON_DASH.ordinal, R.id.more_ways_to_earn_points)
        ).perform(click())
    }

    private fun checkAmazonDash(
        isVisible: Boolean,
        isLoading: Boolean = false,
        isSuccess: Boolean = false
    ) {
        if (!isVisible) {
            onView(withId(R.id.amazon_dash_connect)).check(doesNotExist())
            return
        }

        onView(withId(R.id.amazon_dash_connect)).check(matches(isDisplayed()))
        onView(withId(R.id.connect_logo)).check(matches(isDisplayed()))
        onView(withId(R.id.connect_description)).check(matches(isDisplayed()))
        onView(withId(R.id.connect_title)).check(matches(isDisplayed()))
        onView(withId(R.id.connect_body)).check(matches(isDisplayed()))
        onView(withId(R.id.connect_confirm_button)).check(matches(isDisplayed()))

        if (isLoading) {
            onView(withId(R.id.connect_divider)).check(matches(not(isDisplayed())))
            onView(withId(R.id.connect_progress)).check(matches(isDisplayed()))
            onView(withId(R.id.connect_title)).check(matches(withText(R.string.amazon_dash_connect_loading_title)))
            onView(withId(R.id.connect_body)).check(matches(withText(R.string.amazon_dash_connect_loading_body)))
        } else {
            onView(withId(R.id.connect_divider)).check(matches(isDisplayed()))
            onView(withId(R.id.connect_progress)).check(matches(not(isDisplayed())))

            if (isSuccess) {
                onView(withId(R.id.connect_congratulations)).check(matches(isDisplayed()))
                onView(withId(R.id.connect_dismiss_button)).check(matches(not(isDisplayed())))
                onView(withId(R.id.connect_description)).check(matches(withHtml(R.string.amazon_dash_celebration_description)))
                onView(withId(R.id.connect_title)).check(matches(withText(R.string.amazon_dash_celebration_title)))
                onView(withId(R.id.connect_body)).check(matches(withText(R.string.amazon_dash_celebration_body)))
                onView(withId(R.id.connect_confirm_button)).check(matches(withText(R.string.amazon_dash_celebration_confirm_button)))
            } else {
                onView(withId(R.id.connect_congratulations)).check(matches(not(isDisplayed())))
                onView(withId(R.id.connect_dismiss_button)).check(matches(isDisplayed()))
                onView(withId(R.id.connect_description)).check(matches(withHtml(R.string.amazon_dash_connect_description)))
                onView(withId(R.id.connect_title)).check(matches(withText(R.string.amazon_dash_connect_title)))
                onView(withId(R.id.connect_body)).check(matches(withText(R.string.amazon_dash_connect_body)))
                onView(withId(R.id.connect_confirm_button)).check(matches(withText(R.string.amazon_dash_connect_confirm_button)))
            }
        }
    }

    private fun fakeAlexaFlow(): Pair<String, String> {
        var requestState: String? = null
        var redirectUrl: String? = null

        try {
            Intents.init()
            intending(anyIntent()).respondWithFunction { intent ->
                requestState = intent.data?.getQueryParameter("state").orEmpty()
                redirectUrl = intent.data?.getQueryParameter("redirect_uri").orEmpty()

                return@respondWithFunction Instrumentation.ActivityResult(Activity.RESULT_OK, null)
            }

            amazonDashApi.mockLinksResponse(TEST_LINK_RESPONSE)
            onView(withId(R.id.connect_confirm_button)).perform(click())
        } finally {
            Intents.release()
        }

        assertNotNull("Missing request state! Unable to continue!", requestState)
        assertNotNull("Missing redirect url! Unable to continue!", redirectUrl)

        return redirectUrl!! to requestState!!
    }

    private fun withHtml(res: Int): Matcher<View> {
        return withText(HtmlCompat.fromHtml(context().getString(res), 0).toString())
    }

    companion object {
        private val TEST_LINK_RESPONSE = Response.success(
            AmazonDashGetLinkResponse(
                appUrl = "https://test.com/?redirect_uri=https://staging.kolibree.com/v4/accounts/oauth/fallback/",
                fallbackUrl = "https://test.com/?redirect_uri=https://staging.kolibree.com/v4/accounts/oauth/fallback/"
            )
        )
    }
}
