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
import com.kolibree.android.shop.data.request.query.error.ShopifyCheckoutUserError
import com.kolibree.android.shop.data.request.query.mutation.CreateGooglePayCheckoutMutationRequest
import com.kolibree.android.shop.data.request.query.mutation.toMailingAddressInput
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.googlePayCheckout
import com.kolibree.android.shop.googleWalletPayment
import com.nhaarman.mockitokotlin2.mock
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import java.util.UUID
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class CreateGooglePayCheckoutMutationRequestTest : BaseUnitTest() {
    private val checkoutId = "dada"
    private val shopifyCheckout = BasicCheckout(
        checkoutId = checkoutId,
        cart = Cart()
    )

    private val uuid = UUID.randomUUID()

    private val googleWalletPayment = googleWalletPayment()

    private val request =
        CreateGooglePayCheckoutMutationRequest(
            requestedCheckout = shopifyCheckout,
            googlePayment = googleWalletPayment,
            uuid = uuid
        )

    @Test
    fun `buildQuery creates correct build query`() {
        val query = request.buildQuery()

        val billingAddress = googleWalletPayment.toMailingAddressInput()

        /*
        mutation{checkoutCompleteWithTokenizedPayment(
        checkoutId:"...",
        payment:{amount:"8.26",
        idempotencyKey:"...",
        billingAddress:{address1:"..",address2:"",city:"...",country:"..",firstName:"...",province:"...",zip:"..."},
        type:"google_pay",
        paymentData:"examplePaymentMethodToken",
        test:true})
        {payment{id,ready,errorMessage},checkout{id,ready},userErrors{field,message}}}
         */
        val queryStringBuilder = StringBuilder("mutation{checkoutCompleteWithTokenizedPayment(")
        queryStringBuilder.append("""checkoutId:"$checkoutId",""")
        queryStringBuilder.append("payment:{")
        queryStringBuilder.append("""amount:"${googleWalletPayment.amount.decimalAmount}",""")
        queryStringBuilder.append("""idempotencyKey:"$uuid",""")
        queryStringBuilder.append("""billingAddress:""")

        billingAddress.appendTo(queryStringBuilder)

        queryStringBuilder.append(""",type:"google_pay",""")
        queryStringBuilder.append("""paymentData:"${googleWalletPayment.token}",""")
        queryStringBuilder.append("""test:true""")

        queryStringBuilder.append("}") // close payment
        queryStringBuilder.append(")") // close checkoutCompleteWithTokenizedPayment
        queryStringBuilder.append("{payment{id,ready,errorMessage},") // payment query
        queryStringBuilder.append("checkout{id,ready},") // payment query
        queryStringBuilder.append("checkoutUserErrors{field,message}") // payment query
        queryStringBuilder.append("}") // close payment query
        queryStringBuilder.append("}") // close mutation

        assertEquals(queryStringBuilder.toString(), query.toString())
    }

    @Test
    fun `buildResponse parses the Storefront response correctly`() {
        val order: Storefront.Order = mock()
        val checkout: Storefront.Checkout = Storefront.Checkout(ID(checkoutId))
        checkout.order = order
        val graphResponse = mockShopCheckout(checkout)

        val shopDetails = request.buildResponse(response = graphResponse)

        val expectedResponse = googlePayCheckout(
            order = order,
            checkout = checkout
        )
        assertEquals(expectedResponse, shopDetails)
    }

    @Test
    fun `buildResponse returns ShopifyUserError on error`() {
        val order: Storefront.Order = mock()
        val checkout: Storefront.Checkout = Storefront.Checkout(ID(checkoutId))
        checkout.order = order
        val userError = Storefront.CheckoutUserError().apply {
            message = "blabla"
        }
        val graphResponse = mockShopCheckout(checkout) {
            add(userError)
        }

        var capturedError: ShopifyCheckoutUserError? = null
        try {
            request.buildResponse(response = graphResponse)
        } catch (sue: ShopifyCheckoutUserError) {
            capturedError = sue
        }

        val expectedError = ShopifyCheckoutUserError(listOf(userError))
        assertEquals(expectedError.errors, capturedError!!.errors)
    }

    /*
    Utils
     */

    private fun mockShopCheckout(
        checkout: Storefront.Checkout,
        fillErrorsBlock: MutableList<Storefront.CheckoutUserError>.() -> Unit = {}
    ): GraphResponse<Storefront.Mutation> =
        ShopifyQueryDSL.mutation(checkout, fillErrorsBlock)
}
