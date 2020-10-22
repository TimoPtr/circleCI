/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.persistence.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity.Companion.createEntity
import com.kolibree.android.shop.domain.model.Product
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

class CartEntryEntityTest : BaseUnitTest() {

    @Test
    fun `toQuantityProduct returns QuantityProduct with a given product`() {
        val expectedProfileId = 1L
        val expectedProductId = "2"
        val expectedVariantId = "2"
        val expectedQuantity = 10
        val expectedProduct = mock<Product>()

        whenever(expectedProduct.productId).thenReturn(expectedProductId)
        whenever(expectedProduct.variantId).thenReturn(expectedVariantId)

        val entry = CartEntryEntity(expectedProfileId, expectedProductId, expectedVariantId, expectedQuantity)
        val result = entry.toQuantityProduct(expectedProduct)

        assertEquals(expectedProduct, result!!.product)
        assertEquals(expectedQuantity, result.quantity)
    }

    @Test
    fun `decreaseQuantity removes 1 to quantity when quantity greater than 0`() {
        val expectedProfileId = 1L
        val expectedProductId = "2"
        val expectedVariantId = "2"
        val expectedQuantity = 1

        val entry = CartEntryEntity(expectedProfileId, expectedProductId, expectedVariantId, expectedQuantity)
        val result = entry.decreaseQuantity()
        assertEquals(0, result.quantity)
        assertEquals(expectedProfileId, result.profileId)
        assertEquals(expectedProductId, result.productId)
        assertEquals(expectedVariantId, result.variantId)
    }

    @Test
    fun `decreaseQuantity does nothing when quantity equals 0 and return same CartEntryEntity`() {
        val expectedProfileId = 1L
        val expectedProductId = "2"
        val expectedVariantId = "2"
        val expectedQuantity = 0

        val entry = CartEntryEntity(expectedProfileId, expectedProductId, expectedVariantId, expectedQuantity)
        val result = entry.decreaseQuantity()
        assertEquals(entry, result)
    }

    @Test
    fun `increaseQuantity adds 1 to the quantity`() {
        val expectedProfileId = 1L
        val expectedProductId = "2"
        val expectedVariantId = "2"
        val expectedQuantity = 0

        val entry = CartEntryEntity(expectedProfileId, expectedProductId, expectedVariantId, expectedQuantity)
        val result = entry.increaseQuantity()
        assertEquals(1, result.quantity)
        assertEquals(expectedProfileId, result.profileId)
        assertEquals(expectedProductId, result.productId)
        assertEquals(expectedVariantId, result.variantId)
    }

    @Test
    fun `createEntity create Entity from a Product`() {
        val expectedProfileId = 1L
        val expectedProductId = "2"
        val expectedVariantId = "2"
        val expectedQuantity = 10
        val expectedProduct = mock<Product>()

        whenever(expectedProduct.productId).thenReturn(expectedProductId)
        whenever(expectedProduct.variantId).thenReturn(expectedVariantId)

        val result = expectedProduct.createEntity(expectedProfileId, expectedQuantity)

        assertEquals(expectedProfileId, result.profileId)
        assertEquals(expectedProductId, result.productId)
        assertEquals(expectedVariantId, result.variantId)
        assertEquals(expectedQuantity, result.quantity)
    }
}
