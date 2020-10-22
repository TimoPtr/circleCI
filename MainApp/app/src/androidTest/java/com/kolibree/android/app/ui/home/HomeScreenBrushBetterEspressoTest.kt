/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToBrushBetterCard
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenActivity
import com.kolibree.android.app.ui.home.mindyourspeed.startscreen.MindYourSpeedStartScreenActivity
import com.kolibree.android.app.ui.home.testbrushing.startscreen.TestBrushingStartScreenActivity
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.assertions.RecyclerViewItemCountAssertion
import org.junit.Test

internal class HomeScreenBrushBetterEspressoTest : HomeScreenActivityEspressoTest() {

    override fun setUp() {
        super.setUp()
        init()
    }

    override fun tearDown() {
        super.tearDown()
        release()
    }

    @Test
    fun brushBetterCardIsDisplayed() {
        prepareMocks(showMindYourSpeed = true)

        launchActivity()
        scrollToBrushBetterCard()
        onView(withId(R.id.brush_better_card_title)).check(matches(isDisplayed()))
        onView(withId(R.id.brush_better_card_body)).check(matches(isDisplayed()))
        onView(withId(R.id.brush_better_card_items)).check(matches(isDisplayed()))
        onView(withId(R.id.brush_better_card_items)).check(
            RecyclerViewItemCountAssertion.withItemCount(ITEMS_COUNT)
        )
    }

    @Test
    fun clickOnGuidedBrushingItemOpensProperActivity() {
        prepareMocks()

        launchActivity()
        scrollToBrushBetterCard()
        scrollToBrushItemsAt(GUIDED_BRUSHING_ITEM_POSITION)
        clickBrushItemAt(GUIDED_BRUSHING_ITEM_POSITION)

        intended(
            hasComponent(GuidedBrushingStartScreenActivity::class.java.name),
            times(1)
        )
    }

    @Test
    fun clickOnMindYourSpeedItemOpensProperActivity() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE, showMindYourSpeed = true)

        launchActivity()
        scrollToBrushBetterCard()
        scrollToBrushItemsAt(MIND_YOUR_SPEED_ITEM_POSITION)
        clickBrushItemAt(MIND_YOUR_SPEED_ITEM_POSITION)

        intended(
            hasComponent(MindYourSpeedStartScreenActivity::class.java.name),
            times(1)
        )
    }

    @Test
    fun clickOnTestBrushingItemOpensProperActivity() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE)

        launchActivity()
        scrollToBrushBetterCard()
        scrollToBrushItemsAt(TEST_BRUSHING_ITEM_POSITION)
        clickBrushItemAt(TEST_BRUSHING_ITEM_POSITION)

        intended(
            hasComponent(TestBrushingStartScreenActivity::class.java.name),
            times(1)
        )
    }

    private fun scrollToBrushItemsAt(position: Int) {
        onView(withId(R.id.brush_better_card_items))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    }

    private fun clickBrushItemAt(position: Int) {
        onView(withId(R.id.brush_better_card_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    click()
                )
            )
    }
}

private const val GUIDED_BRUSHING_ITEM_POSITION = 0
private const val TEST_BRUSHING_ITEM_POSITION = 1
private const val MIND_YOUR_SPEED_ITEM_POSITION = 2
private const val ITEMS_COUNT = 3
