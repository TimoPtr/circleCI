/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.base

import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.Kolibree3DModel.HUM_LOWER_JAW
import com.kolibree.android.jaws.Kolibree3DModel.HUM_UPPER_JAW
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.jaws.models.BaseJawVbo
import com.kolibree.android.jaws.models.HumLowerJawVbo
import com.kolibree.android.jaws.models.HumUpperJawVbo
import com.kolibree.android.jaws.models.LowerJawVbo
import com.kolibree.android.jaws.models.UpperJawVbo
import com.kolibree.android.jaws.opengl.BaseOptimizedRenderer
import com.kolibree.android.jaws.opengl.OptimizedVbo
import com.kolibree.android.jaws.tilt.JawsTiltController
import com.kolibree.kml.MouthZone16
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.opengles.GL10

/** Base [BaseOptimizedRenderer] implementation for upper and lower jaws rendering */
internal open class BaseJawsRenderer constructor(
    memoryManager: MemoryManagerInternal,
    upperJawModel: Kolibree3DModel = HUM_UPPER_JAW,
    lowerJawModel: Kolibree3DModel = HUM_LOWER_JAW
) : BaseOptimizedRenderer(
    memoryManager = memoryManager,
    models = arrayOf(upperJawModel, lowerJawModel)
), JawsRenderer {

    private val cameraInitialized = AtomicBoolean(false)

    protected val zoneColors = HashMap<MouthZone16, Int>(MouthZone16.values().size)

    private val tiltController = AtomicReference<JawsTiltController?>(null)

    protected open val cameraZ = CAMERA_POSITION_Z

    final override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)

        if (!cameraInitialized.getAndSet(true)) {
            moveCameraZ(cameraZ)
        }
    }

    final override fun prepareVbo(model: Kolibree3DModel, vbo: OptimizedVbo): Boolean {
        vbo as BaseJawVbo
        vbo.apply {
            synchronized(zoneColors) {
                setMouthZoneColors(zoneColors)
            }
            tiltController.get()?.let { tilt ->
                rotationVector.x = tilt.getJawsRotationX()
                rotationVector.y = tilt.getJawsRotationY()
                positionVector.x = tilt.getTranslationX()
            }
        }

        prepareBaseJawVbo(vbo)

        return true
    }

    private fun prepareBaseJawVbo(vbo: BaseJawVbo) = when (vbo) {
        is UpperJawVbo -> prepareUpperJawVbo(vbo)
        is LowerJawVbo -> prepareLowerJawVbo(vbo)
        is HumUpperJawVbo -> prepareHumUpperJawVbo(vbo)
        is HumLowerJawVbo -> prepareHumLowerJawVbo(vbo)
        else -> throw IllegalStateException("Unknown VBO")
    }

    final override fun colorMouthZones(colors: ColorMouthZones) {
        synchronized(zoneColors) {
            zoneColors.apply {
                clear()
                putAll(colors.zonesColor)
            }
        }
    }

    final override fun setTiltController(jawsTiltController: JawsTiltController) {
        tiltController.set(jawsTiltController)
    }

    final override fun lastMouthZones() = zoneColors

    @CallSuper
    protected open fun prepareUpperJawVbo(upperJawVbo: UpperJawVbo) {
        // no-op
    }

    @CallSuper
    protected open fun prepareLowerJawVbo(lowerJawVbo: LowerJawVbo) {
        // no-op
    }

    @CallSuper
    protected open fun prepareHumUpperJawVbo(upperJawVbo: HumUpperJawVbo) {
        // no-op
    }

    @CallSuper
    protected open fun prepareHumLowerJawVbo(lowerJawVbo: HumLowerJawVbo) {
        // no-op
    }

    @CallSuper
    override fun pause() {
        tiltController.get()?.onPause()
    }

    @CallSuper
    override fun resume() {
        tiltController.get()?.onResume()
    }

    companion object {

        @VisibleForTesting
        const val CAMERA_POSITION_Z = -80f
    }
}
