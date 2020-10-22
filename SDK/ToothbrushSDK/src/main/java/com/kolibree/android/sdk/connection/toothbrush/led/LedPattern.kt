/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.led

import androidx.annotation.Keep

/**
 * Created by aurelien on 19/12/16.
 *
 * Toothbrush LED signal patterns
 */

@Keep
enum class LedPattern {
    /**
     * Fixed LED pattern
     */
    FIXED,

    /**
     * Sinus LED pattern
     */
    SINUS,

    /**
     * Short pulse LED pattern
     */
    SHORT_PULSE,

    /**
     * Long pulse LED pattern
     */
    LONG_PULSE
}
