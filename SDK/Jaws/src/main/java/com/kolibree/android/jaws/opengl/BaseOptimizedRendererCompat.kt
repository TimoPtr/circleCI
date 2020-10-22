/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl

import android.graphics.Color
import android.opengl.Matrix
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManagerInternal
import io.reactivex.functions.Consumer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Base [OptimizedRenderer] implementation */
internal abstract class BaseOptimizedRendererCompat(
    private val memoryManager: MemoryManagerInternal,
    private val models: Array<Kolibree3DModel>,
    private val openGles20Wrapper: OpenGles20Wrapper = OpenGles20Wrapper.createInstance()
) : OptimizedRenderer {

    // Drawers cache
    private val drawersCache = HashMap<Kolibree3DModel, Object3DImpl>()

    // Camera helper
    private val camera = Camera()

    // Model projection matrix
    private val mpMatrix = FloatArray(size = MATRIX_LENGTH_IN_FLOATS)

    // Model view matrix
    private val mvMatrix = FloatArray(size = MATRIX_LENGTH_IN_FLOATS)

    // Model view projection Matrix
    private val mvpMatrix = FloatArray(size = MATRIX_LENGTH_IN_FLOATS)

    // Data binding makes the setBackgroundColor method being called before the Surface is fully
    // initialized, so we also store the color in order to re apply it when the surface changes
    @ColorInt private var backgroundColorInt: Int = Color.WHITE

    final override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        openGles20Wrapper.apply {
            enableFaceCulling()
            enableDepthTesting()
            enableAlphaLayer()
        }
        initDrawersCache()
    }

    final override fun onDrawFrame(gl: GL10?) {
        openGles20Wrapper.clearSurface()
        animateCameraAndUpdateMatrices()
        prepareVbosAndDraw()
    }

    @CallSuper
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        openGles20Wrapper.adjustViewport(0, 0, width, height)
        openGles20Wrapper.setClearColor(backgroundColorInt)
        updateModelViewMatrix()
        initModelProjectionMatrix(width, height)
        updateModelViewProjectionMatrix()
    }

    final override fun setEglBackgroundColor(@ColorInt color: Int) {
        openGles20Wrapper.setClearColor(color)
        backgroundColorInt = color
    }

    private fun animateCameraAndUpdateMatrices() =
        camera.apply {
            animate()
            if (hasChanged()) {
                updateModelViewMatrix()
                updateModelViewProjectionMatrix()
                setChanged(false)
            }
        }

    private fun updateModelViewMatrix() =
        Matrix.setLookAtM(
            mvMatrix, 0,
            camera.xPos, camera.yPos, camera.zPos,
            camera.xView, camera.yView, camera.zView,
            camera.xUp, camera.yUp, camera.zUp
        )

    private fun updateModelViewProjectionMatrix() =
        Matrix.multiplyMM(mvpMatrix, 0, mpMatrix, 0, mvMatrix, 0)

    private fun initModelProjectionMatrix(viewportWidth: Int, viewportHeight: Int) =
        Matrix.perspectiveM(
            mpMatrix,
            0,
            CAMERA_FIELD_OF_VIEW,
            viewportWidth.toFloat() / viewportHeight,
            CAMERA_CLOSEST_VISIBLE_Z,
            CAMERA_FURTHEST_VISIBLE_Z
        )

    private fun initDrawersCache() =
        models.forEach {
            drawersCache[it] = Object3DImpl(it.vertexShaderCode)
        }

    private fun prepareVbosAndDraw() =
        models.forEach { model ->
            memoryManager.lockAndUse(model, Consumer { vbo ->
                if (prepareVbo(model, vbo)) {
                    drawersCache[model]?.draw(vbo, mpMatrix, mvMatrix)
                }
            })
        }

    protected fun moveCameraZ(z: Float) = camera.moveCameraZ(z)

    protected fun translateCamera(dX: Float, dY: Float) = camera.translateCamera(dX, dY)

    /**
     * Implement this method to set up the vbo before it is drawn
     *
     * @param model [Kolibree3DModel]
     * @param vbo [OptimizedVbo]
     * @return true if the object should be drawn, false otherwise
     */
    protected abstract fun prepareVbo(model: Kolibree3DModel, vbo: OptimizedVbo): Boolean

    companion object {

        @VisibleForTesting
        internal const val CAMERA_FIELD_OF_VIEW = 38f

        @VisibleForTesting
        internal const val CAMERA_CLOSEST_VISIBLE_Z = 1f

        @VisibleForTesting
        internal const val CAMERA_FURTHEST_VISIBLE_Z = 150f

        @VisibleForTesting
        internal const val MATRIX_LENGTH_IN_FLOATS = 16
    }
}
