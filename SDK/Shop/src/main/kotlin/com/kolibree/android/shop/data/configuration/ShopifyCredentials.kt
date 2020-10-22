/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.configuration

import androidx.annotation.Keep

@Keep
data class ShopifyCredentials(
    val shopDomain: String,
    val shopApiKey: String
)
