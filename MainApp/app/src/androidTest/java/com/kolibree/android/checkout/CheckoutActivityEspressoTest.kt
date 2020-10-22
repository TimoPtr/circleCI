/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.checkout

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoShopDataModule
import com.kolibree.android.app.ui.home.BottomNavigationTab
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.checkout.RecyclerViewMatchers.hasItemCount
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withRecyclerView
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.rewards.ProfileRewardsBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.math.BigDecimal
import java.util.Currency
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Ignore
import org.junit.Test

internal class CheckoutActivityEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun openCart_withoutProducts_showEmptyCart() {
        mockApp(emptyList())

        launchCheckoutActivity()

        checkCartIsEmpty()

        makeScreenshot("State")
    }

    @Test
    fun openCart_googlePayNotAvailable_doesntShopGooglePayButton() {
        val products = listOf(productInCart())

        mockApp(products, isGooglePayAvailable = false)
        mockCartDatabase(products.single())

        launchCheckoutActivity()

        onView(withId(R.id.cart_google_pay_buy))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        makeScreenshot("State")
    }

    @Test
    fun openCart_googlePayAvailable_showsGooglePayButton() {
        val products = listOf(productInCart())

        mockApp(products, isGooglePayAvailable = true)
        mockCartDatabase(products.single())

        launchCheckoutActivity()

        onView(withId(R.id.cart_google_pay_buy))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        onView(withId(R.id.cart_another_payment_method))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        onView(withId(R.id.cart_proceed_checkout_button))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        makeScreenshot("State")
    }

    @Test
    fun openCart_googlePayUnavailable_is_gone() {
        val products = listOf(productInCart())

        mockApp(products, isGooglePayAvailable = false)
        mockCartDatabase(products.single())

        launchCheckoutActivity()

        onView(withId(R.id.cart_google_pay_buy))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        onView(withId(R.id.cart_another_payment_method))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        onView(withId(R.id.cart_proceed_checkout_button))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        makeScreenshot("State")
    }

    @Test
    @Ignore("Failing on anbox")
    fun openCart_swipeToRemove_removesProduct() {
        val productInCart1 = createProduct("E1 Head x 5", "1100", "variantInCart_#001")
        val productInCart2 = createProduct("The Toothbrush King E2", "2200", "variantInCart_#002")
        val products = listOf(
            productInCart1,
            productInCart2
        )

        mockApp(products, isGooglePayAvailable = true)
        mockCartDatabase(productInCart1, productInCart2)

        launchCheckoutActivity()

        checkProductOnPosition(0, "E1 Head x 5", "1100")
        checkProductOnPosition(1, "The Toothbrush King E2", "2200")
        checkCartIsNotEmpty()

        swipeToRemoveOnPosition(0)
        checkProductOnPosition(0, "The Toothbrush King E2", "2200")
        checkCartIsNotEmpty()
        checkSnackbarWithUndoAction()

        swipeToRemoveOnPosition(0)
        checkCartIsEmpty()
        checkSnackbarWithUndoAction()

        clickSnackbarUndoAction()
        checkProductOnPosition(0, "The Toothbrush King E2", "2200")
        checkCartIsNotEmpty()
    }

    private fun clickSnackbarUndoAction() {
        onView(allOf(withId(com.google.android.material.R.id.snackbar_action))).perform(click())
    }

    private fun checkSnackbarWithUndoAction() {
        onView(allOf(withId(com.google.android.material.R.id.snackbar_action)))
            .check(matches(withText(R.string.cart_undo_action_title)))
    }

    private fun swipeToRemoveOnPosition(position: Int) {
        onView(withId(R.id.product_list)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                position, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_RIGHT, GeneralLocation.BOTTOM_LEFT,
                    Press.FINGER
                )
            )
        )
    }

    @Test
    @Ignore("Failing on anbox")
    fun openCart_happyPath_no_smiles() {
        val productInCart1 = createProduct("E1 Head x 5", "1100", "variantInCart_#001")
        val productInCart2 = createProduct("The Toothbrush King E2", "3300", "variantInCart_#002")
        val products = listOf(
            productInCart1,
            createProduct("Powerful M1 Toothbrush", "2200", "#_not_in_cart!$"),
            productInCart2
        )

        mockApp(products)

        mockWebViewCheckout()
        mockCartDatabase(productInCart1, productInCart2)

        launchCheckoutActivity()

        checkCartIsNotEmpty()

        checkProductOnPosition(0, "E1 Head x 5", "1100")
        checkProductOnPosition(1, "The Toothbrush King E2", "3300")

        checkQuantityManipulation()

        // product on position 0 has been removed
        checkPaymentDetailsOnPosition(1, 0, false, "3300", "0")
        onView(withId(R.id.switch_use_smiles)).check(matches(not(isEnabled())))

        checkAnotherPaymentScreen()
    }

    @Test
    @Ignore("Failing on anbox")
    fun cart_use_smile_enabled_if_profile_have_smiles() {
        val productInCart1 = createProduct("E1 Head x 5", "1100", "variantInCart_#001")
        val profileIdHenry = 5L
        val profileIdAria = 6L
        val totalsmiles = 600
        val profileHenry = ProfileBuilder.create().withId(profileIdHenry).withName("Henry").build()
        val profileAria = ProfileBuilder.create().withId(profileIdAria).withName("Aria").build()

        profileSmilesDatastore().replace(
            ProfileRewardsBuilder.createProfileSmiles(
                profileIdHenry,
                totalsmiles / 2
            )
        )
        profileSmilesDatastore().replace(
            ProfileRewardsBuilder.createProfileSmiles(
                profileIdAria,
                totalsmiles / 2
            )
        )

        val builder = SdkBuilder.create()
            .withProfiles(profileHenry, profileAria)
            .withActiveProfile(profileHenry)
            .prepareForMainScreen()

        mockApp(
            listOf(
                productInCart1
            ), builder
        )

        mockWebViewCheckout()
        mockCartDatabase(productInCart1)

        launchCheckoutActivity()

        checkCartIsNotEmpty()

        checkPaymentDetails("1094") // sub -> "1100"
        checkPaymentDetailsOnPosition(1, totalsmiles, true, "1100", "-6")

        clickIncreaseQuantityOnPosition(0)

        checkPaymentDetails("2194") // sub -> "2200"
        checkPaymentDetailsOnPosition(1, totalsmiles, true, "2200", "-6")

        onView(withId(R.id.switch_use_smiles)).check(matches(isEnabled())).perform(click())

        checkPaymentDetails("2200") // sub -> "2200"
        checkPaymentDetailsOnPosition(1, totalsmiles, false, "2200", "0")

        checkAnotherPaymentScreen()
    }

    private fun checkQuantityManipulation() {
        checkProductQuantityOnPosition(0, 1)
        checkPaymentDetails("4400")
        checkProductsCount(2)

        clickIncreaseQuantityOnPosition(0)
        checkProductQuantityOnPosition(0, 2)
        checkPaymentDetails("5500")
        checkProductsCount(2)

        clickDecreaseQuantityOnPosition(0)
        checkProductQuantityOnPosition(0, 1)
        checkPaymentDetails("4400")
        checkProductsCount(2)

        clickDecreaseQuantityOnPosition(0)
        checkPaymentDetails("3300")
        checkProductsCount(1)
    }

    private fun checkProductsCount(count: Int) {
        onView(withId(R.id.product_list)).check(matches(hasItemCount(count + 1)))
    }

    private fun clickIncreaseQuantityOnPosition(position: Int) =
        withDelay {
            onView(
                withRecyclerView(R.id.product_list)
                    .atPositionOnView(position, R.id.product_increase)
            ).perform(click())
        }

    private fun clickDecreaseQuantityOnPosition(position: Int) =
        withDelay {
            onView(
                withRecyclerView(R.id.product_list)
                    .atPositionOnView(position, R.id.product_decrease)
            ).perform(click())
        }

    private fun checkProductQuantityOnPosition(position: Int, quantity: Int) {
        onView(
            withRecyclerView(R.id.product_list)
                .atPositionOnView(position, R.id.product_quantity)
        ).check(matches(withText(quantity.toString())))
    }

    private fun checkPaymentDetailsOnPosition(
        position: Int,
        smilesCount: Int,
        isUseSmilesChecked: Boolean,
        subtotal: String,
        smilesDiscount: String
    ) {
        onView(withId(R.id.product_list)).perform(scrollToPosition<RecyclerView.ViewHolder>(position))

        checkSmilesCountDescription(position, smilesCount)

        checkUseSmilesSwitch(position, isUseSmilesChecked)

        checkSubtotal(position, subtotal)

        checkSmilesDiscount(position, smilesDiscount)
    }

    private fun checkSmilesDiscount(position: Int, smilesDiscount: String) {
        val discountPrice = toPrice(smilesDiscount)
        val textualDiscountPrice = discountPrice.formattedPrice()
        onView(
            withRecyclerView(R.id.product_list).atPositionOnView(position, R.id.points_discount)
        ).check(matches(withText(textualDiscountPrice)))
    }

    private fun checkSubtotal(position: Int, subtotal: String) {
        val subtotalPrice = Price.create(BigDecimal(subtotal), Currency.getInstance("EUR"))
        val textualSubtotalPrice = subtotalPrice.formattedPrice()
        onView(
            withRecyclerView(R.id.product_list).atPositionOnView(position, R.id.subtotal)
        ).check(matches(withText(textualSubtotalPrice)))
    }

    private fun checkUseSmilesSwitch(position: Int, isUseSmilesChecked: Boolean) {
        if (isUseSmilesChecked) {
            onView(
                withRecyclerView(R.id.product_list).atPositionOnView(
                    position,
                    R.id.switch_use_smiles
                )
            ).check(matches(allOf(isEnabled(), isChecked())))
        } else {
            onView(
                withRecyclerView(R.id.product_list).atPositionOnView(
                    position,
                    R.id.switch_use_smiles
                )
            ).check(matches(isNotChecked()))
        }
    }

    private fun checkSmilesCountDescription(position: Int, smilesCount: Int) {
        val text = context().getString(
            R.string.cart_use_smile_points,
            smilesCount,
            Price.createFromSmiles(smilesCount, Currency.getInstance("EUR")).formattedPrice()
        )
        onView(
            withRecyclerView(R.id.product_list).atPositionOnView(position, R.id.switch_use_smiles)
        ).check(matches(withText(text)))
    }

    private fun checkCartIsNotEmpty() {
        IdlingResourceFactory.viewVisibility(
            R.id.product_list,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.logo_animation)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cart_empty_icon)).check(matches(not(isDisplayed())))
        onView(withText(R.string.cart_empty_title)).check(matches(not(isDisplayed())))
        onView(withText(R.string.cart_empty_subtitle)).check(matches(not(isDisplayed())))
    }

    private fun checkCartIsEmpty() {
        IdlingResourceFactory.viewVisibility(
            R.id.product_list,
            View.INVISIBLE
        ).waitForIdle()

        onView(withId(R.id.cart_empty_icon)).check(matches(isDisplayed()))
        onView(withText(R.string.cart_empty_title)).check(matches(isDisplayed()))
        onView(withText(R.string.cart_empty_subtitle)).check(matches(isDisplayed()))
        onView(withId(R.id.logo_animation)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cart_payment)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cart_visit_shop)).check(matches(isDisplayed()))
    }

    private fun mockApp(
        products: List<Product>,
        sdkBuilder: SdkBuilder = SdkBuilder.create(),
        isGooglePayAvailable: Boolean = true
    ) {
        AppMocker.create().withSdkBuilder(sdkBuilder)
            .withMockedShopifyProducts(products)
            .prepareForMainScreen()
            .withGooglePayAvailable(isGooglePayAvailable)
            .mock()
    }

    private fun mockWebViewCheckout() {
        val shopifyMock = EspressoShopDataModule.shopifyClientWrapperMock
        val checkout = BasicCheckout(
            checkoutId = "checkout_#id_0019",
            cart = Cart()
        )
        whenever(shopifyMock.createCheckout(any())).thenReturn(Single.just(checkout))
    }

    private fun checkAnotherPaymentScreen() {
        // start payment screen
        onView(withId(R.id.cart_buttons_container)).perform(click())

        onView(withText(R.string.shop_web_payment_toolbar_title)).check(matches(isDisplayed()))
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }

    private fun checkPaymentDetails(totalPriceStr: String) {
        onView(withId(R.id.cart_payment)).check(matches(isDisplayed()))
        onView(withText(R.string.cart_total_title)).check(matches(isDisplayed()))
        onView(withId(R.id.cart_google_pay_buy)).check(matches(isDisplayed()))

        val totalPrice = toPrice(totalPriceStr)
        val textualTotalPrice = totalPrice.formattedPrice()
        onView(withId(R.id.cart_total_price))
            .check(matches(allOf(isDisplayed(), withText(textualTotalPrice))))

        onView(withText(R.string.cart_buy_another_method)).check(matches(isDisplayed()))
    }

    private fun mockCartDatabase(vararg products: Product) {
        for (product in products) {
            component().cartRepository().addProduct(product).blockingGet()
        }
    }

    private fun checkProductOnPosition(position: Int, title: String, amountPrice: String) {
        onView(withRecyclerView(R.id.product_list).atPosition(position))
            .check(matches(hasDescendant(withText(title))))

        val points = context().getString(R.string.shop_points, toPrice(amountPrice).smilePoints)
        onView(withRecyclerView(R.id.product_list).atPositionOnView(position, R.id.product_points))
            .check(matches(withText(points)))

        val price = toPrice(amountPrice).formattedPrice()
        onView(withRecyclerView(R.id.product_list).atPositionOnView(position, R.id.product_price))
            .check(matches(withText(price)))
    }

    private fun createProduct(title: String, price: String, variantId: String) = Product(
        productId = "prod001",
        variantId = variantId,
        description = "desc1",
        htmlDescription = "",
        price = toPrice(price),
        productImages = listOf("http://image.com"),
        productTitle = title,
        productType = "type",
        sku = "SKU0001",
        variantImage = null,
        variantTitle = "variant title"
    )

    private fun toPrice(textualPrice: String) =
        Price.create(BigDecimal(textualPrice), Currency.getInstance("EUR"))

    private fun launchCheckoutActivity() {
        launchActivity()

        bottomNavigationTo(BottomNavigationTab.SHOP)
        clickCartButton()
    }

    private fun clickCartButton() {
        onView(
            allOf(
                withId(R.id.shopping_cart),
                isDescendantOfA(withId(R.id.shop_container))
            )
        ).perform(click())
    }

    private fun productInCart() =
        createProduct("Powerful M1 Toothbrush", "2200", "variantInCart_#001")
}

fun withDelay(actionToSucceed: () -> Unit) {
    Thread.sleep(1000)
    actionToSucceed()
}

object RecyclerViewMatchers {
    @JvmStatic
    fun hasItemCount(itemCount: Int): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(
            RecyclerView::class.java
        ) {

            override fun describeTo(description: Description) {
                description.appendText("has $itemCount items")
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                return view.adapter?.itemCount == itemCount
            }
        }
    }
}
