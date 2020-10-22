/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Taxes
import com.kolibree.android.shop.presentation.ShopItemBindingModel
import com.kolibree.android.shop.presentation.list.ShopProductBindingModel
import kotlin.math.min
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ShopCartViewState(
    val cartProducts: List<ShopProductBindingModel> = emptyList(),
    val paymentDetails: ShopPaymentDetailsBindingModel = ShopPaymentDetailsBindingModel(),
    val cartResult: CartProductsResult = CartProductsResult.Loading,
    val useSmiles: Boolean = false,
    val areButtonsClickable: Boolean = true,
    val availableSmiles: Int = 0,
    val isGooglePayButtonVisible: Boolean,
    val estimatedTaxes: Taxes? = null,
    val voucherApplied: Boolean? = null
) : BaseViewState {

    companion object {
        fun initial(isGooglePayButtonVisible: Boolean) =
            ShopCartViewState(isGooglePayButtonVisible = isGooglePayButtonVisible)
    }

    fun withAvailableSmiles(smiles: Int): ShopCartViewState =
        copy(availableSmiles = smiles, useSmiles = smiles > 0)

    fun withAvailableRates(taxes: Taxes): ShopCartViewState {
        return currentCurrency?.let {
            copy(estimatedTaxes = taxes)
        } ?: this
    }

    fun subTotalPrice(): Price? {
        if (cartProducts.isEmpty()) return null

        var totalPrice = Price.empty(currentCurrency!!)
        for (cartProduct in cartProducts) {
            totalPrice += cartProduct.product.price * cartProduct.quantity
        }
        return totalPrice
    }

    fun totalPrice(): Price? =
        subTotalPrice()?.let { subTotalPrice ->
            val currency = subTotalPrice.currency

            subTotalPrice -
                (actualDiscount?.let { actualDiscount } ?: Price.empty(currency)) +
                (estimatedTaxes?.let { estimatedTaxes.total } ?: Price.empty(currency))
        }

    @IgnoredOnParcel
    val cartItems: List<ShopItemBindingModel> = when {
        cartProducts.isEmpty() -> emptyList()
        else -> cartProducts + paymentDetails
    }

    @IgnoredOnParcel
    val currentCurrency = cartProducts.firstOrNull()?.product?.price?.currency

    @IgnoredOnParcel
    val isPossibleToUseSmiles = availableSmiles > 0

    @IgnoredOnParcel
    val potentialSmilesUsed = subTotalPrice()?.let { subTotalPrice ->
        min(availableSmiles, subTotalPrice.smilePoints)
    }

    @IgnoredOnParcel
    val potentialDiscountPrice = currentCurrency?.let { currency ->
        potentialSmilesUsed?.let {
            Price.createFromSmiles(potentialSmilesUsed, currency)
        }
    }

    @IgnoredOnParcel
    val actualDiscount = if (useSmiles) potentialDiscountPrice else emptyPrice()

    @VisibleForTesting
    fun emptyPrice(): Price? = currentCurrency?.let {
        Price.empty(currentCurrency)
    }

    fun withVoucherApplied(voucherApplied: Boolean): ShopCartViewState =
        copy(voucherApplied = voucherApplied)
}

internal enum class CartProductsResult {
    Loading,
    CartProductsAvailable,
    EmptyCart
}
