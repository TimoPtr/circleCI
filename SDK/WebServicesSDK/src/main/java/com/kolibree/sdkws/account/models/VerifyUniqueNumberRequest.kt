/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.models

import com.google.gson.annotations.SerializedName

internal data class VerifyUniqueNumberRequest(
    @field:SerializedName("phone_number") val phoneNumber: String,
    @field:SerializedName("verification_token") val verificationToken: String,
    @field:SerializedName("verification_code") val verificationCode: String
)
