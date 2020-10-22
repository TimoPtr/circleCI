package com.kolibree.android.network.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LogoutBody(
    @field:SerializedName("refresh_token") val refreshToken: String,
    @field:SerializedName("access_token") val accessToken: String
)
