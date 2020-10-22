/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.Partner.HEADSPACE
import com.kolibree.android.partnerships.headspace.data.api.KEY_DISCOUNT_CODE
import com.kolibree.android.partnerships.headspace.data.api.KEY_POINTS_NEEDED
import com.kolibree.android.partnerships.headspace.data.api.KEY_POINTS_THRESHOLD
import com.kolibree.android.partnerships.headspace.data.api.KEY_REDEEM_URL
import com.kolibree.android.partnerships.headspace.data.api.KEY_STATUS
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_INACTIVE
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_UNLOCKED
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withProgress
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.android.test.utils.webViewIntentWithData
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

internal class HeadspaceTrialCardEspressoTest : HomeScreenActivityEspressoTest() {
    @Test
    fun clickingOnTrialCard_showsDescriptionAndTogglesArrow() {
        prepareMocks()

        launchActivity()

        HomeTabNavigationUtils.scrollToHeadspaceTrialCard()

        assertInProgressCardIsCollapsed()

        makeScreenshot("HeadspaceTrial_InProgress_Collapsed")

        onView(withId(R.id.headspace_trial_card_icon))
            .check(matches(isDisplayed()))
            .perform(click())

        assertInProgressCardIsExpanded()

        makeScreenshot("HeadspaceTrial_InProgress_Expanded")
    }

    @Test
    fun trialCard_statesIntegrationTest() {
        prepareMocks()

        fakeHeadspaceStatusSequence()

        launchActivity()

        HomeTabNavigationUtils.scrollToHeadspaceTrialCard()

        assertProgressDisplayed(pendingPoints = THRESHOLD)

        makeScreenshot("HeadspaceTrial_InProgress_200PointsPending")

        refreshPartnershipState()

        assertProgressDisplayed(pendingPoints = SECOND_STATE_PENDING_POINTS)

        makeScreenshot("HeadspaceTrial_InProgress_HalfPointsPending")

        refreshPartnershipState()

        assertUnlockableStateIsDisplayed()

        makeScreenshot("HeadspaceTrial_Unlockable")

        swipeUntilVisible(R.id.headspace_trial_card_unlock_btn)
        onView(withId(R.id.headspace_trial_card_unlock_btn))
            .perform(click())

        assertUnlockedStateIsDisplayed()

        makeScreenshot("HeadspaceTrial_Unlocked")

        onView(withId(R.id.headspace_trial_card_copy_code_button))
            .perform(click())
            .check(matches(allOf(isDisplayed(), withText(R.string.headspace_card_copied))))

        makeScreenshot("HeadspaceTrial_Unlocked_CopiedToClipboard")

        runAndCheckIntent(webViewIntentWithData(REDEEM_URL)) {
            onView(withText(R.string.headspace_card_visit_headspace)).perform(click())
        }
    }

    /*
    Utils
     */

    private fun assertUnlockedStateIsDisplayed() {
        swipeUntilVisible(R.id.headspace_trial_card_description_mutable)
        assertMutableDescription(context().getString(R.string.headspace_card_unlocked_description))

        assertProgressBarIsNotDisplayed()

        val tapToCopyText = context().getString(R.string.headspace_card_tap_to_copy, DISCOUNT_CODE)
        onView(withId(R.id.headspace_trial_card_copy_code_button))
            .check(matches(allOf(isDisplayed(), withText(tapToCopyText))))

        onView(withId(R.id.headspace_trial_card_unlock_btn))
            .check(matches(allOf(isDisplayed(), withText(R.string.headspace_card_visit_headspace))))
    }

    private fun assertUnlockableStateIsDisplayed() {
        swipeUntilVisible(R.id.headspace_trial_card_description_mutable)

        assertMutableDescription(context().getString(R.string.headspace_card_unlock_description))

        assertProgressBarIsNotDisplayed()

        onView(withId(R.id.headspace_trial_card_unlock_btn))
            .check(matches(allOf(isDisplayed(), withText(R.string.headspace_card_unlock_button))))
    }

    private fun swipeUntilVisible(@IdRes viewResId: Int) {
        HomeTabNavigationUtils.swipeUpUntilVisible(R.id.content_recyclerview, viewResId)
    }

    private fun assertProgressBarIsNotDisplayed() {
        onView(withId(R.id.headspace_trial_card_progressbar))
            .check(matches(withEffectiveVisibility(GONE)))
    }

    private fun assertProgressDisplayed(pendingPoints: Int, threshold: Int = THRESHOLD) {
        val pointsText =
            context().getString(R.string.headspace_card_progress_points_to_unlock, pendingPoints)
        val expectedText =
            context().getString(R.string.headspace_card_progress_to_unlock, pointsText)
        assertMutableDescription(expectedText)

        val expectedProgress =
            (threshold - pendingPoints) * HeadspacePartnershipStatus.InProgress.MAX_PROGRESS / threshold

        onView(withId(R.id.headspace_trial_card_progressbar))
            .check(matches(isDisplayed()))
            .check(matches(withProgress(expectedProgress)))
    }

    private fun assertMutableDescription(expectedText: String) {
        IdlingResourceFactory
            .textViewContent(R.id.headspace_trial_card_description_mutable, expectedText)
            .waitForIdle()

        onView(withId(R.id.headspace_trial_card_description_mutable))
            .check(matches(isDisplayed()))
            .check(matches(withText(expectedText)))
    }

    private fun fakeHeadspaceStatusSequence(statusSequence: List<Map<Partner, Map<String, Any?>>> = defaultSequence()) {
        component().partnershipApiFake().setStateSequence(statusSequence)
    }

    private fun defaultSequence(): List<Map<Partner, Map<String, Any?>>> {
        return listOf(
            mapOf(
                HEADSPACE to mapOf(
                    KEY_POINTS_NEEDED to THRESHOLD,
                    KEY_POINTS_THRESHOLD to THRESHOLD
                )
            ),
            mapOf(
                HEADSPACE to mapOf(
                    KEY_POINTS_NEEDED to SECOND_STATE_PENDING_POINTS,
                    KEY_POINTS_THRESHOLD to THRESHOLD
                )
            ),
            mapOf(HEADSPACE to mapOf(KEY_POINTS_NEEDED to 0, KEY_POINTS_THRESHOLD to THRESHOLD)),
            mapOf(
                HEADSPACE to mapOf(
                    KEY_STATUS to VALUE_STATUS_UNLOCKED,
                    KEY_DISCOUNT_CODE to DISCOUNT_CODE,
                    KEY_REDEEM_URL to REDEEM_URL
                )
            ),
            mapOf(HEADSPACE to mapOf(KEY_STATUS to VALUE_STATUS_INACTIVE))
        )
    }

    private fun refreshPartnershipState() {
        component().partnershipStatusRepository().refreshPartnerships(
            accountId = SdkBuilder.DEFAULT_ACCOUNT_ID,
            profileId = PROFILE_ID
        )
            .test()
            .assertComplete()
    }

    private fun assertInProgressCardIsCollapsed() {
        onView(withId(R.id.headspace_trial_card_description))
            .check(matches(withEffectiveVisibility(GONE)))

        onView(withId(R.id.headspace_trial_card_expand_button))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_icon_navigation_expand_down_24_px)))

        assertProgressSectionHasVisibility()
    }

    private fun assertInProgressCardIsExpanded() {
        onView(withId(R.id.headspace_trial_card_description))
            .check(matches(isDisplayed()))

        onView(withId(R.id.headspace_trial_card_expand_button))
            .check(matches(isDisplayed()))
            .check(matches(withDrawable(R.drawable.ic_icon_navigation_expand_up_24_px)))

        assertProgressSectionHasVisibility()
    }

    private fun assertProgressSectionHasVisibility(visibility: ViewMatchers.Visibility = VISIBLE) {
        onView(withId(R.id.headspace_trial_card_description_mutable))
            .check(matches(withEffectiveVisibility(visibility)))

        onView(withId(R.id.headspace_trial_card_progressbar))
            .check(matches(withEffectiveVisibility(visibility)))
    }
}

private const val THRESHOLD = 200
private const val SECOND_STATE_PENDING_POINTS = 100
private const val DISCOUNT_CODE: String = "discount_code"
private const val REDEEM_URL: String = "https://www.google.com"
