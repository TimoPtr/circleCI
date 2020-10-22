/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.core.capabilities

import javax.inject.Inject

internal class AcceptCapabilitiesHeaderProviderImpl
@Inject constructor() : AcceptCapabilitiesHeaderProvider() {

    override val capabilityValues: List<String> = listOf(
        CAPABILITY_ANDROID,
        CAPABILITY_BOOTLOADER_OTA
    )
}

private const val CAPABILITY_ANDROID = "device-android"
private const val CAPABILITY_BOOTLOADER_OTA = "bootloader-ota"
