/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updates

import androidx.annotation.IntDef
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.version.SoftwareVersion

/** OTA update base interface  */
internal interface OtaUpdate {

    /**
     * Get the expected CRC32 of the file
     *
     * @return long CRC32 value
     */
    val crc: Long

    /**
     * Get the firmware file content
     *
     * @return non null byte array
     */
    val data: ByteArray

    /**
     * Get the update version
     *
     * @return non null [SoftwareVersion]
     */
    val version: SoftwareVersion

    /**
     * Get update type (TYPE_FIRMWARE or TYPE_GRU_DATA)
     *
     * @return type
     */
    @get:OtaType
    val type: Int

    /**
     * Check if a toothbrush firmware version is compatible with this update
     *
     * @param version non null toothbrush firmware SoftwareVersion
     * @return true if this update can be installed on the device, false otherwise (brick risk)
     */
    fun isCompatible(version: SoftwareVersion): Boolean

    /**
     * Check if a toothbrush model is compatible with this update
     *
     * @param model non null ToothbrushModel
     * @return true if this update can be installed on the device, false otherwise (brick risk)
     */
    fun isCompatible(model: ToothbrushModel): Boolean

    /** Check file integrity  */
    @Throws(Exception::class)
    fun checkCRC()

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_FIRMWARE, TYPE_GRU_DATA)
    annotation class OtaType

    companion object {

        /** Firmware update file  */
        const val TYPE_FIRMWARE = 1

        /** GRU data update file  */
        const val TYPE_GRU_DATA = 2
    }
}
