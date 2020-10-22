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
import com.kolibree.android.shop.data.request.query.mutation.CreateShopifyCheckoutMutationRequest
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CreateShopifyCheckoutMutationRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates the correct build query if the address is empty`() {
        val address = Address.empty()
        val request = CreateShopifyCheckoutMutationRequest(address)

        val query = request.buildQuery()

        val expectedQuery = """
            mutation{checkoutCreate(input:{}){checkout{id},checkoutUserErrors{field,message}}}
        """.trimIndent()

        assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildQuery creates the correct build query if the address has a mail`() {
        val address = Address(email = "elprofessor@delacasa.delpapel")
        val request = CreateShopifyCheckoutMutationRequest(address)

        val query = request.buildQuery()

        val expectedQuery = """
            mutation{checkoutCreate(input:{email:"elprofessor@delacasa.delpapel"}){checkout{id},checkoutUserErrors{field,message}}}
        """.trimIndent()

        assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildQuery creates the correct build query if the address is complete`() {
        val address = Address(
            firstName = "Freddie",
            lastName = "Mercury",
            street = "123 Street",
            city = "Boston",
            postalCode = "456 789",
            country = "United States",
            province = "MA-MA",
            email = "bohemian@rhapsody.ma",
            phoneNumber = "+33123456789"
        )

        val request = CreateShopifyCheckoutMutationRequest(address)
        val query = request.buildQuery()

        val expectedQuery = """
            mutation{checkoutCreate(input:{email:"bohemian@rhapsody.ma",shippingAddress:{address1:"123 Street",city:"Boston",country:"United States",firstName:"Freddie",lastName:"Mercury",phone:"+33123456789",province:"MA-MA",zip:"456 789"}}){checkout{id},checkoutUserErrors{field,message}}}
        """.trimIndent()

        assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildResponse parses the ShopifyCheckout response correctly and returns an empty Cart`() {
        val address = Address.empty()
        val request = CreateShopifyCheckoutMutationRequest(address)

        val expectedCheckoutId = "das"
        val checkout: Storefront.Checkout = Storefront.Checkout(ID(expectedCheckoutId))
        val graphResponse = mockShopCheckout(checkout)

        val createdCheckout = request.buildResponse(response = graphResponse)

        val expectedResponse = BasicCheckout(
            checkoutId = expectedCheckoutId,
            cart = Cart()
        )
        TestCase.assertEquals(expectedResponse, createdCheckout)
    }

    /*
    Utils
     */

    private fun mockShopCheckout(
        checkout: Storefront.Checkout
    ): GraphResponse<Storefront.Mutation> =
        ShopifyQueryDSL.mutation(checkout)
}
