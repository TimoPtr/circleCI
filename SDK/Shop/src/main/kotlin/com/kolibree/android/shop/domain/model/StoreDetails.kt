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
import java.util.Currency
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoreDetails(
    val name: String,
    val description: String,
    val countryCode: String,
    val currency: Currency,
    val supportedDigitalWallets: List<String>,
    val acceptedCardBrands: List<String>
) : Parcelable
