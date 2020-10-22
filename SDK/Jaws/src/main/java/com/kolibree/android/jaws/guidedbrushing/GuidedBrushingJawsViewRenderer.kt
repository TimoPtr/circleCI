/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.guidedbrushing

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.ColorUtils
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.base.BaseJawsRenderer
import com.kolibree.android.jaws.base.JawsRenderer
import com.kolibree.android.jaws.models.HumLowerJawVbo
import com.kolibree.android.jaws.models.HumUpperJawVbo
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltController
import com.kolibree.kml.MouthZone16
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/** Guided Brushing [JawsRenderer] implementation */
@Keep
interface GuidedBrushingJawsViewRenderer : JawsRenderer {

    /**
     * Set the currently brushed zone and its completion percentage
     *
     * @param zone non null [MouthZone16]
     * @param progress zone coverage in percents [Int]
     */
    fun setCurrentlyBrushedZone(zone: MouthZone16, progress: Int)

    /**
     * Reset (or set) the 3D view as it has to be at the beginning of the brushing activity
     */
    fun reset()

    /**
     * Currently brushed zone [ColorInt]
     */
    val currentZoneColor: AtomicInteger

    /**
     * Missed zones [ColorInt]
     */
    val missedZonesColor: AtomicInteger
}

/** [GuidedBrushingJawsViewRenderer] implementation */
internal class GuidedBrushingJawsViewRendererImpl @Inject constructor(
    memoryManager: MemoryManagerInternal,
    private val tiltController: AnimatedTiltController
) : BaseJawsRenderer(
    memoryManager = memoryManager
), GuidedBrushingJawsViewRenderer {

    private val currentlyBrushedZone = AtomicReference<MouthZone16>()

    private val currentlyBrushedZoneProgressPercent = AtomicInteger()

    override val cameraZ = CAMERA_Z

    override val currentZoneColor = AtomicInteger()

    override val missedZonesColor = AtomicInteger()

    init {
        reset()
        setTiltController(tiltController)
    }

    override fun setCurrentlyBrushedZone(zone: MouthZone16, progress: Int) {
        val oldZone = currentlyBrushedZone.getAndSet(zone)
        val oldProgress = currentlyBrushedZoneProgressPercent.getAndSet(progress)

        synchronized(zoneColors) {
            zoneColors[zone] = blendToWhite(currentZoneColor.get(), progress / HUNDRED_PERCENTS)
        }

        if (oldZone != zone) {
            if (oldZone != null) {
                synchronized(zoneColors) {
                    zoneColors[oldZone] =
                        blendToWhite(missedZonesColor.get(), oldProgress / HUNDRED_PERCENTS)
                }
            }
            updateJawsFacingAngle(zone)
        }
    }

    override fun reset() {
        synchronized(zoneColors) {
            for (zone in MouthZone16.values()) {
                zoneColors[zone] = missedZonesColor.get()
            }
        }
        currentlyBrushedZoneProgressPercent.set(0)
    }

    override fun prepareHumUpperJawVbo(upperJawVbo: HumUpperJawVbo) {
        super.prepareHumUpperJawVbo(upperJawVbo)

        upperJawVbo.selfRotationVector.x = UPPER_JAW_ROTATION
        upperJawVbo.selfRotationVector.y = SELF_ROTATION_VECTOR_Y
        upperJawVbo.positionVector.y = UPPER_JAW_TRANSLATION_Y
    }

    override fun prepareHumLowerJawVbo(lowerJawVbo: HumLowerJawVbo) {
        super.prepareHumLowerJawVbo(lowerJawVbo)

        lowerJawVbo.selfRotationVector.x = LOWER_JAW_ROTATION
        lowerJawVbo.selfRotationVector.y = SELF_ROTATION_VECTOR_Y
        lowerJawVbo.positionVector.y = LOWER_JAW_TRANSLATION_Y
        lowerJawVbo.positionVector.z = LOWER_JAW_POSITION_VECTOR_Z
    }

    /*
    Set the jaws facing angle
     */
    @VisibleForTesting
    fun updateJawsFacingAngle(zone: MouthZone16) = when (zone) {
        MouthZone16.UpMolLeOcc,
        MouthZone16.LoMolLeOcc,
        MouthZone16.UpMolRiOcc,
        MouthZone16.LoMolRiOcc,
        MouthZone16.LoIncExt,
        MouthZone16.UpIncExt -> tiltController.faceCenter()
        MouthZone16.UpIncInt -> tiltController.faceTop()
        MouthZone16.LoIncInt -> tiltController.faceBottom()
        MouthZone16.UpMolLeInt,
        MouthZone16.LoMolLeInt,
        MouthZone16.UpMolRiExt,
        MouthZone16.LoMolRiExt -> tiltController.faceLeft()
        MouthZone16.LoMolLeExt,
        MouthZone16.UpMolLeExt,
        MouthZone16.UpMolRiInt,
        MouthZone16.LoMolRiInt -> tiltController.faceRight()
    }

    private fun blendToWhite(@ColorInt color: Int, ratio: Float) =
        ColorUtils.blendARGB(color, Color.WHITE, ratio)

    companion object {

        @VisibleForTesting
        const val CAMERA_Z = 0f

        @VisibleForTesting
        const val UPPER_JAW_ROTATION = 370f

        @VisibleForTesting
        const val UPPER_JAW_TRANSLATION_Y = 0.33f

        @VisibleForTesting
        const val LOWER_JAW_ROTATION = 355f

        @VisibleForTesting
        const val LOWER_JAW_TRANSLATION_Y = -0.35f

        @VisibleForTesting
        const val SELF_ROTATION_VECTOR_Y = 180f

        @VisibleForTesting
        const val LOWER_JAW_POSITION_VECTOR_Z = -0.017048f

        private const val HUNDRED_PERCENTS = 100f
    }
}
