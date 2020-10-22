/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.utils

import android.content.Context
import android.provider.Settings
import com.kolibree.android.annotation.VisibleForApp

/** Espresso utilities */
@VisibleForApp
object EspressoUtils {

    /** Check if system animations are enabled (they are not when running Espresso) */
    fun areSystemAnimationsEnabled(context: Context): Boolean {
        val duration = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transition = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )

        return duration != 0f && transition != 0f
    }
}
