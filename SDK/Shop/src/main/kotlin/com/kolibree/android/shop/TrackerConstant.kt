/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import android.util.Base64
import androidx.annotation.VisibleForTesting
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.EventTracker
import timber.log.Timber

/*
 * See [https://kolibree.atlassian.net/wiki/spaces/PROD/pages/97222835/Shop#Analytics]
 */

@VisibleForTesting
val SHOP_CATEGORY = AnalyticsEvent("Shop")

internal val TAB_PRODUCTS = SHOP_CATEGORY

internal val TAB_BRAND_DEALS = SHOP_CATEGORY + "BrandDeals"

internal val CART_ACTIVITY = SHOP_CATEGORY + "Cart"

@VisibleForTesting
val ADD_TO_CART = SHOP_CATEGORY + "AddToCart"

@VisibleForTesting
val CHECKOUT_CATEGORY = AnalyticsEvent("Checkout")

@VisibleForTesting
const val GPAY_CHECKOUT = "googlepay"

@VisibleForTesting
const val CLASSIC_CHECKOUT = "classic"

internal fun EventTracker.onUseSmilesClick(useSmiles: Boolean) {
    sendEvent(CHECKOUT_CATEGORY + "UseMySmiles" + if (useSmiles) "On" else "Off")
}

internal fun EventTracker.onBuyWithAnotherMethodClick() {
    sendEvent(CHECKOUT_CATEGORY + CLASSIC_CHECKOUT)
}

internal fun EventTracker.onBuyWithGooglePayClick() {
    sendEvent(CHECKOUT_CATEGORY + GPAY_CHECKOUT)
}

internal fun EventTracker.onCheckoutIncreaseQuantityClick(product: Product) {
    sendEvent((CHECKOUT_CATEGORY + "plus").addProduct(product))
}

internal fun EventTracker.onCheckoutDecreaseQuantityClick(product: Product) {
    sendEvent((CHECKOUT_CATEGORY + "minus").addProduct(product))
}

internal fun EventTracker.onCheckoutDelete(product: Product) {
    sendEvent((CHECKOUT_CATEGORY + "delete").addProduct(product))
}

internal fun EventTracker.onCheckoutDeleteUndo() {
    sendEvent(CHECKOUT_CATEGORY + "delete_banner_undo")
}

internal fun EventTracker.onVisitOurShopClick() {
    sendEvent(CART_ACTIVITY + "Empty_Visit")
}

internal fun EventTracker.quitOnEmptyCart() {
    sendEvent(CART_ACTIVITY + "Empty_GoBack")
}

internal fun EventTracker.quit() {
    sendEvent(CART_ACTIVITY + "Quit")
}

internal fun EventTracker.onProductListAddToCartClick(product: Product) {
    sendEvent(ADD_TO_CART.addProduct(product))
}

internal fun EventTracker.onProductListIncreaseQuantityClick(product: Product) {
    sendEvent((ADD_TO_CART + "plus").addProduct(product))
}

internal fun EventTracker.onProductListDecreaseQuantityClick(product: Product) {
    sendEvent((ADD_TO_CART + "minus").addProduct(product))
}

@VisibleForTesting
internal fun decodeVariantFromProduct(product: Product): String? = try {
    String(
        Base64.decode(product.variantId.toByteArray(Charsets.UTF_8), Base64.DEFAULT),
        Charsets.UTF_8
    ).split("/").lastOrNull()
} catch (e: Exception) {
    Timber.e(e)
    null
}

private fun AnalyticsEvent.addProduct(product: Product): AnalyticsEvent =
    plus("variantId" to (decodeVariantFromProduct(product) ?: "Unknown"))
