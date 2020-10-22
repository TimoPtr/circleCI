/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.renderer

import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.coach.animation.RegularBrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.brushhead.PlaqlessBrushHeadPositionMapper
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererImpl.Companion.CAMERA_Z_OFFSET
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererImpl.Companion.JAW_ANGLE
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererImpl.Companion.LOWER_JAW_OPEN_ROTATION
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererImpl.Companion.LOWER_JAW_OPEN_TRANSLATION_Y
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererImpl.Companion.UPPER_JAW_OPEN_ROTATION
import com.kolibree.android.jaws.coach.renderer.CoachPlusRendererImpl.Companion.UPPER_JAW_OPEN_TRANSLATION_Y
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [CoachPlusRendererImpl] tests
 */
class CoachPlusRendererImplTest {

    private lateinit var renderer: CoachPlusRendererImpl

    @Before
    fun before() {
        val memoryManager = mock<MemoryManagerInternal>()
        val positionMapper = PlaqlessBrushHeadPositionMapper()
        val animationMapper = RegularBrushHeadAnimationMapper()
        renderer = CoachPlusRendererImpl(
            memoryManager,
            Kolibree3DModel.TOOTHBRUSH,
            positionMapper,
            animationMapper
        )
    }

    @Test
    fun computeTranslationX_withPreviousStateFrontReturnsNewAngle() {
        assertEquals(1f, renderer.computeTranslation(0f, 1f))
        assertEquals(-1f, renderer.computeTranslation(0f, -1f))
    }

    @Test
    fun computeTranslationX_withNewStateFrontAndRotatedOldStateReturnsNewAngleOpposite() {
        assertEquals(-1f, renderer.computeTranslation(1f, 0f))
        assertEquals(1f, renderer.computeTranslation(-1f, 0f))
    }

    @Test
    fun computeTranslationX_oppositeStatesReturnsNewAngleMinusOldAngle() {
        assertEquals(0f, renderer.computeTranslation(-1f, -1f))
        assertEquals(-2f, renderer.computeTranslation(1f, -1f))
        assertEquals(2f, renderer.computeTranslation(-1f, 1f))
        assertEquals(0f, renderer.computeTranslation(1f, 1f))
    }

    /*
    Constants
     */

    @Test
    fun `value of JAW_ANGLE is 20f`() {
        assertEquals(20f, JAW_ANGLE)
    }

    @Test
    fun `value of CAMERA_Z_OFFSET is -100f`() {
        assertEquals(-100f, CAMERA_Z_OFFSET)
    }

    @Test
    fun `value of UPPER_JAW_OPEN_TRANSLATION_Y is 10f`() {
        assertEquals(10f, UPPER_JAW_OPEN_TRANSLATION_Y)
    }

    @Test
    fun `value of LOWER_JAW_OPEN_TRANSLATION_Y is -13f`() {
        assertEquals(-13f, LOWER_JAW_OPEN_TRANSLATION_Y)
    }

    @Test
    fun `value of UPPER_JAW_OPEN_ROTATION is 340f`() {
        assertEquals(340f, UPPER_JAW_OPEN_ROTATION)
    }

    @Test
    fun `value of LOWER_JAW_OPEN_ROTATION is 380f`() {
        assertEquals(380f, LOWER_JAW_OPEN_ROTATION)
    }
}
