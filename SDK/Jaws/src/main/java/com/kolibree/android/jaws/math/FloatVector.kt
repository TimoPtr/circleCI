/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.math

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting

/** [Float] vector definition */
@Keep
data class FloatVector(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {

    fun set(source: FloatArray) {
        set(
            source[X_AXIS],
            source[Y_AXIS],
            source[Z_AXIS]
        )
    }

    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun offsetX(offset: Float) {
        x += offset
    }

    fun offsetY(offset: Float) {
        y += offset
    }

    fun offsetZ(offset: Float) {
        z += offset
    }

    companion object {

        @VisibleForTesting
        internal const val X_AXIS = 0

        @VisibleForTesting
        internal const val Y_AXIS = 1

        @VisibleForTesting
        internal const val Z_AXIS = 2
    }
}
