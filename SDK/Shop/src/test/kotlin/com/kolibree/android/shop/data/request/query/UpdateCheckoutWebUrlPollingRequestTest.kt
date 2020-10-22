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
import com.kolibree.android.shop.data.request.query.poll.UpdateCheckoutWebUrlPollingRequest
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

class UpdateCheckoutWebUrlPollingRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates correct build query`() {
        val checkout = BasicCheckout("id123", mock())
        val request = UpdateCheckoutWebUrlPollingRequest(checkout)
        val query = request.buildQuery()

        val expectedQuery = """
            {node(id:"${checkout.checkoutId}"){__typename,... on Checkout{id,webUrl}}}
        """.trimIndent()

        Assert.assertEquals(expectedQuery, query.toString())
    }

    @Test
    fun `buildResponse parses the Rates with an empty shipping rates and correct taxes`() {
        val expectedUrl = "https://youtu.be/dQw4w9WgXcQ"
        val expectedId = "id123"
        val expectedCart: Cart = mock()
        val graphResponse = ShopifyQueryDSL.checkout {
            this.webUrl = expectedUrl
        }

        val checkout = BasicCheckout(expectedId, expectedCart)
        val request = UpdateCheckoutWebUrlPollingRequest(checkout)

        val response = request.buildResponse(response = graphResponse)

        val expectedResponse = WebViewCheckout(
            checkoutId = expectedId,
            cart = expectedCart,
            webUrl = expectedUrl
        )

        assertEquals(expectedResponse, response)
    }
}
