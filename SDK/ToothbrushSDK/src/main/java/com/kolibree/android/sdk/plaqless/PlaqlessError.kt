/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.plaqless.PlaqlessError.NONE
import com.kolibree.android.sdk.plaqless.PlaqlessError.OUT_OF_MOUTH
import com.kolibree.android.sdk.plaqless.PlaqlessError.REPLACE_BRUSH_HEAD
import com.kolibree.android.sdk.plaqless.PlaqlessError.RINSE_BRUSH_HEAD
import com.kolibree.android.sdk.plaqless.PlaqlessError.UNKNOWN
import com.kolibree.android.sdk.plaqless.PlaqlessError.WRONG_HANDLE

/**
 * Error codes emitted by BLE_GATT_PLAQLESS_DETECTOR_CHAR
 *
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755082/BLE+protocol+for+Plaqless+data+in+online+mode
 */
enum class PlaqlessError(val code: Byte) {
    NONE(NONE_CODE),
    OUT_OF_MOUTH(OUT_OF_MOUTH_CODE),
    WRONG_HANDLE(WRONG_HANDLE_CODE),
    RINSE_BRUSH_HEAD(RINSE_BRUSH_HEAD_CODE),
    REPLACE_BRUSH_HEAD(REPLACE_BRUSH_HEAD_CODE), // Hiding the camera
    UNKNOWN(UNKNOWN_CODE);
}

internal fun fromErrorCode(errorCode: Byte): PlaqlessError {
    // .toByte() is evaluated at compile time, so there's no cost of conversion at runtime
    return when (errorCode) {
        NONE_CODE -> NONE
        OUT_OF_MOUTH_CODE -> OUT_OF_MOUTH
        WRONG_HANDLE_CODE -> WRONG_HANDLE
        RINSE_BRUSH_HEAD_CODE -> RINSE_BRUSH_HEAD
        REPLACE_BRUSH_HEAD_CODE -> REPLACE_BRUSH_HEAD
        else -> UNKNOWN
    }
}

@VisibleForTesting
internal const val UNKNOWN_CODE = 0xff.toByte()

@VisibleForTesting
internal const val NONE_CODE = 0.toByte()

@VisibleForTesting
internal const val OUT_OF_MOUTH_CODE = 10.toByte()

@VisibleForTesting
internal const val WRONG_HANDLE_CODE = 11.toByte()

@VisibleForTesting
internal const val RINSE_BRUSH_HEAD_CODE = 81.toByte()

@VisibleForTesting
internal const val REPLACE_BRUSH_HEAD_CODE = 82.toByte()
