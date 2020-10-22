/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import com.kolibree.android.app.ui.text.TextPaintModifiers
import com.kolibree.android.shop.BR
import com.kolibree.android.shop.R
import com.kolibree.android.shop.domain.model.Taxes
import com.kolibree.android.shop.presentation.ShopItemBindingModel
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding

@Parcelize
internal data class ShopPaymentDetailsBindingModel(
    val actualDiscount: String = "",
    val potentialDiscountPrice: String = "",
    val potentialDiscountSmilePoints: String = "",
    val subtotal: String = "",
    val useSmiles: Boolean = false,
    val isPossibleToUseSmiles: Boolean = false,
    val taxes: Taxes? = null
) : ShopItemBindingModel {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.apply {
            set(BR.item, R.layout.item_shop_payment_details_item)
            bindExtra(BR.linkStyle, getLinkStyle())
        }
    }

    private fun getLinkStyle() =
        TextPaintModifiers.Builder()
            .withUnderlineText(true)
            .build()

    fun getFormattedRates(): String {
        return taxes?.let {
            taxes.taxesAmount.formattedPrice() + "\n" + taxes.shippingRate.price.formattedPrice()
        } ?: ""
    }
}
