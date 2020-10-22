/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.data.model

import com.google.gson.annotations.SerializedName
import com.kolibree.android.commons.models.StrippedMac

internal data class SendBatteryLevelRequest(
    @SerializedName("mac_address") val macAddress: StrippedMac,
    @SerializedName("serial_number") val serialNumber: String,
    @SerializedName("discrete_level") val discreteLevel: Int
)
