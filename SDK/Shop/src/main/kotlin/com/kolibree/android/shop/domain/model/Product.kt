/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val productId: String,
    val variantId: String,
    val productTitle: String,
    val variantTitle: String,
    val productType: String,
    val description: String,
    val htmlDescription: String,
    val price: Price,
    val productImages: List<String>,
    private val variantImage: String?,
    val sku: String
) : Parcelable {

    @IgnoredOnParcel
    val mainImage: String? = when {
        variantImage != null -> variantImage
        productImages.isNotEmpty() -> productImages[0]
        else -> null
    }
}
