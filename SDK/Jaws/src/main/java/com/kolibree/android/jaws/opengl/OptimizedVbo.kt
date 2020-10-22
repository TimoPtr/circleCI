/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl

import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.math.FloatVector
import com.kolibree.android.jaws.opengl.colors.ColorConverter
import java.nio.FloatBuffer

/** Optimized Vertex Buffer Object that ensures fast and smooth rendering in any case */
internal interface OptimizedVbo {

    /** Vertex coordinates [FloatBuffer] */
    val vertexBuffer: FloatBuffer

    /** Vertex normal vectors [FloatBuffer] */
    val normalBuffer: FloatBuffer

    /** Optimized materials thap map colors to 3D faces */
    val materials: Collection<OptimizedMaterial>

    /** Position vector in 3D space */
    val positionVector: FloatVector

    /** Rotation vector from 3D space point of view */
    val rotationVector: FloatVector

    /** Rotation vector from self point of view */
    val selfRotationVector: FloatVector

    /** Scaling vector */
    val scaleVector: FloatVector
}

/** Base [OptimizedVbo] implementation */
// Thread safety is ensured by the MemoryManager class
internal open class BaseOptimizedVbo<T>(
    override val vertexBuffer: FloatBuffer,
    override val normalBuffer: FloatBuffer,
    materialToFaceIndexMap: Map<T, Array<IntArray>>
) : OptimizedVbo {

    private val materialMap: Map<T, OptimizedMaterial> =
        materialToFaceIndexMap
            .mapValues { OptimizedMaterial(facesIndexRanges = it.value) }

    override val materials: Collection<OptimizedMaterial> = materialMap.values

    override val positionVector = FloatVector()

    override val rotationVector = FloatVector()

    override val selfRotationVector = FloatVector()

    override val scaleVector = FloatVector()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setAllMaterialsColor(@ColorInt color: Int) =
        setAllMaterialsColor(ColorConverter.toEglHdr(color))

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setAllMaterialsColor(color: FloatArray) =
        materialMap
            .values
            .forEach { it.color.copyValuesFrom(color) }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setMaterialColor(material: T, @ColorInt color: Int) =
        setMaterialColor(
            material,
            ColorConverter.toEglHdr(color)
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setMaterialColor(material: T, color: FloatArray) =
        materialMap[material]?.color?.copyValuesFrom(color)

    fun setMaterialColor(materialColors: Map<T, Int>) =
        materialColors
            .forEach {
                setMaterialColor(it.key, it.value)
            }
}

private fun FloatArray.copyValuesFrom(floatArray: FloatArray) =
    System.arraycopy(floatArray, 0, this, 0, floatArray.size)
