/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.push

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755030/Device+Token
 */
@Keep
data class PushNotificationTokenRequestBody(
    @SerializedName("registration_id") val token: String,
    @SerializedName("device_id") val deviceId: String
) {
    @SerializedName("active")
    val active: Boolean = true
    @SerializedName("device_type")
    val deviceType = 1 // 0 for ios, 1 for android, 2 for web
    @SerializedName("name")
    val name: String? = null
}
