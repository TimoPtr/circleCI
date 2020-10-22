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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoShopDataModule
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToRewardYourselfCard
import com.kolibree.android.test.assertions.RecyclerViewItemCountAssertion
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Assume.assumeTrue
import org.junit.Test

internal class HomeScreenRewardYourselfEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun rewardYourselfCardIsDisplayed() {
        prepareMocks()

        launchActivity()
        scrollToRewardYourselfCard()
        makeScreenshot(
            activity.findViewById(R.id.reward_yourself_card),
            "RewardYourselfCard"
        )

        onView(withId(R.id.reward_yourself_card_title)).check(matches(isDisplayed()))
        onView(withId(R.id.reward_yourself_card_body)).check(matches(isDisplayed()))
        onView(withId(R.id.reward_yourself_card_items)).check(matches(isDisplayed()))
        onView(withId(R.id.reward_yourself_card_items)).check(
            RecyclerViewItemCountAssertion.withItemCount(EspressoShopDataModule.defaultProductList.size)
        )
    }

    @Test
    fun clickOnRewardItemOpensShopTab() {
        assumeTrue(EspressoShopDataModule.defaultProductList.isNotEmpty())

        prepareMocks()

        launchActivity()
        scrollToRewardYourselfCard()
        scrollToRewardItemsAt(0)
        clickRewardItemAt(0)

        onView(withId(R.id.shop_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun clickOnRewardItemOpensShopTabWithBrandDealsEnabled() {
        assumeTrue(EspressoShopDataModule.defaultProductList.isNotEmpty())

        prepareMocks(showTabsInShop = true)

        launchActivity()
        scrollToRewardYourselfCard()
        scrollToRewardItemsAt(0)
        clickRewardItemAt(0)

        onView(withId(R.id.shop_fragment)).check(matches(isDisplayed()))

        onView(withText(R.string.shop_tab_brand_deals)).perform(click())

        onView(withText(R.string.shop_tab_products))
            .check(matches(allOf(isDisplayed(), not(isSelected()))))
        onView(withText(R.string.shop_tab_brand_deals))
            .check(matches(allOf(isDisplayed(), isSelected())))

        BottomNavigationUtils.navigateToDashboard()

        clickRewardItemAt(0)
        onView(withText(R.string.shop_tab_products))
            .check(matches(allOf(isDisplayed(), isSelected())))
        onView(withText(R.string.shop_tab_brand_deals))
            .check(matches(allOf(isDisplayed(), not(isSelected()))))
    }

    private fun scrollToRewardItemsAt(position: Int) {
        onView(withId(R.id.reward_yourself_card_items))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
    }

    private fun clickRewardItemAt(position: Int) {
        onView(withId(R.id.reward_yourself_card_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    click()
                )
            )
    }
}
