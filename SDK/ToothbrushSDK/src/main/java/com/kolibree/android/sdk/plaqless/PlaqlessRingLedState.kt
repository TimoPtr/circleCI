/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import androidx.annotation.Keep
import com.kolibree.android.sdk.core.binary.PayloadReader

/**
 * While using this class we should check first the white value if it's different than 0 we should ignore
 * the other values
 *
 *
 * @param red
 * @param green
 * @param blue
 * @param white
 */
@Keep
data class PlaqlessRingLedState constructor(
    val red: Short,
    val green: Short,
    val blue: Short,
    val white: Short
) {

    companion object {

        @JvmStatic
        fun create(reader: PayloadReader) =
            PlaqlessRingLedState(
                reader.readUnsignedInt8(),
                reader.readUnsignedInt8(),
                reader.readUnsignedInt8(),
                reader.readUnsignedInt8()
            )
    }
}
