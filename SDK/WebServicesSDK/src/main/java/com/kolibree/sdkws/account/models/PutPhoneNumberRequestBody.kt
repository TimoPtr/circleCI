/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PutPhoneNumberRequestBody(
    @field:SerializedName("verification_token") val verificationToken: String,
    @field:SerializedName("verification_code") val verificationCode: Int,
    @field:SerializedName("phone_number") val phoneNumber: String
)
