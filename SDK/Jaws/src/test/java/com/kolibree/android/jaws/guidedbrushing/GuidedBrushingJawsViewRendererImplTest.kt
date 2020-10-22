/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.guidedbrushing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.CAMERA_Z
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.LOWER_JAW_POSITION_VECTOR_Z
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.LOWER_JAW_ROTATION
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.LOWER_JAW_TRANSLATION_Y
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.SELF_ROTATION_VECTOR_Y
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.UPPER_JAW_ROTATION
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsViewRendererImpl.Companion.UPPER_JAW_TRANSLATION_Y
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltController
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [GuidedBrushingJawsViewRendererImpl] tests
 */
class GuidedBrushingJawsViewRendererImplTest : BaseUnitTest() {

    private val memoryManager = mock<MemoryManagerInternal>()

    private val tiltController = mock<AnimatedTiltController>()

    private lateinit var renderer: GuidedBrushingJawsViewRendererImpl

    override fun setup() {
        super.setup()

        renderer = GuidedBrushingJawsViewRendererImpl(memoryManager, tiltController)
    }

    /*
    updateJawsFacingAngle
     */

    @Test
    fun `updateJawsFacingAngle invokes faceCenter() when zone is UpMolLeOcc`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpMolLeOcc)
        verify(tiltController).faceCenter()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceCenter() when zone is UpMolRiOcc`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpMolRiOcc)
        verify(tiltController).faceCenter()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceCenter() when zone is LoMolRiOcc`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoMolRiOcc)
        verify(tiltController).faceCenter()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceCenter() when zone is LoIncExt`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoIncExt)
        verify(tiltController).faceCenter()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceCenter() when zone is UpIncExt`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpIncExt)
        verify(tiltController).faceCenter()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceTop() when zone is UpIncInt`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpIncInt)
        verify(tiltController).faceTop()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceBottom() when zone is LoIncInt`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoIncInt)
        verify(tiltController).faceBottom()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceLeft() when zone is UpMolLeInt`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpMolLeInt)
        verify(tiltController).faceLeft()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceLeft() when zone is LoMolLeInt`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoMolLeInt)
        verify(tiltController).faceLeft()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceLeft() when zone is UpMolRiExt`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpMolRiExt)
        verify(tiltController).faceLeft()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceLeft() when zone is LoMolRiExt`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoMolRiExt)
        verify(tiltController).faceLeft()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceRight() when zone is LoMolLeExt`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoMolLeExt)
        verify(tiltController).faceRight()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceRight() when zone is UpMolLeExt`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpMolLeExt)
        verify(tiltController).faceRight()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceRight() when zone is UpMolRiInt`() {
        renderer.updateJawsFacingAngle(MouthZone16.UpMolRiInt)
        verify(tiltController).faceRight()
    }

    @Test
    fun `updateJawsFacingAngle invokes faceRight() when zone is LoMolRiInt`() {
        renderer.updateJawsFacingAngle(MouthZone16.LoMolRiInt)
        verify(tiltController).faceRight()
    }

    /*
    Constants
     */

    @Test
    fun `value of CAMERA_Z is 0f`() {
        assertEquals(0f, CAMERA_Z)
    }

    @Test
    fun `value of UPPER_JAW_ROTATION is 370f`() {
        assertEquals(370f, UPPER_JAW_ROTATION)
    }

    @Test
    fun `value of UPPER_JAW_TRANSLATION_Y is 0,33f`() {
        assertEquals(0.33f, UPPER_JAW_TRANSLATION_Y)
    }

    @Test
    fun `value of LOWER_JAW_ROTATION is 355f`() {
        assertEquals(355f, LOWER_JAW_ROTATION)
    }

    @Test
    fun `value of LOWER_JAW_TRANSLATION_Y is -0,35f`() {
        assertEquals(-0.35f, LOWER_JAW_TRANSLATION_Y)
    }

    @Test
    fun `value of SELF_ROTATION_VECTOR_Y is 180f`() {
        assertEquals(180f, SELF_ROTATION_VECTOR_Y)
    }

    @Test
    fun `value of LOWER_JAW_POSITION_VECTOR_Z is -0,017048f`() {
        assertEquals(-0.017048f, LOWER_JAW_POSITION_VECTOR_Z)
    }
}
