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
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.checkout.withDelay
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withRecyclerView
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.EspressoProduct
import com.kolibree.android.test.utils.SdkBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import java.math.BigDecimal
import java.util.Currency
import org.hamcrest.CoreMatchers.not
import org.junit.Test

@Suppress("LongMethod", "FunctionNaming")
internal open class ShopTabEspressoTest : HomeScreenActivityEspressoTest() {

    open fun shouldShowTabs(): Boolean = false

    @Test
    fun shopTab_happyPath() {
        val products = listOf(
            createProduct("E1 Toothbrush Product", "1000"),
            createProduct("Super M1 Toothbrush", "2000"),
            createProduct("New E2 Toothbrush", "3000")
        )

        AppMocker.create().withSdkBuilder(SdkBuilder.create())
            .withMockedShopifyData(products)
            .withFeature(ShowShopTabsFeature, shouldShowTabs())
            .prepareForMainScreen()
            .mock()

        launchActivity()

        BottomNavigationUtils.navigateToShop()

        checkTabs()

        checkProductsInCartCount(0)

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withText(R.string.shop_no_products)).check(matches(not(isDisplayed())))

        checkProductOnPosition(0, "E1 Toothbrush Product", "1000")
        checkProductOnPosition(1, "Super M1 Toothbrush", "2000")
        checkProductOnPosition(2, "New E2 Toothbrush", "3000")

        makeScreenshot("InitialState")

        checkProductQuantityManipulation()
    }

    private fun checkTabs() {
        if (shouldShowTabs()) {
            onView(withText(R.string.shop_tab_products)).check(matches(isDisplayed()))
            onView(withText(R.string.shop_tab_brand_deals)).check(matches(isDisplayed()))
        } else {
            onView(withText(R.string.shop_tab_products)).check(matches(not(isDisplayed())))
            onView(withText(R.string.shop_tab_brand_deals)).check(doesNotExist())
        }
    }

    private fun checkProductQuantityManipulation() {
        checkProductQuantityOnPosition(0, 0)
        clickIncreaseQuantityOnPosition(0)
        checkProductsInCartCount(1)
        checkProductQuantityOnPosition(0, 1)
        clickIncreaseQuantityOnPosition(0)
        checkProductsInCartCount(2)
        checkProductQuantityOnPosition(0, 2)
        clickIncreaseQuantityOnPosition(0)
        checkProductQuantityOnPosition(0, 3)
        checkProductsInCartCount(3)

        makeScreenshot("ManipulatedState")

        clickDecreaseQuantityOnPosition(0)
        checkProductQuantityOnPosition(0, 2)
        clickDecreaseQuantityOnPosition(0)
        checkProductQuantityOnPosition(0, 1)
        clickDecreaseQuantityOnPosition(0)
    }

    private fun clickIncreaseQuantityOnPosition(position: Int) =
        withDelay {
            onView(
                withRecyclerView(R.id.product_list).atPositionOnView(
                    position,
                    R.id.product_increase
                )
            ).perform(click())
        }

    private fun clickDecreaseQuantityOnPosition(position: Int) =
        withDelay {
            onView(
                withRecyclerView(R.id.product_list).atPositionOnView(
                    position,
                    R.id.product_decrease
                )
            ).perform(click())
        }

    private fun checkProductQuantityOnPosition(position: Int, quantity: Int) {
        onView(withId(R.id.product_list))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
        onView(
            withRecyclerView(R.id.product_list).atPositionOnView(position, R.id.product_quantity)
        ).check(matches(withText(quantity.toString())))
    }

    private fun checkProductsInCartCount(productsInCount: Int) {
        // not implemented yet in HUM
    }

    @Test
    fun shopTab_noProducts() {
        AppMocker.create().withSdkBuilder(SdkBuilder.create())
            .withMockedShopifyData(emptyList())
            .withFeature(ShowShopTabsFeature, shouldShowTabs())
            .prepareForMainScreen()
            .mock()

        launchActivity()
        BottomNavigationUtils.navigateToShop()

        checkTabs()

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withText(R.string.shop_no_products)).check(matches(isDisplayed()))

        makeScreenshot("State")
    }

    @Test
    fun discountBanner_happyPath() {
        val profileId1 = 1986L
        val expectedProfileSmiles = 2001

        val products = listOf(
            createProduct("New E2 Toothbrush", "3000")
        )

        AppMocker
            .create()
            .withFeature(ShowShopTabsFeature, shouldShowTabs())
            .withMockedShopifyData(products)
            .withProfileSmiles(profileId1, "Profile", expectedProfileSmiles)
            .prepareForMainScreen()
            .mock()

        whenever(component().brushingsRepository().getLastBrushingSessionFlowable(any()))
            .thenReturn(Flowable.never())

        launchActivity()

        // Go to Shop tab
        BottomNavigationUtils.navigateToShop()

        checkTabs()

        // Assert that the discount banner is present and displays correct data
        assertDiscountBannerVisible("Save €20.01")

        makeScreenshot("State")
    }

    @Test
    fun discountBanner_noPoints_noBanner() {
        AppMocker
            .create()
            .withFeature(ShowShopTabsFeature, shouldShowTabs())
            .withMockedShopifyData(listOf())
            .withProfileSmiles(1986L, "Profile", 0)
            .prepareForMainScreen()
            .mock()

        whenever(component().brushingsRepository().getLastBrushingSessionFlowable(any()))
            .thenReturn(Flowable.never())

        launchActivity()

        // Go to Shop tab
        BottomNavigationUtils.navigateToShop()

        checkTabs()

        // Banner is not present
        onView(withId(R.id.discount_banner)).check(matches(not(isDisplayed())))

        makeScreenshot("State")
    }

    private fun checkProductOnPosition(position: Int, title: String, amountPrice: String) {
        onView(withId(R.id.product_list))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(position))

        onView(withRecyclerView(R.id.product_list).atPosition(position))
            .check(matches(hasDescendant(withText(title))))

        val points = context().getString(R.string.shop_points, toPrice(amountPrice).smilePoints)
        onView(withRecyclerView(R.id.product_list).atPosition(position))
            .check(matches(hasDescendant(withText(points))))

        val price = toPrice(amountPrice).formattedPrice()
        onView(withRecyclerView(R.id.product_list).atPosition(position))
            .check(matches(hasDescendant(withText(price))))
    }

    protected fun createProduct(title: String, price: String) =
        EspressoProduct(
            product = Product(
                productId = "prod001",
                variantId = "variant001",
                description = "desc1",
                htmlDescription = "",
                price = toPrice(price),
                productImages = listOf("http://image.com"),
                productTitle = title,
                productType = "type",
                sku = "SKU0001",
                variantImage = null,
                variantTitle = "variant title"
            ),
            quantity = 0
        )

    private fun toPrice(textualPrice: String) =
        Price.create(BigDecimal(textualPrice), Currency.getInstance("EUR"))

    private fun assertDiscountBannerVisible(expectedSavingsCaption: String) {
        onView(withId(R.id.discount_banner)).check(matches(isDisplayed()))
        onView(withText(R.string.shop_discount_caption)).check(matches(isDisplayed()))
        onView(withId(R.id.discount_amount))
            .check(matches(withText(expectedSavingsCaption)))
    }

    override fun makeScreenshot(name: String) =
        super.makeScreenshot(name + if (shouldShowTabs()) "_WithTabs" else "")
}
