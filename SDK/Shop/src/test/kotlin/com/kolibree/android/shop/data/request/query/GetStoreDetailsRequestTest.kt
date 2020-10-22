/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.ShopifyQueryDSL.shop
import com.kolibree.android.shop.ShopifyQueryDSL.shopPaymentSettings
import com.kolibree.android.shop.buildStoreDetails
import com.kolibree.android.shop.domain.model.StoreDetails
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GetStoreDetailsRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates correct build query`() {
        val query = GetStoreDetailsRequest.buildQuery()

        assertEquals(
            "{shop{" +
                "name," +
                "description," +
                "paymentSettings{countryCode,currencyCode,supportedDigitalWallets,acceptedCardBrands}" +
                "}}",
            query.toString()
        )
    }

    @Test
    fun `buildResponse parses the Storefront response correctly`() {
        val expectedResponse = buildStoreDetails()

        val shopDetails =
            GetStoreDetailsRequest.buildResponse(response = mockShopDetails(buildStoreDetails()))

        assertEquals(expectedResponse, shopDetails)
    }

    private fun mockShopDetails(data: StoreDetails): GraphResponse<Storefront.QueryRoot> =
        shop {
            name = data.name
            description = data.description
            paymentSettings = shopPaymentSettings {
                countryCode = Storefront.CountryCode.FR
                currencyCode = Storefront.CurrencyCode.EUR
                supportedDigitalWallets = listOf(Storefront.DigitalWallet.GOOGLE_PAY)
                acceptedCardBrands = listOf(Storefront.CardBrand.VISA)
            }
        }
}
