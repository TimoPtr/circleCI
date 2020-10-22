/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.renderer

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.coach.CoachFacingAngleMapper
import com.kolibree.android.jaws.coach.animation.Animator
import com.kolibree.android.jaws.coach.animation.BrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.brushhead.BrushHeadPositionMapper
import com.kolibree.android.jaws.models.BaseJawVbo
import com.kolibree.android.jaws.models.DefaultBrushHeadVbo
import com.kolibree.android.jaws.models.LowerJawVbo
import com.kolibree.android.jaws.models.PlaqlessBrushHeadVbo
import com.kolibree.android.jaws.models.UpperJawVbo
import com.kolibree.android.jaws.opengl.BaseOptimizedRendererCompat
import com.kolibree.android.jaws.opengl.OptimizedVbo
import com.kolibree.android.jaws.opengl.colors.ColorConverter
import com.kolibree.kml.MouthZone16
import java.util.HashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

/**
 * Jaws and toothbrush renderer
 */
@Suppress("TooManyFunctions")
internal class CoachPlusRendererImpl(
    memoryManager: MemoryManagerInternal,
    brushHeadModel: Kolibree3DModel,
    private val brushHeadPositionMapper: BrushHeadPositionMapper,
    private val brushHeadAnimationMapper: BrushHeadAnimationMapper
) : BaseOptimizedRendererCompat(
    memoryManager,
    arrayOf(Kolibree3DModel.UPPER_JAW, Kolibree3DModel.LOWER_JAW, brushHeadModel)
),
    CoachPlusRenderer {

    private val facingAngleMapper = CoachFacingAngleMapper()

    private val animator = Animator()

    private val zoneColors = HashMap<MouthZone16, Int>(MouthZone16.values().size)

    @VisibleForTesting
    val currentlyBrushedZone = AtomicReference<MouthZone16>()

    private val showToothbrushHead: AtomicBoolean = AtomicBoolean()

    @ColorInt
    private var ledColor: Int = Color.WHITE

    private var cameraSet: Boolean = false

    init {
        reset()
    }

    // Set the jaws with default untouched color
    override fun reset() {
        for (zone in MouthZone16.values()) {
            zoneColors[zone] = DEFAULT_TEETH_COLOR
        }
    }

    override fun prepareVbo(model: Kolibree3DModel, vbo: OptimizedVbo) =
        when (vbo) {
            is UpperJawVbo -> prepareUpperJawVbo(vbo)
            is LowerJawVbo -> prepareLowerJawVbo(vbo)
            is DefaultBrushHeadVbo -> prepareDefaultBrushHeadVbo(vbo)
            is PlaqlessBrushHeadVbo -> preparePlaqlessBrushHeadVbo(vbo)
            else -> false
        }

    @VisibleForTesting
    fun prepareUpperJawVbo(jawVbo: UpperJawVbo) =
        prepareJawVbo(
            jawVbo,
            UPPER_JAW_OPEN_TRANSLATION_Y,
            UPPER_JAW_OPEN_ROTATION,
            JAW_ANGLE
        )

    @VisibleForTesting
    fun prepareLowerJawVbo(jawVbo: LowerJawVbo) =
        prepareJawVbo(
            jawVbo,
            LOWER_JAW_OPEN_TRANSLATION_Y,
            LOWER_JAW_OPEN_ROTATION,
            -JAW_ANGLE
        )

    @VisibleForTesting
    fun prepareJawVbo(jawVbo: BaseJawVbo, positionY: Float, selfRotationX: Float, angle: Float) =
        jawVbo.run {
            setMouthZoneColors(zoneColors)
            rotationVector.x = angle
            rotationVector.y = 0f
            selfRotationVector.set(selfRotationX, 0f, 0f)
            positionVector.set(0f, positionY, 0f)

            true
        }

    private fun prepareDefaultBrushHeadVbo(vbo: DefaultBrushHeadVbo) =
        showToothbrushHead.get().apply {
            if (this) {
                updateToothbrushHead(vbo)
            }
        }

    @VisibleForTesting
    fun preparePlaqlessBrushHeadVbo(vbo: PlaqlessBrushHeadVbo) =
        showToothbrushHead.get().apply {
            if (this) {
                vbo.setLedColor(ledColor)
                updateToothbrushHead(vbo)
            }
        }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)

        if (!cameraSet) {
            cameraSet = true
            moveCameraZ(CAMERA_Z_OFFSET)
        }
    }

    override fun setCurrentlyBrushedZone(zone: MouthZone16, @ColorInt color: Int) {
        synchronized(zoneColors) {
            zoneColors.put(zone, color)
        }

        val oldZone = currentlyBrushedZone.getAndSet(zone)
        if (oldZone != zone) {
            updateJawsFacingAngle(oldZone, zone)
        }
    }

    override fun showToothbrushHead(show: Boolean) = showToothbrushHead.set(show)

    override fun setBackgroundColor(color: Int) = setEglBackgroundColor(color)

    override fun setRingLedColor(color: Int) {
        ledColor = color
    }

    override fun pause() {
        // no-op
    }

    override fun resume() {
        // no-op
    }

    /*
    Set the jaws facing angle
     */
    private fun updateJawsFacingAngle(oldZone: MouthZone16?, newZone: MouthZone16) {
        val oldAngles = if (oldZone != null)
            facingAngleMapper.mapZoneToFacingAngle(oldZone)
        else
            FloatArray(2)

        val newAngles = facingAngleMapper.mapZoneToFacingAngle(newZone)
        translateCamera(
            computeTranslation(oldAngles[0], newAngles[0]),
            computeTranslation(oldAngles[1], newAngles[1])
        )
    }

    /*
    Compute camera translations
     */
    @VisibleForTesting
    fun computeTranslation(oldAngle: Float, newAngle: Float): Float {
        val angle = when {
            oldAngle == CoachFacingAngleMapper.FACING_FRONT_ANGLE -> newAngle
            newAngle == CoachFacingAngleMapper.FACING_FRONT_ANGLE -> -oldAngle
            else -> newAngle - oldAngle
        }

        // Since we are using floats and relative angles we make sure there is no noise even after
        // a lot of rotations
        return if (abs(angle) > CoachFacingAngleMapper.FACING_POSITIVE_ANGLE / 2f)
            angle
        else
            CoachFacingAngleMapper.FACING_FRONT_ANGLE
    }

    /*
    Set the toothbrush head position and angles, then animate it
     */
    @VisibleForTesting
    fun updateToothbrushHead(vbo: OptimizedVbo) {
        val currentZone = currentlyBrushedZone.get() ?: return

        updateToothbrushHeadPosition(vbo, currentZone)
        animateToothbrushHead(vbo, currentZone)
    }

    private fun updateToothbrushHeadPosition(vbo: OptimizedVbo, zone: MouthZone16) {
        val positionVectors = brushHeadPositionMapper.mapZoneToPositionMatrix(zone)

        vbo.positionVector.set(positionVectors[0])
        vbo.rotationVector.set(positionVectors[1])

        if (positionVectors.size == 3) {
            vbo.selfRotationVector.set(positionVectors[2])
        } else {
            vbo.selfRotationVector.set(0f, 0f, 0f)
        }
    }

    private fun animateToothbrushHead(vbo: OptimizedVbo, zone: MouthZone16) {
        val animation = brushHeadAnimationMapper.getAnimationForZone(zone)
        animator.animate(vbo, animation)
    }

    companion object {

        private val DEFAULT_TEETH_COLOR = ColorConverter
            .toAndroidColor(0.8f, 0.8f, 0.8f, 1f)

        @VisibleForTesting
        const val JAW_ANGLE = 20f

        @VisibleForTesting
        const val CAMERA_Z_OFFSET = -100f

        @VisibleForTesting
        const val UPPER_JAW_OPEN_TRANSLATION_Y = 10f

        @VisibleForTesting
        const val LOWER_JAW_OPEN_TRANSLATION_Y = -13f

        @VisibleForTesting
        const val UPPER_JAW_OPEN_ROTATION = 340f

        @VisibleForTesting
        const val LOWER_JAW_OPEN_ROTATION = 380f
    }
}
