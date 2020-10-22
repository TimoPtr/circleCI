/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import android.os.Bundle
import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.shopify.buy3.Storefront
import kotlinx.android.parcel.Parcelize

@VisibleForApp
sealed class Checkout : Parcelable {
    abstract val checkoutId: String
    abstract val cart: Cart
}

@VisibleForApp
@Parcelize
data class BasicCheckout(
    override val checkoutId: String,
    override val cart: Cart
) : Checkout()

@VisibleForApp
@Parcelize
data class GooglePayCheckout(
    val orderId: Storefront.Order,
    val checkout: Storefront.Checkout,
    override val checkoutId: String = checkout.id.toString(),
    override val cart: Cart
) : Checkout()

@VisibleForApp
@Parcelize
data class WebViewCheckout(
    override val checkoutId: String,
    override val cart: Cart,
    val webUrl: String
) : Checkout() {

    fun asArguments(): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(EXTRA_WEB_VIEW_CHECKOUT, this)
        return bundle
    }

    @VisibleForApp
    companion object {

        private const val EXTRA_WEB_VIEW_CHECKOUT = "EXTRA_WEB_VIEW_CHECKOUT"

        fun extractArguments(bundle: Bundle?): WebViewCheckout {
            if (bundle == null) throw IllegalStateException()
            if (!bundle.containsKey(EXTRA_WEB_VIEW_CHECKOUT)) throw IllegalStateException()

            return checkNotNull<WebViewCheckout>(bundle.getParcelable(EXTRA_WEB_VIEW_CHECKOUT)) {
                "Impossible to parse WebViewCheckout"
            }
        }
    }
}
