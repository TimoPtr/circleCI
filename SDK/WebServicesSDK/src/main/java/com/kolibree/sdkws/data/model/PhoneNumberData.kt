package com.kolibree.sdkws.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PhoneNumberData(
    @field:SerializedName("phone_number") val phoneNumber: String,
    @field:SerializedName("verification_token") val verificationToken: String,
    @field:SerializedName("verification_code") val verificationCode: String
)
