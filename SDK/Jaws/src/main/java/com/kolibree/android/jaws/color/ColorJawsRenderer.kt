package com.kolibree.android.jaws.color

import android.os.SystemClock
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.Kolibree3DModel.LOWER_JAW
import com.kolibree.android.jaws.Kolibree3DModel.UPPER_JAW
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.base.JawsRenderer
import com.kolibree.android.jaws.models.LowerJawVbo
import com.kolibree.android.jaws.models.UpperJawVbo
import com.kolibree.android.jaws.opengl.BaseOptimizedRendererCompat
import com.kolibree.android.jaws.opengl.OptimizedVbo
import com.kolibree.android.jaws.tilt.JawsTiltController
import com.kolibree.kml.MouthZone16
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

@Keep
interface ColorJawsRenderer : JawsRenderer {

    fun openAnimation()

    fun closeAnimation()
}

internal class ColorJawsRendererImpl @Inject constructor(
    memoryManager: MemoryManagerInternal
) : BaseOptimizedRendererCompat(
    memoryManager = memoryManager,
    models = arrayOf(UPPER_JAW, LOWER_JAW)
), ColorJawsRenderer {

    private var mouthColors: ColorMouthZones = ColorMouthZones.white()
    private var cameraInitialized = AtomicBoolean(false)

    override fun openAnimation() {
        upperJawAnimator.animateTo(
            UPPER_JAW_OPEN_ROTATION,
            UPPER_JAW_OPEN_TRANSLATION_Y,
            UPPER_JAW_OPEN_TRANSLATION_Z
        )

        lowerJawAnimator.animateTo(
            LOWER_JAW_OPEN_ROTATION,
            LOWER_JAW_OPEN_TRANSLATION_Y,
            LOWER_JAW_OPEN_TRANSLATION_Z
        )
    }

    override fun closeAnimation() {
        upperJawAnimator.animateTo(
            UPPER_JAW_CLOSE_ROTATION,
            UPPER_JAW_CLOSE_TRANSLATION_Y,
            UPPER_JAW_CLOSE_TRANSLATION_Z
        )

        lowerJawAnimator.animateTo(
            LOWER_JAW_CLOSE_ROTATION,
            LOWER_JAW_CLOSE_TRANSLATION_Y,
            LOWER_JAW_CLOSE_TRANSLATION_Z
        )
    }

    @VisibleForTesting
    internal var upperJawAnimator = JawAnimator(
        UPPER_JAW_OPEN_ROTATION,
        UPPER_JAW_OPEN_TRANSLATION_Y,
        UPPER_JAW_OPEN_TRANSLATION_Z
    )

    @VisibleForTesting
    internal var lowerJawAnimator = JawAnimator(
        LOWER_JAW_OPEN_ROTATION,
        LOWER_JAW_OPEN_TRANSLATION_Y,
        LOWER_JAW_OPEN_TRANSLATION_Z
    )

    override fun prepareVbo(model: Kolibree3DModel, vbo: OptimizedVbo): Boolean {
        when (vbo) {
            is UpperJawVbo -> prepareUpperJawVbo(vbo)
            is LowerJawVbo -> prepareLowerJawVbo(vbo)
        }

        return true
    }

    override fun colorMouthZones(colors: ColorMouthZones) {
        this.mouthColors = colors
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)

        if (!cameraInitialized.getAndSet(true)) {
            moveCameraZ(CAMERA_POSITION_Z)
        }
    }

    override fun setTiltController(jawsTiltController: JawsTiltController) {
        // no-op
    }

    override fun lastMouthZones(): HashMap<MouthZone16, Int> {
        return hashMapOf()
    }

    private fun prepareUpperJawVbo(upperJawVbo: UpperJawVbo) =
        upperJawVbo.apply {
            rotationVector.x = 0f
            setMaterialColor(mouthColors.zonesColor)
            upperJawAnimator.update(this)
        }

    private fun prepareLowerJawVbo(lowerJawVbo: LowerJawVbo) =
        lowerJawVbo.apply {
            rotationVector.x = 0f
            setMaterialColor(mouthColors.zonesColor)
            lowerJawAnimator.update(this)
        }

    override fun pause() {
        // no-op
    }

    override fun resume() {
        // no-op
    }

    internal inner class JawAnimator(
        private var rotation: Float,
        private var positionY: Float,
        private var positionZ: Float
    ) {

        private var animationStartTime = 0L
        private var startRotation = 0f
        private var startPositionY = 0f
        private var startPositionZ = 0f
        private var stopRotation = 0f
        private var stopPositionY = 0f
        private var stopPositionZ = 0f

        init {
            startRotation = rotation
            stopRotation = rotation
            startPositionY = positionY
            stopPositionY = positionY
            startPositionZ = positionZ
            stopPositionZ = positionZ
        }

        private fun animationProgress(): Float {
            val elapsedTime = SystemClock.elapsedRealtime() - animationStartTime
            val progress = elapsedTime / ANIMATION_DURATION
            return min(1f, progress)
        }

        fun animateTo(
            destinationRotation: Float,
            destinationPositionY: Float,
            destinationPositionZ: Float
        ) {
            startRotation = rotation
            startPositionY = positionY
            startPositionZ = positionZ
            stopRotation = destinationRotation
            stopPositionY = destinationPositionY
            stopPositionZ = destinationPositionZ

            animationStartTime = SystemClock.elapsedRealtime()
        }

        private fun calcValue(start: Float, stop: Float, progress: Float): Float {
            val max = max(start, stop)
            val min = min(start, stop)
            val distance = max - min
            val progressDistance = distance * progress
            return if (stop > start) start + progressDistance else start - progressDistance
        }

        fun update(vbo: OptimizedVbo) {
            val progress = animationProgress()
            rotation = calcValue(startRotation, stopRotation, progress)
            positionY = calcValue(startPositionY, stopPositionY, progress)
            positionZ = calcValue(startPositionZ, stopPositionZ, progress)

            vbo.positionVector.set(0f, positionY, positionZ)
            vbo.selfRotationVector.x = rotation
        }
    }

    companion object {

        @VisibleForTesting
        const val ANIMATION_DURATION = 500f

        @VisibleForTesting
        const val UPPER_JAW_OPEN_ROTATION = 340f

        @VisibleForTesting
        const val UPPER_JAW_OPEN_TRANSLATION_Y = 11f

        @VisibleForTesting
        const val UPPER_JAW_OPEN_TRANSLATION_Z = 0f

        @VisibleForTesting
        const val UPPER_JAW_CLOSE_ROTATION = 375f

        @VisibleForTesting
        const val UPPER_JAW_CLOSE_TRANSLATION_Y = 7f

        @VisibleForTesting
        const val UPPER_JAW_CLOSE_TRANSLATION_Z = 4f

        @VisibleForTesting
        const val LOWER_JAW_OPEN_ROTATION = 385f

        @VisibleForTesting
        const val LOWER_JAW_OPEN_TRANSLATION_Y = -14f

        @VisibleForTesting
        const val LOWER_JAW_OPEN_TRANSLATION_Z = 0f

        @VisibleForTesting
        const val LOWER_JAW_CLOSE_ROTATION = 345f

        @VisibleForTesting
        const val LOWER_JAW_CLOSE_TRANSLATION_Y = -9f

        @VisibleForTesting
        const val LOWER_JAW_CLOSE_TRANSLATION_Z = 5f

        @VisibleForTesting
        const val CAMERA_POSITION_Z = -80f
    }
}
