/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ShopProductListViewState(
    val products: List<ShopProductBindingModel> = emptyList(),
    val productsResult: ProductsResult = ProductsResult.Loading
) : BaseViewState {

    fun withProducts(products: List<ShopProductBindingModel>) = copy(
        products = products,
        productsResult = if (products.isEmpty()) ProductsResult.NoProducts else ProductsResult.ProductsAvailable
    )

    companion object {
        fun initial() = ShopProductListViewState()
    }
}

internal enum class ProductsResult {
    Loading,
    ProductsAvailable,
    NoProducts
}
