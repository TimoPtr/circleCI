/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test

internal open class ShopTabWithProductAndBrandDealsEspressoTest : ShopTabEspressoTest() {

    override fun shouldShowTabs(): Boolean = true

    @Test
    fun tabsSwitching_happyPath() {
        val products = listOf(
            createProduct("E1 Toothbrush Product", "1000")
        )

        AppMocker.create().withSdkBuilder(SdkBuilder.create())
            .withMockedShopifyData(products)
            .withFeature(ShowShopTabsFeature, shouldShowTabs())
            .prepareForMainScreen()
            .mock()

        launchActivity()

        BottomNavigationUtils.navigateToShop()

        onView(withText(R.string.shop_tab_products))
            .check(matches(allOf(isDisplayed(), isSelected())))
        onView(withText(R.string.shop_tab_brand_deals))
            .check(matches(allOf(isDisplayed(), not(isSelected()))))

        makeScreenshot("ProductTab")

        onView(withText(R.string.shop_tab_brand_deals)).perform(click())

        onView(withText(R.string.shop_tab_products))
            .check(matches(allOf(isDisplayed(), not(isSelected()))))
        onView(withText(R.string.shop_tab_brand_deals))
            .check(matches(allOf(isDisplayed(), isSelected())))

        makeScreenshot("BrandDealsTab")
    }
}
