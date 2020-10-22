/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.wallet

import androidx.annotation.VisibleForTesting
import com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_PRODUCTION
import com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_TEST
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.GooglePayProductionEnvironmentFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.googlewallet.GooglePayTheme
import com.kolibree.android.shop.domain.model.StoreDetails
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.CardBrand.AMERICAN_EXPRESS
import com.shopify.buy3.Storefront.CardBrand.DINERS_CLUB
import com.shopify.buy3.Storefront.CardBrand.DISCOVER
import com.shopify.buy3.Storefront.CardBrand.JCB
import com.shopify.buy3.Storefront.CardBrand.MASTERCARD
import com.shopify.buy3.Storefront.CardBrand.VISA
import io.reactivex.Single
import javax.inject.Inject

internal interface GoogleWalletConfiguration {
    val paymentsEnvironment: Int
    val supportedMethods: List<String>
    val paymentGatewayTokenizationName: String
    val paymentGatewayTokenizationParameters: Map<String, String>
    val isProductionEnvironment: Boolean

    val theme: Int

    fun supportedNetworks(): Single<List<String>>
    fun storeCountryCode(): Single<String>
}

internal class GoogleWalletConfigurationImpl
@Inject constructor(
    featureToggleSet: FeatureToggleSet,
    private val shopifyClientWrapper: ShopifyClientWrapper,
    @GooglePayTheme override val theme: Int
) : GoogleWalletConfiguration {

    override val isProductionEnvironment: Boolean = featureToggleSet.useProductionEnvironment()

    /**
     * @value the environment to use for payments. [ENVIRONMENT_PRODUCTION] or [ENVIRONMENT_TEST]
     */
    override val paymentsEnvironment: Int =
        if (isProductionEnvironment) ENVIRONMENT_PRODUCTION else ENVIRONMENT_TEST

    private var storeDetails: StoreDetails? = null

    /*
    This implementation isn't exactly thread safe, but since the returned StoredDetails for a
    session should remain constant, I don't mind that theoretically 2 getStoreDetails() can
    run at the same time
     */
    @VisibleForTesting
    fun fetchStoreDetails(): Single<StoreDetails> {
        return synchronized(this) {
            if (storeDetails == null) {
                shopifyClientWrapper.getStoreDetails()
                    .doOnSuccess { storeDetails ->
                        synchronized(this) {
                            this.storeDetails = storeDetails
                        }
                    }
            } else {
                Single.just(storeDetails)
            }
        }
    }

    /**
     * The allowed networks to be requested from the API. If the user has cards from networks not
     * specified here in their account, these will not be offered for them to choose in the popup.
     */
    override fun supportedNetworks(): Single<List<String>> {
        return fetchStoreDetails()
            .map { storeDetails -> readCardBrands(storeDetails) }
            .map { brands ->
                brands
                    .map { it.toGooglePayNetwork() }
                    .filterNot { it == GoogleWalletNetwork.UNKNOWN }
                    .map { it.jsonValue }
            }
    }

    private fun readCardBrands(storeDetails: StoreDetails): List<Storefront.CardBrand> {
        return if (storeDetails.acceptedCardBrands.isEmpty() && !isProductionEnvironment) {
            // TestShop. Accept all cards
            Storefront.CardBrand.values().toList()
        } else {
            storeDetails.acceptedCardBrands.map { Storefront.CardBrand.valueOf(it) }
        }
    }

    override fun storeCountryCode(): Single<String> {
        return fetchStoreDetails()
            .map { storeDetails -> storeDetails.countryCode }
    }

    /**
     * The Google Pay API may return cards on file on Google.com (PAN_ONLY) and/or a device token on
     * an Android device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS).
     */
    override val supportedMethods = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"
    )

    /**
     * The name of your payment processor/gateway
     */
    override val paymentGatewayTokenizationName: String = "shopify"

    /**
     * Custom parameters required by the processor/gateway.
     */
    override val paymentGatewayTokenizationParameters: Map<String, String> = mapOf(
        "gateway" to paymentGatewayTokenizationName,
        "gatewayMerchantId" to "8929e7790a3cba7e2116ba8a8ba7c8d2" // test shop in Colgate US
    )
}

private fun FeatureToggleSet.useProductionEnvironment() =
    toggleForFeature(GooglePayProductionEnvironmentFeature).value

/**
 * https://developers.google.com/pay/api/web/reference/request-objects#CardParameters
 */
@VisibleForTesting
internal enum class GoogleWalletNetwork(val jsonValue: String) {
    AMERICAN_EXPRESS("AMEX"),
    DISCOVER("DISCOVER"),
    JCB("JCB"),
    INTERAC("INTERAC"),
    MASTERCARD("MASTERCARD"),
    VISA("VISA"),
    UNKNOWN("")
}

@VisibleForTesting
internal fun Storefront.CardBrand.toGooglePayNetwork(): GoogleWalletNetwork {
    return when (this) {
        AMERICAN_EXPRESS -> GoogleWalletNetwork.AMERICAN_EXPRESS
        DISCOVER -> GoogleWalletNetwork.DISCOVER
        JCB -> GoogleWalletNetwork.JCB
        MASTERCARD -> GoogleWalletNetwork.MASTERCARD
        VISA -> GoogleWalletNetwork.VISA
        DINERS_CLUB -> GoogleWalletNetwork.UNKNOWN
        else -> GoogleWalletNetwork.UNKNOWN
    }
}
