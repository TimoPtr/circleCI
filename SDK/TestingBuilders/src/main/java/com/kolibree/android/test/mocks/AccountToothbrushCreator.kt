/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_BOOTLOADER_VERSION
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_FW_VERSION
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_HW_VERSION
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MODEL
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_NAME
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_SERIAL

@Keep
fun createAccountToothbrush(
    mac: String = DEFAULT_MAC,
    name: String = DEFAULT_NAME,
    serial: String = DEFAULT_SERIAL,
    model: ToothbrushModel = DEFAULT_MODEL,
    firmwareVersion: SoftwareVersion = DEFAULT_FW_VERSION,
    bootloaderVersion: SoftwareVersion = DEFAULT_BOOTLOADER_VERSION,
    hardwareVersion: HardwareVersion = DEFAULT_HW_VERSION,
    dspVersion: DspVersion = DspVersion.NULL,
    accountId: Long = DEFAULT_TEST_ACCOUNT_ID,
    profileId: Long = ProfileBuilder.DEFAULT_ID,
    dirty: Boolean = false
): AccountToothbrush {
    return AccountToothbrush(
        mac = mac,
        name = name,
        serial = serial,
        model = model,
        firmwareVersion = firmwareVersion,
        bootloaderVersion = bootloaderVersion,
        hardwareVersion = hardwareVersion,
        dspVersion = dspVersion,
        accountId = accountId,
        profileId = profileId,
        dirty = dirty
    )
}
