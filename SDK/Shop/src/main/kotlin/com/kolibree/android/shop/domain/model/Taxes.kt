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
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class Taxes(
    val taxesAmount: Price,
    val shippingRate: ShippingRate
) : Parcelable {

    @IgnoredOnParcel
    val total: Price = taxesAmount + shippingRate.price
}

@VisibleForApp
@Parcelize
data class ShippingRate(
    val price: Price,
    val handle: String? = null
) : Parcelable
