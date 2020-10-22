package com.kolibree.sdkws.api.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VerificationTokenResponse(
    @field:SerializedName("verification_token") val verificationToken: String
)
