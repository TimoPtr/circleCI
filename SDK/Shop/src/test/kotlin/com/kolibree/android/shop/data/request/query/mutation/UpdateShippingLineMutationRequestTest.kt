/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query.mutation

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.ShopifyQueryDSL
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.ShippingRate
import com.kolibree.android.shop.domain.model.Taxes
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import java.math.BigDecimal
import java.util.Currency
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

class UpdateShippingLineMutationRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates correct build query`() {
        val expectedHandle = "expectedHandle"
        val checkoutId = "123456"
        val checkout = BasicCheckout(checkoutId, mock())
        val shippingRate: ShippingRate = mock()
        val taxes = Taxes(mock(), shippingRate)
        val request = UpdateShippingLineMutationRequest(checkout, taxes)

        whenever(shippingRate.handle).thenReturn(expectedHandle)

        val query = request.buildQuery()
        val expectedQuery = """
            mutation{checkoutShippingLineUpdate(checkoutId:"$checkoutId",shippingRateHandle:"$expectedHandle"){checkout{id,totalTaxV2{amount,currencyCode}},checkoutUserErrors{field,message}}}
        """.trimIndent()

        Assert.assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildResponse parses the Taxes`() {
        val currency = Currency.getInstance("EUR")
        val handle = "expectedHandle"
        val checkoutId = "123456"
        val checkout = BasicCheckout(checkoutId, mock())
        val shippingRate = ShippingRate(
            Price.createFromRate(BigDecimal(24.55), currency), handle
        )
        val taxes = Taxes(Price.empty(currency), shippingRate)
        val request = UpdateShippingLineMutationRequest(checkout, taxes)

        val graphResponse = mockShopCheckout("23", currency)

        val checkoutDetail = request.buildResponse(response = graphResponse)

        val expectedResponse = Taxes(
            taxesAmount = Price.createFromRate(BigDecimal(23), currency),
            shippingRate = ShippingRate(
                Price.createFromRate(BigDecimal(24.55), currency), handle
            )
        )

        TestCase.assertEquals(expectedResponse, checkoutDetail)
        TestCase.assertEquals(
            Price.createFromRate(BigDecimal(47.55), currency),
            expectedResponse.total
        )
    }

    private fun mockShopCheckout(
        amount: String,
        currency: Currency
    ): GraphResponse<Storefront.Mutation> {
        val totalTaxV2Mock: Storefront.MoneyV2 = mock()
        val currencyCodeMock: Storefront.CurrencyCode = mock()

        whenever(totalTaxV2Mock.currencyCode).thenReturn(currencyCodeMock)
        whenever(totalTaxV2Mock.amount).thenReturn(amount)
        whenever(currencyCodeMock.name).thenReturn(currency.currencyCode)

        val checkout = Storefront.Checkout().apply {
            totalTaxV2 = totalTaxV2Mock
        }

        return ShopifyQueryDSL.mutation(checkout)
    }
}
