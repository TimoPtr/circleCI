/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.kolibree.android.shop.BR
import com.kolibree.android.shop.R
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.presentation.ShopItemBindingModel
import com.kolibree.android.shop.presentation.checkout.cart.ShopPaymentDetailsBindingModel
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding

@Parcelize
internal data class ShopProductBindingModel(
    val product: Product,
    val quantity: Int,
    val withBottomDivider: Boolean = true
) : ShopItemBindingModel {

    @IgnoredOnParcel
    val imageUrl = product.mainImage

    @IgnoredOnParcel
    val title = product.productTitle

    fun formatPoints(context: Context): CharSequence {
        val smilePoints = product.price.smilePoints.toString()
        return context.getString(R.string.shop_points, smilePoints)
    }

    fun textualPrice() = product.price.formattedPrice()

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_shop_product_list_item)
    }
}

internal object ShopProductBindingModelDiffUtils :
    DiffUtil.ItemCallback<ShopItemBindingModel>() {

    override fun areItemsTheSame(
        oldItem: ShopItemBindingModel,
        newItem: ShopItemBindingModel
    ) = when {
        oldItem is ShopProductBindingModel && newItem is ShopProductBindingModel -> {
            oldItem.product == newItem.product
        }
        oldItem is ShopPaymentDetailsBindingModel && newItem is ShopPaymentDetailsBindingModel -> {
            true
        }
        else -> false
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: ShopItemBindingModel,
        newItem: ShopItemBindingModel
    ) = oldItem == newItem
}
