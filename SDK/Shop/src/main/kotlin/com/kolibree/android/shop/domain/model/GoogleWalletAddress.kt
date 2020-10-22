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

/**
 * @see https://developers.google.com/pay/api/android/reference/response-objects#Address
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
@Parcelize
data class GoogleWalletAddress(
    val address1: String,
    val address2: String,
    val address3: String,
    val sortingCode: String,
    val countryCode: String,
    val postalCode: String,
    val name: String,
    val locality: String,
    val administrativeArea: String
) : Parcelable
