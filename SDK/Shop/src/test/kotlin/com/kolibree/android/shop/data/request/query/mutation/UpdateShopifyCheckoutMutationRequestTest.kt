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
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.cartWithProducts
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.nhaarman.mockitokotlin2.mock
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import org.junit.Assert.assertEquals
import org.junit.Test

internal class UpdateShopifyCheckoutMutationRequestTest : BaseUnitTest() {

    private val oldCart = Cart()
    private val newCart = cartWithProducts(
        listOf(QuantityProduct(1, buildProduct(productOrdinal = 1)))
    )

    @Test
    fun `buildQuery creates the correct query according to the checkout and shop cart`() {
        val expectedId = "checkoutId"
        val request = UpdateShopifyCheckoutMutationRequest(BasicCheckout(expectedId, oldCart), newCart)

        val query = request.buildQuery()

        val expectedQuery = """
            mutation{checkoutLineItemsReplace(lineItems:[{quantity:1,variantId:"efgh_1_100"}],checkoutId:"$expectedId"){checkout{id},userErrors{field,message}}}
        """.trimIndent()

        assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildResponse parses the ShopifyCheckout response correctly and returns the checkout with the newly updated cart`() {
        val expectedId = "checkoutId"
        val request = UpdateShopifyCheckoutMutationRequest(BasicCheckout(expectedId, oldCart), newCart)

        val checkout: Storefront.Checkout = Storefront.Checkout(ID(expectedId))
        val graphResponse = mockShopCheckout(checkout)

        val updatedCheckout = request.buildResponse(response = graphResponse)

        val expectedResponse = BasicCheckout(
            checkoutId = expectedId,
            cart = newCart
        )
        assertEquals(expectedResponse, updatedCheckout)
    }

    private fun mockShopCheckout(
        checkout: Storefront.Checkout
    ): GraphResponse<Storefront.Mutation> =
        ShopifyQueryDSL.mutation(checkout, fillDataBlock = {
            it.checkoutLineItemsReplace = mock()
        })
}
