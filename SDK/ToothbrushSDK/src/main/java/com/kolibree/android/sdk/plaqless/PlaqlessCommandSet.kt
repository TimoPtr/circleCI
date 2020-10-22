/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

internal const val CONTROL_DETECTOR_PAYLOAD_POS: Int = 0
internal const val CONTROL_RAW_DATA_PAYLOAD_POS: Int = 1

internal fun generateControlPayload(plaqlessDataEnable: Boolean, rawDataEnable: Boolean): Byte {
    var output = 0
    if (plaqlessDataEnable) {
        output = output or (1 shl CONTROL_DETECTOR_PAYLOAD_POS)
    }

    if (rawDataEnable) {
        output = output or (1 shl CONTROL_RAW_DATA_PAYLOAD_POS)
    }

    return output.toByte()
}
