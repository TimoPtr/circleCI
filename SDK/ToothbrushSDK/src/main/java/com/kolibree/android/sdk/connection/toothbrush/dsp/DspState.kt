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
import com.kolibree.android.sdk.version.DspVersion

/** Toothbrush DSP information and data */
@Keep
data class DspState(
    val hasValidFirmware: Boolean,
    val usesDeprecatedFirmwareFormat: Boolean,
    val firmwareVersion: DspVersion,
    val flashFileType: DspFlashFileType,
    val flashFileVersion: DspVersion,
    val bootloaderVersion: Int
)
