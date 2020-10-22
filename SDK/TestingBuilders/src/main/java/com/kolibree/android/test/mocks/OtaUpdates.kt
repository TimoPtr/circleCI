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
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.sdk.version.SoftwareVersion

@Keep
object OtaUpdates {

    @JvmStatic
    fun createBootloaderUpdate(version: String = DEFAULT_VERSION) =
        createAvailableUpdate(type = UpdateType.TYPE_BOOTLOADER, version = version)

    @JvmStatic
    fun createDspUpdate(version: String = DEFAULT_VERSION) =
        createAvailableUpdate(type = UpdateType.TYPE_DSP, version = version)

    @JvmOverloads
    @JvmStatic
    fun createFirmwareUpdate(version: String = DEFAULT_VERSION) =
        createAvailableUpdate(type = UpdateType.TYPE_FIRMWARE, version = version)

    @JvmOverloads
    @JvmStatic
    fun createGruUpdate(version: String = DEFAULT_VERSION) =
        createAvailableUpdate(type = UpdateType.TYPE_GRU, version = version)

    @JvmStatic
    fun createAvailableUpdate(type: UpdateType, version: String): AvailableUpdate {
        return AvailableUpdate.create(version, PATH, type, null)
    }

    @JvmStatic
    fun defaultSoftwareVersion() = SoftwareVersion(DEFAULT_VERSION)

    @JvmStatic
    fun higherSoftwareVersion() = SoftwareVersion(HIGHER_VERSION)

    @JvmStatic
    fun lowerSoftwareVersion() = SoftwareVersion(LOWER_VERSION)
}

const val PATH = "none"
const val DEFAULT_VERSION = "1.2.3"
const val LOWER_VERSION = "1.2.2"
const val HIGHER_VERSION = "1.2.4"
