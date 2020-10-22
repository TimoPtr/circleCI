package com.kolibree.sdkws.data.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Immutable class that contains the data to update an Account  */
@Keep
data class CreateEmailAccountData(
    @SerializedName("email") val email: String,
    @SerializedName("appid") val appid: String
)
