/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
enum class ShortTask(val internalValue: String) {
    MIND_YOUR_SPEED("ms"),
    TEST_YOUR_ANGLE("ta");

    companion object {
        fun fromInternalValue(internalValue: String): ShortTask? = when (internalValue) {
            MIND_YOUR_SPEED.internalValue -> MIND_YOUR_SPEED
            TEST_YOUR_ANGLE.internalValue -> TEST_YOUR_ANGLE
            else -> null
        }
    }
}
