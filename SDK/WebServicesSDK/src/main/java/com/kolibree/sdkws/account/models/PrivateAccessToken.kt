package com.kolibree.sdkws.account.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Guillaume Agis on 15/10/2018.
 */
@Keep
data class PrivateAccessToken(
    @field:SerializedName("access_token") val accessToken: String,
    @field:SerializedName("token_expires") val expiryDate: String
)
