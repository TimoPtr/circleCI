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
import com.kolibree.android.shop.data.request.query.error.ShopifyInputError
import com.kolibree.android.shop.data.request.query.mutation.UpdateCheckoutAddressMutationRequest
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import junit.framework.TestCase
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test

class UpdateCheckoutAddressMutationRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates correct build query`() {
        val checkout = BasicCheckout("123456", Cart())
        val address = Address(
            firstName = "Mister",
            lastName = "Bean",
            company = "TotoCompany",
            street = "147 Hanover St",
            city = "Boston",
            postalCode = "02108",
            country = "United States",
            province = "MA"
        )

        val request =
            UpdateCheckoutAddressMutationRequest(
                checkout,
                address
            )
        val query = request.buildQuery()

        val expectedQuery =
            """mutation{checkoutShippingAddressUpdateV2(shippingAddress:{address1:"${address.street}",city:"${address.city}",""" +
                """company:"${address.company}",country:"${address.country}",firstName:"${address.firstName}",lastName:"${address.lastName}",""" +
                """province:"${address.province}",zip:"${address.postalCode}"},checkoutId:"${checkout.checkoutId}"){checkoutUserErrors{field,message}}}""".trimIndent()

        Assert.assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildResponse should returns the exact same checkout passed to the request`() {
        val simpleCheckout = BasicCheckout("123456", Cart())
        val address = Address(
            firstName = "Mister",
            lastName = "Bean",
            company = "TotoCompany",
            street = "147 Hanover St",
            city = "Boston",
            postalCode = "02108",
            country = "United States",
            province = "MA"
        )

        val graphResponse = mockShopCheckout(mock()) {
            it.checkoutShippingAddressUpdateV2 = mock()
        }

        val request =
            UpdateCheckoutAddressMutationRequest(
                simpleCheckout,
                address
            )
        val checkoutDetail = request.buildResponse(response = graphResponse)

        TestCase.assertEquals(simpleCheckout, checkoutDetail)
    }

    @Test
    fun `buildResponse should return an error`() {
        val simpleCheckout = BasicCheckout("123456", Cart())
        val addressPayloadMock: Storefront.CheckoutShippingAddressUpdateV2Payload = mock()
        val address = Address(
            firstName = "Mister",
            lastName = "Bean",
            company = "TotoCompany",
            street = "147 Hanover St",
            city = "Boston",
            postalCode = "02108",
            country = "United States",
            province = "MA"
        )

        val expectedError = listOf(Storefront.CheckoutUserError())
        whenever(addressPayloadMock.checkoutUserErrors).thenReturn(expectedError)

        val graphResponse = mockShopCheckout(mock()) {
            it.checkoutShippingAddressUpdateV2 = addressPayloadMock
        }

        val request =
            UpdateCheckoutAddressMutationRequest(
                simpleCheckout,
                address
            )

        var capturedError: ShopifyInputError? = null
        try {
            request.buildResponse(response = graphResponse)
        } catch (error: ShopifyInputError) {
            capturedError = error
        }

        assertThat(capturedError, instanceOf(ShopifyInputError::class.java))
    }

    private fun mockShopCheckout(
        checkout: Storefront.Checkout,
        fillDataBlock: ((Storefront.Mutation) -> Unit)
    ): GraphResponse<Storefront.Mutation> {
        return ShopifyQueryDSL.mutation(checkout, fillDataBlock = fillDataBlock)
    }
}
