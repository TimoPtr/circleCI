/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.data.model

import com.google.gson.annotations.SerializedName
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
data class AmazonDashSendTokenRequest(
    @SerializedName("amazon_login_code") val token: String
)
