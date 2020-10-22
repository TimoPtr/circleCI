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
import org.junit.Assert.assertEquals
import org.junit.Test

class ShopListScrollUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: ShopListScrollUseCase

    override fun setup() {
        super.setup()
        useCase = ShopListScrollUseCaseImpl()
    }

    @Test
    fun `preserves only latest item id`() {
        useCase.scrollToItem("1")
        useCase.scrollToItem("2")
        useCase.scrollToItem("3")

        val observer = useCase.getItemIdToScroll().test()
        assertEquals(1, observer.valueCount())
        assertEquals("3", observer.values().first())
    }
}
