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
import com.kolibree.android.shop.ShopifyQueryDSL
import com.kolibree.android.shop.data.request.query.poll.ShippingLinesPollingRequest
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.ShippingRate
import com.kolibree.android.shop.domain.model.Taxes
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.buy3.Storefront.AvailableShippingRates
import java.math.BigDecimal
import java.util.Currency
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test
import org.mockito.stubbing.OngoingStubbing

class UpdateShippingLinesPollingRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates correct build query`() {
        val checkoutId = "123456"
        val request =
            ShippingLinesPollingRequest(
                checkoutId
            )
        val query = request.buildQuery()

        val expectedQuery = """
            {node(id:"$checkoutId"){__typename,... on Checkout{id,availableShippingRates{ready,shippingRates{handle,title,priceV2{amount}}},currencyCode}}}
        """.trimIndent()

        Assert.assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildResponse parses the Rates with an empty shipping rates and correct taxes`() {
        val checkoutId = "123456"
        val currency = Currency.getInstance("EUR")
        val graphResponse = mockShopCheckout(currency)

        val request = ShippingLinesPollingRequest(checkoutId)

        val checkoutDetail = request.buildResponse(response = graphResponse)

        val expectedResponse = Taxes(
            taxesAmount = Price.empty(currency),
            shippingRate = ShippingRate(Price.empty(currency))
        )

        assertEquals(expectedResponse, checkoutDetail)
        assertEquals(Price.empty(currency), expectedResponse.total)
    }

    @Test
    fun `buildResponse parses the Rates with a valid shipping rates`() {
        val currency = Currency.getInstance("EUR")
        val handle = "handle"
        val graphResponse = mockShopCheckout(currency, "15.25", handle)

        val request = ShippingLinesPollingRequest("10")

        val checkoutDetail = request.buildResponse(response = graphResponse)

        val expectedResponse = Taxes(
            taxesAmount = Price.empty(currency),
            shippingRate = ShippingRate(Price.createFromRate(BigDecimal(15.25), currency), handle)
        )

        assertEquals(expectedResponse, checkoutDetail)
        assertEquals(Price.createFromRate(BigDecimal(15.25), currency), expectedResponse.total)
    }

    /*
    Utils
     */

    private fun mockShopCheckout(
        currency: Currency,
        shippingRate: String? = null,
        handle: String? = null
    ): GraphResponse<Storefront.QueryRoot> {
        val totalTaxV2Mock: Storefront.MoneyV2 = mock()
        val currencyCodeMock: Storefront.CurrencyCode = mock()
        val availableShippingRatesMock: AvailableShippingRates = mock()

        whenever(currencyCodeMock.name).thenReturn(currency.currencyCode)
        whenever(availableShippingRatesMock.ready).thenReturn(true)

        shippingRate?.let {
            mockShippingRate(shippingRate, handle, availableShippingRatesMock)
        }

        return ShopifyQueryDSL.checkout {
            availableShippingRates = availableShippingRatesMock
            totalTaxV2 = totalTaxV2Mock
            currencyCode = currencyCodeMock
        }
    }

    private fun mockShippingRate(
        amount: String?,
        handle: String?,
        availableShippingRatesMock: AvailableShippingRates
    ): OngoingStubbing<MutableList<Storefront.ShippingRate>> {

        val shippingRateMock: Storefront.ShippingRate = mock()
        val priceV2Mock: Storefront.MoneyV2 = mock()

        whenever(shippingRateMock.priceV2).thenReturn(priceV2Mock)
        whenever(shippingRateMock.handle).thenReturn(handle)
        whenever(priceV2Mock.amount).thenReturn(amount)

        return whenever(availableShippingRatesMock.shippingRates).thenReturn(listOf(shippingRateMock))
    }
}
