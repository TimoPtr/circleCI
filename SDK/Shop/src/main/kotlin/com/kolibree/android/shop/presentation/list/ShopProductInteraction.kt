/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import com.kolibree.android.shop.domain.model.Product

interface ShopProductInteraction {
    fun onAddToCartClick(product: Product)
    fun onIncreaseQuantityClick(product: Product)
    fun onDecreaseQuantityClick(product: Product)
}
