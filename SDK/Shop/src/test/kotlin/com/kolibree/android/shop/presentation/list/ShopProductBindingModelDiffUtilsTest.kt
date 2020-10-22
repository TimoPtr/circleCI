/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.presentation.list.ShopProductBindingModelDiffUtils.areContentsTheSame
import com.kolibree.android.shop.presentation.list.ShopProductBindingModelDiffUtils.areItemsTheSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ShopProductBindingModelDiffUtilsTest : BaseUnitTest() {

    @Test
    fun `areItemsTheSame returns true if product in items are the same`() {
        assertTrue(areItemsTheSame(toItem(product1, 6), toItem(product1, 6)))
        assertTrue(areItemsTheSame(toItem(product2, 0), toItem(product2, 4)))
        assertTrue(areItemsTheSame(toItem(product3, 19), toItem(product3, 42)))
        assertFalse(areItemsTheSame(toItem(product1, 13), toItem(product2, 13)))
        assertFalse(areItemsTheSame(toItem(product2, 2), toItem(product3, 3)))
    }

    @Test
    fun `areContentsTheSame returns true if items are exactly the same `() {
        assertTrue(areContentsTheSame(toItem(product1, 12), toItem(product1, 12)))
        assertTrue(areContentsTheSame(toItem(product2, 0), toItem(product2, 0)))
        assertFalse(areContentsTheSame(toItem(product3, 4), toItem(product3, 5)))
        assertFalse(areContentsTheSame(toItem(product1, 11), toItem(product2, 11)))
    }

    private fun toItem(product: Product, quantity: Int) =
        ShopProductBindingModel(product, quantity)

    companion object {
        val product1 = buildProduct(productOrdinal = 1, variantOrdinal = 2)
        val product2 = buildProduct(productOrdinal = 2, variantOrdinal = 3)
        val product3 = buildProduct(productOrdinal = 3, variantOrdinal = 4)
    }
}
