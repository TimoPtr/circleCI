/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.shop.data.configuration.ShopifyProductTag
import com.kolibree.android.shop.data.request.query.GetProductsRequest
import com.kolibree.android.shop.data.request.query.GetStoreDetailsRequest
import com.kolibree.android.shop.data.request.query.mutation.ApplyVoucherCheckoutMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.CreateGooglePayCheckoutMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.CreateShopifyCheckoutMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.RemoveVoucherCheckoutMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.UpdateCheckoutAddressMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.UpdateShippingLineMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.UpdateShopifyCheckoutMutationRequest
import com.kolibree.android.shop.data.request.query.poll.ShippingLinesPollingRequest
import com.kolibree.android.shop.data.request.query.poll.UpdateCheckoutWebUrlPollingRequest
import com.kolibree.android.shop.data.request.query.poll.WebViewCheckoutPollingRequest
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.GooglePayCheckout
import com.kolibree.android.shop.domain.model.GoogleWalletPayment
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.StoreDetails
import com.kolibree.android.shop.domain.model.Taxes
import com.kolibree.android.shop.domain.model.Voucher
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.shopify.buy3.GraphClient
import com.shopify.buy3.HttpCachePolicy
import com.shopify.buy3.HttpCachePolicy.Default.CACHE_FIRST
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.util.concurrent.TimeUnit

@SuppressWarnings("TooManyFunctions")
@Keep
interface ShopifyClientWrapper {

    fun getStoreDetails(): Single<StoreDetails>

    fun getProducts(): Single<List<Product>>

    fun createGooglePayCheckout(
        shopifyCheckout: Checkout,
        googleWalletPayment: GoogleWalletPayment
    ): Single<GooglePayCheckout>

    fun createCheckout(address: Address): Single<Checkout>

    fun updateCheckoutCart(checkout: Checkout, cart: Cart): Single<Checkout>

    fun updateCheckoutAddress(checkout: Checkout, address: Address): Single<Checkout>

    fun updateShippingLine(checkout: Checkout, taxes: Taxes): Single<Taxes>

    fun pollShippingRates(checkoutId: String): Single<Taxes>

    fun pollWebUrl(checkout: Checkout): Single<WebViewCheckout>

    fun applyVoucher(voucher: Voucher, checkout: Checkout): Completable

    fun removeVoucher(checkout: Checkout): Completable

    fun checkWebViewCheckoutState(checkout: WebViewCheckout): Single<Boolean>
}

internal class ShopifyClientWrapperImpl(
    private val client: GraphClient,
    private val productTag: ShopifyProductTag
) : ShopifyClientWrapper {

    override fun getStoreDetails(): Single<StoreDetails> =
        client.executeRxQuery(GetStoreDetailsRequest)

    override fun getProducts(): Single<List<Product>> =
        client.executeRxQuery(GetProductsRequest(productTag))

    override fun applyVoucher(voucher: Voucher, checkout: Checkout): Completable =
        client.executeRxMutationQueryCompletable(
            ApplyVoucherCheckoutMutationRequest(voucher, checkout)
        )

    override fun removeVoucher(checkout: Checkout): Completable =
        client.executeRxMutationQueryCompletable(RemoveVoucherCheckoutMutationRequest(checkout))

    override fun checkWebViewCheckoutState(checkout: WebViewCheckout): Single<Boolean> =
        client.executeRxPollingQuery(WebViewCheckoutPollingRequest(checkout))

    override fun createGooglePayCheckout(
        shopifyCheckout: Checkout,
        googleWalletPayment: GoogleWalletPayment
    ): Single<GooglePayCheckout> =
        client.executeRxMutationQuery(
            CreateGooglePayCheckoutMutationRequest(shopifyCheckout, googleWalletPayment)
        )

    override fun createCheckout(address: Address): Single<Checkout> = createShopifyCheckout(address)

    override fun updateCheckoutCart(checkout: Checkout, cart: Cart): Single<Checkout> =
        updateShopifyCheckout(checkout, cart)

    override fun updateCheckoutAddress(checkout: Checkout, address: Address): Single<Checkout> =
        client.executeRxMutationQuery(UpdateCheckoutAddressMutationRequest(checkout, address))

    override fun updateShippingLine(checkout: Checkout, taxes: Taxes): Single<Taxes> =
        client.executeRxMutationQuery(UpdateShippingLineMutationRequest(checkout, taxes))

    override fun pollShippingRates(checkoutId: String): Single<Taxes> =
        client.executeRxPollingQuery(ShippingLinesPollingRequest(checkoutId))

    override fun pollWebUrl(checkout: Checkout): Single<WebViewCheckout> =
        client.executeRxPollingQuery(UpdateCheckoutWebUrlPollingRequest(checkout))

    private fun createShopifyCheckout(address: Address): Single<Checkout> =
        client.executeRxMutationQuery(CreateShopifyCheckoutMutationRequest(address))

    private fun updateShopifyCheckout(checkout: Checkout, cart: Cart): Single<Checkout> =
        client.executeRxMutationQuery(UpdateShopifyCheckoutMutationRequest(checkout, cart))

    companion object {

        const val HTTP_CACHE_SIZE = 10L * 1024 * 1024

        val httpCachePolicy: HttpCachePolicy.ExpirePolicy =
            CACHE_FIRST.expireAfter(5, TimeUnit.MINUTES)

        fun httpCachePath(context: Context): File =
            File(context.applicationContext.cacheDir, "/http")
    }
}
