/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.dsp

import androidx.annotation.Keep

/** DSP flash memory file type */
@Keep
enum class DspFlashFileType {

    /** There is no valid and pushable file on the DSP flash memory */
    NO_FLASH_FILE,

    /** There is a valid and pushable firmware update for the DSP in the flash memory */
    FIRMWARE_FLASH_FILE,

    /** There is a valid and pushable bootloader update for the DSP in the flash memory */
    BOOTLOADER_FLASH_FILE,

    /** There is a valid and pushable DSP configuration file in the flash memory */
    CONFIGURATION_FLASH_FILE
}
