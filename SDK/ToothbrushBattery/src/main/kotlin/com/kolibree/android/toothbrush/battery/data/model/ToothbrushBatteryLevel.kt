/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.data.model

import com.kolibree.android.toothbrush.battery.domain.BatteryLevel

internal data class ToothbrushBatteryLevel(
    val macAddress: String,
    val serialNumber: String,
    val batteryLevel: BatteryLevel
)
