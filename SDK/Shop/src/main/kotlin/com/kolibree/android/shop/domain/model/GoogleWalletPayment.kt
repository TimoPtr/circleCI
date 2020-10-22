/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class GoogleWalletPayment(
    val token: String,
    val amount: Price,
    val billingAddress: GoogleWalletAddress,
    val shippingAddress: GoogleWalletAddress,
    val isProductionPayment: Boolean
) : Parcelable
