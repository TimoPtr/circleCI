/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.persitence.model

import androidx.annotation.Keep
import androidx.room.Entity
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.QuantityProduct
import timber.log.Timber

@Keep
@Entity(tableName = "cart_entries", primaryKeys = ["profileId", "productId", "variantId"])
internal data class CartEntryEntity(
    val profileId: Long,
    val productId: String,
    val variantId: String,
    val quantity: Int
) {
    fun toQuantityProduct(product: Product): QuantityProduct? {
        FailEarly.failInConditionMet(productId != product.productId, "productId is wrong")
        FailEarly.failInConditionMet(variantId != product.variantId, "variantId is wrong")

        return if (productId != product.productId || variantId != product.variantId)
            null
        else QuantityProduct(quantity, product)
    }

    fun decreaseQuantity(): CartEntryEntity {
        val newQuantity = quantity - 1
        return if (newQuantity < 0) {
            Timber.w("Quantity cannot be negative")
            this
        } else {
            CartEntryEntity(profileId, productId, variantId, newQuantity)
        }
    }

    fun increaseQuantity(): CartEntryEntity = CartEntryEntity(profileId, productId, variantId, quantity + 1)

    internal companion object {

        fun Product.createEntity(profileId: Long, quantity: Int = 0): CartEntryEntity =
            CartEntryEntity(profileId, productId, variantId, quantity)
    }
}
