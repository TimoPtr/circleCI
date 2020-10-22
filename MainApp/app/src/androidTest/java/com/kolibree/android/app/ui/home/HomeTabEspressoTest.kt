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
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.onAppBar
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.onRecyclerView
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToBrushingCard
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToEarningPointsCard
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToFrequencyCard
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToOralCareSupport
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToProductSupport
import com.kolibree.android.app.ui.home.matcher.CoverageDurationProgressViewMatchers
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DurationFormatter
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withRecyclerView
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.BrushingBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.android.test.utils.webViewIntentWithData
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Ignore
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import zendesk.support.guide.HelpCenterActivity

internal class HomeTabEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun home_collapsingToolbar() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()
        makeScreenshot("InitialState")

        assertOpenSmilesHistory()

        assertSmilesShowing()

        onAppBar()
            .perform(swipeUp())

        assertSmilesGone()

        onRecyclerView()
            .perform(swipeDown())

        assertSmilesShowing()
    }

    @Test
    fun home_earningPointsCard_verifyExpandCollapse() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()

        onAppBar().perform(swipeUp())

        checkEarningPointsCardCollapsedLayout()

        clickEarningPointsCard()

        checkEarningPointsCardExpandLayout()

        clickEarningPointsCard()

        checkEarningPointsCardCollapsedLayout()
    }

    @Test
    fun home_lastBrushingCard() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()

        checkLastBrushingCard()
    }

    @Test
    fun home_newUser_profileCreatedOneDayAgo() {
        val creationDate = TrustedClock.getNowZonedDateTime().minusDays(1)
        val profile = ProfileBuilder.create()
            .withCreationDate(creationDate)
            .withName(PROFILE_NAME)
            .withId(PROFILE_ID)
            .build()

        prepareMocks(profile = profile)

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()

        scrollToBrushingCard()
        makeScreenshot(
            activity.findViewById(R.id.last_brushing_card),
            "HomeTab_LastBrushingCard"
        )

        val today = TrustedClock.getNowLocalDate()
        checkNoViewOnPosition(0, today)
        checkNoViewOnPosition(1, today.minusDays(1))
        checkDashedViewOnPosition(2)
        checkDashedViewOnPosition(3)
        checkDashedViewOnPosition(4)
    }

    @Test
    fun home_oldUser_profileCreatedWeekAgo_oneBrushingTwoDaysAgo() {
        val creationDate = TrustedClock.getNowZonedDateTime().minusWeeks(1)
        val profile = ProfileBuilder.create()
            .withCreationDate(creationDate)
            .withName(PROFILE_NAME)
            .withId(PROFILE_ID)
            .build()

        prepareMocks(profile = profile)

        val brushingDate = TrustedClock.getNowOffsetDateTime().minusDays(1)
        val brushing = brushing(brushingDate)
        mockProfileBrushings(listOf(brushing))

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()

        scrollToBrushingCard()

        val today = TrustedClock.getNowLocalDate()
        checkNoViewOnPosition(0, today)
        checkBrushingViewOnPosition(1, brushing)
        checkNoViewOnPosition(2, today.minusDays(2))
        checkNoViewOnPosition(3, today.minusDays(3))
        checkNoViewOnPosition(4, today.minusDays(4))

        checkDetailsOnPosition(0, "- %", "-")
        checkDetailsOnPosition(1, "- %", "0:50")
    }

    @Test
    fun headerShowsSmiles() {
        prepareMocks(profileSmiles = DUMMY_SMILES_VALUE)

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()

        onView(withId(R.id.smiles_background))
            .check(matches(isDisplayed()))

        IdlingResourceFactory.viewVisibility(
            R.id.smiles_value,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.smiles_value))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(DUMMY_SMILES_VALUE.toString())
                    )
                )
            )

        onView(withId(R.id.smiles_label))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(R.string.home_screen_smile_points)
                    )
                )
            )
    }

    @Ignore("Throws NPE when deleting a brushing session")
    @Test
    fun lastBrushingCard_brushingDeletion_happyPath() {
        // Current profile setup
        val currentProfileId = 1986L
        val currentProfileName = "Jean-Louis"
        val currentProfile = ProfileBuilder.create()
            .withId(currentProfileId)
            .withName(currentProfileName)
            .build()

        // Brushing sessions setup
        val expectedDurationMonday = 90L
        val brushingMon = BrushingBuilder.create()
            .withDuration(expectedDurationMonday)
            .withDateTime(TrustedClock.getNowOffsetDateTime().minusDays(2))
            .build()

        val expectedDurationTuesday = 100L
        val brushingTues = BrushingBuilder.create()
            .withDuration(expectedDurationTuesday)
            .withDateTime(TrustedClock.getNowOffsetDateTime().minusDays(1).minusHours(6))
            .build()

        val expectedDurationTuesday2 = 120L
        val brushingTuesday2 = BrushingBuilder.create()
            .withDuration(expectedDurationTuesday2)
            .withDateTime(TrustedClock.getNowOffsetDateTime())
            .build()

        // SDK and app mocks
        val sdkBuilder = SdkBuilder.create()
            .withProfiles(currentProfile)
            .withActiveProfile(currentProfile)
            .withBluetoothEnabled(true)
            .withBrushingsForProfile(currentProfileId, brushingMon, brushingTues, brushingTuesday2)
            .prepareForMainScreen()

        AppMocker.create()
            .withSdkBuilder(sdkBuilder)
            .withProfileSmiles(currentProfileId, currentProfileName, 3)
            .withLocationPermissionGranted(true)
            .withLocationEnabled(true)
            .withMockedShopifyProducts()
            .prepareForMainScreen()
            .mock()

        // Go to the last brushing card
        launchActivity()
        BottomNavigationUtils.navigateToDashboard()

        scrollToBrushingCard()

        // We should be displaying tuesday2's brushing data
        assertDisplayedDuration(expectedDurationTuesday2)

        // We click on tuesday's item
        clickOnItemAtPosition(1)

        // We should be displaying tuesday's brushing data
        assertDisplayedDuration(expectedDurationTuesday)

        onView(withId(R.id.content_recyclerview))
            .perform(swipeUp())

        // Then we want to delete this tuesday's brushing
        onView(withId(R.id.last_brushing_card_delete_button))
            .perform(click())

        // The confirmation dialog appears
        onView(withText(R.string.orphan_brushings_delete_message))
            .check(matches(isDisplayed()))

        // We click on the confirmation button
        onView(withText(R.string.um_yes)).perform(click())

        // Then monday's session is shown, so we are happy
        assertDisplayedDuration(expectedDurationMonday)
    }

    @Test
    fun homeFrequencyChartCard() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToDashboard()

        checkFrequencyChartCard()
    }

    @Test
    fun homeProductSupportCard() {
        prepareMocks()

        launchActivity()

        checkProductSupportCard()
    }

    @Test
    fun homeOralCareSupportCard() {
        prepareMocks()

        launchActivity()

        checkOralCardSupportCard()
    }

    private fun checkProductSupportCard() {
        scrollToProductSupport()

        onView(withText(R.string.product_support_card_title)).check(isVisible())
        onView(withText(R.string.product_support_card_subtitle)).check(isVisible())
        onView(withDrawable(R.drawable.ic_product_support)).check(isVisible())
        makeScreenshot(
            activity.findViewById(R.id.product_support_card),
            "HomeTab_ProductSupportCard"
        )

        runAndCheckIntent(hasComponent(HelpCenterActivity::class.java.name)) {
            onView(withId(R.id.product_support_card)).check(matches(isDisplayed()))
                .perform(click())
        }
    }

    private fun checkOralCardSupportCard() {
        scrollToOralCareSupport()

        onView(withText(R.string.oral_care_support_title)).check(isVisible())
        onView(withText(R.string.oral_care_support_subtitle)).check(isVisible())
        onView(withDrawable(R.drawable.ic_oral_care_support)).check(isVisible())
        makeScreenshot(
            activity.findViewById(R.id.oral_care_support_card),
            "HomeTab_OralCareSupportCard"
        )

        runAndCheckIntent(webViewIntentWithData(context(), R.string.oral_care_support_url)) {
            onView(withId(R.id.oral_care_support_card)).check(matches(isDisplayed()))
                .perform(click())
        }
    }

    private fun checkFrequencyChartCard() {
        scrollToFrequencyCard()

        onView(withText(R.string.frequency_card_title)).check(isVisible())
        makeScreenshot(
            activity.findViewById(R.id.frequency_chart_card),
            "HomeTabFrequencyChartCard"
        )

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val month = formatter.format(TrustedClock.getCurrentMonth())
        onView(withText(month)).check(isVisible())
    }

    private fun assertOpenSmilesHistory() {
        runAndCheckIntent(hasComponent("com.kolibree.android.rewards.smileshistory.SmilesHistoryActivity")) {
            onView(withId(R.id.smiles_background)).check(matches(isDisplayed())).perform(click())
        }
    }

    private fun assertSmilesShowing() {
        onView(withId(R.id.smiles_background)).check(matches(isDisplayed()))
        IdlingResourceFactory.viewVisibility(
            R.id.smiles_value,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.smiles_value)).check(matches(isDisplayed()))
        onView(withId(R.id.smiles_label)).check(matches(isDisplayed()))
    }

    private fun assertSmilesGone() {
        onView(withId(R.id.smiles_background)).check(matches(not(isVisible())))
        onView(withId(R.id.smiles_value)).check(matches(not(isVisible())))
        onView(withId(R.id.smiles_label)).check(matches(not(isVisible())))
    }

    private fun assertDisplayedDuration(expectedDuration: Long) =
        onView(
            withRecyclerView(R.id.content_recyclerview)
                .atPositionOnView(LAST_BRUSHING_CARD_POSITION, R.id.checkup_duration)
        )
            .check(
                matches(
                    withText(
                        DurationFormatter().format(expectedDuration, false)
                    )
                )
            )

    private fun clickOnItemAtPosition(position: Int) =
        onView(withId(R.id.brushing_top_recycler))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(position, scrollTo()))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))

    private fun checkDetailsOnPosition(position: Int, coverage: String, duration: String) {
        clickOnItemAtPosition(position)

        onView(
            withRecyclerView(R.id.content_recyclerview)
                .atPositionOnView(LAST_BRUSHING_CARD_POSITION, R.id.checkup_coverage)
        ).check(matches(withText(coverage)))

        onView(
            withRecyclerView(R.id.content_recyclerview)
                .atPositionOnView(LAST_BRUSHING_CARD_POSITION, R.id.checkup_duration)
        ).check(matches(withText(duration)))
    }

    private fun checkBrushingViewOnPosition(position: Int, brushing: Brushing) {
        onView(withId(R.id.brushing_top_recycler))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(position))

        onView(
            withRecyclerView(R.id.brushing_top_recycler)
                .atPositionOnView(position, R.id.date)
        ).check(matches(withText(day(brushing.dateTime.toLocalDate()))))

        val duration = brushing.duration.toFloat() / brushing.goalDuration
        checkDuration(position, duration)
        checkCoverage(position, 0f)
    }

    private fun brushing(brushingDate: OffsetDateTime) = BrushingBuilder.create()
        .withGame("of")
        .withDuration(50)
        .withGoalDuration(100)
        .withDateTime(brushingDate)
        .build()

    private fun mockProfileBrushings(brushing: List<Brushing>) {
        val repository = component().brushingsRepository()
        whenever(repository.brushingsFlowable(PROFILE_ID)).thenReturn(Flowable.just(brushing))
    }

    private fun checkDashedViewOnPosition(position: Int) {
        onView(withId(R.id.brushing_top_recycler))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(position))

        onView(withRecyclerView(R.id.brushing_top_recycler).atPositionOnView(position, R.id.day))
            .check(matches(withText("--")))

        onView(withRecyclerView(R.id.brushing_top_recycler).atPositionOnView(position, R.id.date))
            .check(matches(withText("--")))

        noCoverageAndDuration(position)
    }

    private fun day(date: LocalDate) = String.format(WITH_LEADING_ZERO_FORMAT, date.dayOfMonth)

    private fun checkNoViewOnPosition(position: Int, date: LocalDate) {
        onView(withId(R.id.brushing_top_recycler))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(position))

        onView(
            withRecyclerView(R.id.brushing_top_recycler)
                .atPositionOnView(position, R.id.date)
        ).check(matches(withText(day(date))))

        noCoverageAndDuration(position)
    }

    private fun noCoverageAndDuration(position: Int) {
        checkCoverage(position, 0f)
        checkDuration(position, 0f)
    }

    private fun checkCoverage(position: Int, coverage: Float) {
        onView(
            withRecyclerView(R.id.brushing_top_recycler)
                .atPositionOnView(position, R.id.coverage_duration_progress)
        ).check(matches(CoverageDurationProgressViewMatchers.withCoverage(coverage)))
    }

    private fun checkDuration(position: Int, duration: Float) {
        onView(
            withRecyclerView(R.id.brushing_top_recycler)
                .atPositionOnView(position, R.id.coverage_duration_progress)
        ).check(matches(CoverageDurationProgressViewMatchers.withDuration(duration)))
    }

    private fun checkLastBrushingCard() {
        onView(
            allOf(
                withText(R.string.last_brushing_card_title),
                isDescendantOfA(withId(R.id.home_tab))
            )
        ).check(isVisible())

        val coverageTitle = context().getString(R.string.last_brushing_card_coverage).toUpperCase()
        onView(
            allOf(
                withText(coverageTitle),
                isDescendantOfA(withId(R.id.home_tab))
            )
        ).check(isVisible())

        val durationTitle = context().getString(R.string.last_brushing_card_duration).toUpperCase()
        onView(
            allOf(
                withText(durationTitle),
                isDescendantOfA(withId(R.id.home_tab))
            )
        ).check(isVisible())

        onView(
            allOf(
                withText(R.string.last_brushing_card_no_brushing),
                isDescendantOfA(withId(R.id.home_tab))
            )
        ).check(isVisible())
    }

    private fun isVisible(): ViewAssertion {
        return matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
    }

    private fun checkEarningPointsCardCollapsedLayout() {
        scrollToEarningPointsCard()

        onView(withId(R.id.earning_points_title))
            .check(matches(isDisplayed()))
        onView(withId(R.id.earning_points_subtitle))
            .check(matches(isDisplayed()))
        onView(withId(R.id.badge))
            .check(matches(isDisplayed()))
        onView(withId(R.id.info_icon))
            .check(matches(isDisplayed()))

        onView(withId(R.id.divider))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.earning_points_detail_title))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.earning_points_detail_body))
            .check(matches(not(isDisplayed())))
    }

    private fun checkEarningPointsCardExpandLayout() {
        onView(withId(R.id.divider))
            .check(matches(isDisplayed()))
        onView(withId(R.id.earning_points_detail_title))
            .check(matches(isDisplayed()))
        onView(withId(R.id.earning_points_detail_body))
            .check(matches(isDisplayed()))
    }

    private fun clickEarningPointsCard() {
        scrollToEarningPointsCard()
        onView(withId(R.id.earning_points_title)).perform(click())
    }
}

private const val WITH_LEADING_ZERO_FORMAT = "%02d"
private const val DUMMY_SMILES_VALUE = 555
const val LAST_BRUSHING_CARD_POSITION = 2
