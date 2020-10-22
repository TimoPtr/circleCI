/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.buildProduct
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ProductTest : BaseUnitTest() {

    @Test
    fun `mainImage returns variant image if it's available`() {
        assertEquals(
            "variant.url",
            buildProduct(variantImage = "variant.url", productImages = emptyList()).mainImage
        )
    }

    @Test
    fun `mainImage returns first product image if it's available and variant image is not`() {
        assertEquals(
            "product.url",
            buildProduct(
                variantImage = null,
                productImages = listOf("product.url", "product2.url")
            ).mainImage
        )
    }

    @Test
    fun `mainImage returns null if both variant and product images are missing`() {
        assertNull(buildProduct(productImages = emptyList(), variantImage = null).mainImage)
    }
}
