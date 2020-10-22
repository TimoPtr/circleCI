/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.content.Context.MODE_PRIVATE
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToBrushBetterCard
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToBrushingCard
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToFrequencyCard
import com.kolibree.android.persistence.BasePreferences.PREFS_FILENAME
import org.junit.Test

@SuppressWarnings("TooManyFunctions", "LargeClass")
internal class PulsingDotEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun smilePulsingDotIsDisplayedWhenNotClickedAndShowForTheFirstTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = SMILE_KEY,
            timesShown = 0,
            isClicked = false
        )

        launchActivity()
        onView(withId(R.id.pulsing_dot_smiles)).check(matches(isDisplayed()))
    }

    @Test
    fun smilePulsingDotIsNotDisplayedWhenItHasAlreadyBeenClicked() {
        prepareMocks()
        setupPreferences(
            preferenceKey = SMILE_KEY,
            timesShown = 1,
            isClicked = true
        )

        launchActivity()
        onView(withId(R.id.pulsing_dot_smiles)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun smilePulsingDotIsNotDisplayedWhenShownMoreThanFiveTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = SMILE_KEY,
            timesShown = 5,
            isClicked = false
        )

        launchActivity()
        onView(withId(R.id.pulsing_dot_smiles)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun smilePulsingDotFeatureToggleAlwaysVisible() {
        prepareMocks(pulsingDotAlwaysVisible = true)
        setupPreferences(
            preferenceKey = SMILE_KEY,
            timesShown = 123,
            isClicked = true
        )

        launchActivity()
        onView(withId(R.id.pulsing_dot_smiles)).check(matches(isDisplayed()))
    }

    @Test
    fun lastBrushingPulsingDotIsDisplayedWhenNotClickedAndShowForTheFirstTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = LAST_BRUSHING_KEY,
            timesShown = 0,
            isClicked = false
        )

        launchActivity()
        scrollToBrushingCard()
        onView(withId(R.id.pulsing_dot_last_brushing)).check(matches(isDisplayed()))
    }

    @Test
    fun lastBrushingPulsingDotIsNotDisplayedWhenItHasAlreadyBeenClicked() {
        prepareMocks()
        setupPreferences(
            preferenceKey = LAST_BRUSHING_KEY,
            timesShown = 1,
            isClicked = true
        )

        launchActivity()
        scrollToBrushingCard()
        onView(withId(R.id.pulsing_dot_last_brushing)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun lastBrushingPulsingDotIsNotDisplayedWhenShownMoreThanFiveTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = LAST_BRUSHING_KEY,
            timesShown = 5,
            isClicked = false
        )

        launchActivity()
        scrollToBrushingCard()
        onView(withId(R.id.pulsing_dot_last_brushing)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun lastBrushingDotFeatureToggleAlwaysVisible() {
        prepareMocks(pulsingDotAlwaysVisible = true)
        setupPreferences(
            preferenceKey = LAST_BRUSHING_KEY,
            timesShown = 123,
            isClicked = true
        )

        launchActivity()
        scrollToBrushingCard()
        onView(withId(R.id.pulsing_dot_last_brushing)).check(matches(isDisplayed()))
    }

    @Test
    fun frequencyChartPulsingDotIsDisplayedWhenNotClickedAndShowForTheFirstTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = FREQUENCY_CHART_KEY,
            timesShown = 0,
            isClicked = false
        )

        launchActivity()
        scrollToFrequencyCard()
        onView(withId(R.id.pulsing_dot_frequency_chart)).check(matches(isDisplayed()))
    }

    @Test
    fun frequencyChartPulsingDotIsNotDisplayedWhenItHasAlreadyBeenClicked() {
        prepareMocks()
        setupPreferences(
            preferenceKey = FREQUENCY_CHART_KEY,
            timesShown = 1,
            isClicked = true
        )

        launchActivity()
        scrollToFrequencyCard()
        onView(withId(R.id.pulsing_dot_frequency_chart)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun frequencyChartPulsingDotIsNotDisplayedWhenShownMoreThanFiveTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = FREQUENCY_CHART_KEY,
            timesShown = 5,
            isClicked = false
        )

        launchActivity()
        scrollToFrequencyCard()
        onView(withId(R.id.pulsing_dot_frequency_chart)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun frequencyChartDotFeatureToggleAlwaysVisible() {
        prepareMocks(pulsingDotAlwaysVisible = true)
        setupPreferences(
            preferenceKey = FREQUENCY_CHART_KEY,
            timesShown = 123,
            isClicked = true
        )

        launchActivity()
        scrollToFrequencyCard()
        onView(withId(R.id.pulsing_dot_frequency_chart)).check(matches(isDisplayed()))
    }

    @Test
    fun brushBetterPulsingDotIsDisplayedWhenNotClickedAndShowForTheFirstTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = BRUSH_BETTER_KEY,
            timesShown = 0,
            isClicked = false
        )

        launchActivity()
        scrollToBrushBetterCard()
        onView(withId(R.id.pulsing_dot_brush_better)).check(matches(isDisplayed()))
    }

    @Test
    fun brushBetterPulsingDotIsNotDisplayedWhenItHasAlreadyBeenClicked() {
        prepareMocks()
        setupPreferences(
            preferenceKey = BRUSH_BETTER_KEY,
            timesShown = 1,
            isClicked = true
        )

        launchActivity()
        scrollToBrushBetterCard()
        onView(withId(R.id.pulsing_dot_brush_better)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun brushBetterPulsingDotIsNotDisplayedWhenShownMoreThanFiveTime() {
        prepareMocks()
        setupPreferences(
            preferenceKey = BRUSH_BETTER_KEY,
            timesShown = 5,
            isClicked = false
        )

        launchActivity()
        scrollToBrushBetterCard()
        onView(withId(R.id.pulsing_dot_brush_better)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun brushBetterDotFeatureToggleAlwaysVisible() {
        prepareMocks(pulsingDotAlwaysVisible = true)
        setupPreferences(
            preferenceKey = BRUSH_BETTER_KEY,
            timesShown = 123,
            isClicked = true
        )

        launchActivity()
        scrollToBrushBetterCard()
        onView(withId(R.id.pulsing_dot_brush_better)).check(matches(isDisplayed()))
    }

    private fun setupPreferences(
        preferenceKey: String,
        timesShown: Int,
        isClicked: Boolean
    ) {
        context().getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)
            .edit()
            .putInt("${preferenceKey}_times_shown", timesShown)
            .putBoolean("${preferenceKey}_is_clicked", isClicked)
            .apply()
    }
}

private const val SMILE_KEY = "dot_smile"
private const val LAST_BRUSHING_KEY = "dot_last_brushing_session"
private const val FREQUENCY_CHART_KEY = "dot_frequency_chart"
private const val BRUSH_BETTER_KEY = "dot_brush_better"
