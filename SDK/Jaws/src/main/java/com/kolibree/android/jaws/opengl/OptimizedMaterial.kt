/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl

/** Optimized material that directly maps a color to the corresponding triangles (faces) indexes */
internal data class OptimizedMaterial(
    val color: FloatArray = floatArrayOf(1f, 1f, 1f, 1f), // HDR 16 bytes, default opaque white
    val facesIndexRanges: Array<IntArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptimizedMaterial

        if (!color.contentEquals(other.color)) return false
        if (!facesIndexRanges.contentDeepEquals(other.facesIndexRanges)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color.contentHashCode()
        result = 31 * result + facesIndexRanges.contentDeepHashCode()
        return result
    }
}
