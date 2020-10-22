package com.kolibree.android.network.core.useragent

import com.kolibree.android.network.retrofit.DeviceParameters
import javax.inject.Inject

/** Created by lookash on 30/12/2018. */
internal class UserAgentHeaderProviderImpl
@Inject constructor(
    private val deviceParameters: DeviceParameters
) : UserAgentHeaderProvider() {

    override val userAgentValue: String
        get() = "Dalvik/${deviceParameters.appVersion} (" +
            "Linux; " +
            "U; " +
            "${deviceParameters.buildNumber}; " +
            "${deviceParameters.deviceManufacturer} ${deviceParameters.deviceModel}; " +
            "Android ${deviceParameters.osVersion}; " +
            "API ${deviceParameters.osApiLevel}" +
            ")"
}
