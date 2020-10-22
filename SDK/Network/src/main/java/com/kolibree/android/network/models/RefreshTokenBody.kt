package com.kolibree.android.network.models

import com.google.gson.annotations.SerializedName

internal data class RefreshTokenBody(
    @field:SerializedName("refresh_token") val refreshToken: String,
    @field:SerializedName("email") val email: String? = null,
    @field:SerializedName("phone_number") val phoneNumber: String? = null,
    @field:SerializedName("appid") val appId: String? = null,
    @field:SerializedName("account") val account: Boolean = false,
    @field:SerializedName("duration") val duration: Long? = null, // to be used only for testing purposes
    @field:SerializedName("test") val test: Boolean = false
) {
    init {
        if (email != null && phoneNumber != null) {
            throw IllegalArgumentException(
                "email (%s) and phoneNumber (%s) can't be present at the same time".format(
                    email,
                    phoneNumber
                )
            )
        }

        if (email == null && phoneNumber == null && appId == null) {
            throw IllegalArgumentException("email, phoneNumber or appId must be not null")
        }
    }
}
