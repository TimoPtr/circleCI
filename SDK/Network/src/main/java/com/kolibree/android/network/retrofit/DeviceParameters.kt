package com.kolibree.android.network.retrofit

import android.os.Build
import android.util.Base64
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Keep
data class DeviceParameters(
    @field:SerializedName("app_version") val appVersion: String,
    @field:SerializedName("os_device") val osDevice: String = "ANDROID",
    @field:SerializedName("os_version") val osVersion: String = Build.VERSION.RELEASE,
    @field:SerializedName("device_model") val deviceModel: String = Build.MODEL,
    @field:SerializedName("device_manufacturer") val deviceManufacturer: String = Build.MANUFACTURER,
    @Transient val buildNumber: Int, // TODO serialize this field if it's needed
    @Transient val osApiLevel: Int = Build.VERSION.SDK_INT // TODO serialize this field if it's needed
) {
    fun encrypt(): String =
        Base64.encodeToString(Gson().toJson(this).trim().toByteArray(), Base64.NO_WRAP)

    @Keep
    companion object {
        fun create(appVersion: String, buildNumber: Int) =
            DeviceParameters(appVersion = appVersion, buildNumber = buildNumber)
    }
}
