/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import com.kolibree.kml.MouthZone16.LoIncInt
import com.kolibree.kml.MouthZone16.UpMolLeExt
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Duration

/**
 * [CoachPlusManualControllerImpl] tests
 */
class CoachPlusManualControllerImplTest {

    @Test
    fun reset_resetsValues() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(1), 1L
        )
        controller.currentZoneCompletionMillis = 3000L
        controller.reset()
        assertEquals(0, controller.currentZoneCompletionMillis)
        assertEquals(0, controller.computeBrushingDuration())
    }

    @Test
    fun isCurrentZoneCompleted_true() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        controller.currentZoneCompletionMillis = 10000L
        assertTrue(controller.isCurrentZoneCompleted())
    }

    @Test
    fun isCurrentZoneCompleted_trueEvenWhenOverLapsed() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        controller.currentZoneCompletionMillis = 10001L
        assertTrue(controller.isCurrentZoneCompleted())
    }

    @Test
    fun isCurrentZoneCompleted_false() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        controller.currentZoneCompletionMillis = 9999L
        assertFalse(controller.isCurrentZoneCompleted())
    }

    @Test
    fun computeBrushingDuration_beginning() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        assertEquals(0, controller.computeBrushingDuration())
    }

    @Test
    fun computeBrushingDuration_firstZone() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        controller.currentZoneCompletionMillis = 5000L
        assertEquals(5, controller.computeBrushingDuration())
    }

    @Test
    fun computeBrushingDuration_nthZone() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        controller.currentZoneIndex = 10
        controller.currentZoneCompletionMillis = 4000L
        assertEquals(104, controller.computeBrushingDuration())
    }

    @Test
    fun computeBrushingDuration_end() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 1L
        )
        controller.currentZoneIndex = 15
        controller.currentZoneCompletionMillis = 10000L
        assertEquals(160, controller.computeBrushingDuration())
    }

    @Test
    fun onTick_increasesCurrentZoneCompletionMillisByTickPeriod() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(1), 40L
        )
        assertEquals(0, controller.currentZoneCompletionMillis)
        controller.onTick()
        assertEquals(40L, controller.currentZoneCompletionMillis)
    }

    @Test
    fun onTick_currentZoneNotCompleted() {
        val controller = spy(
            CoachPlusManualControllerImpl(
                Duration.ofSeconds(160),
                0L
            )
        )
        controller.currentZoneCompletionMillis = 3000L

        val result = controller.onTick()
        verify(controller, never()).brushNextZone()
        assertEquals(UpMolLeExt, result.zoneToBrush)
        assertEquals(30, result.completionPercent)
        assertTrue(result.brushingGoodZone)
        assertFalse(result.sequenceFinished)
    }

    @Test
    fun onTick_currentZoneCompletedSequenceNotFinished() {
        val controller = spy(
            CoachPlusManualControllerImpl(
                Duration.ofSeconds(160),
                0L
            )
        )
        controller.currentZoneCompletionMillis = 10000L

        val result = controller.onTick()
        verify(controller).brushNextZone()
        assertEquals(0L, controller.currentZoneCompletionMillis)
        assertEquals(UpMolLeExt, result.zoneToBrush)
        assertEquals(100, result.completionPercent)
        assertTrue(result.brushingGoodZone)
        assertFalse(result.sequenceFinished)
    }

    @Test
    fun onTick_currentZoneCompletedSequenceFinished() {
        val controller = spy(
            CoachPlusManualControllerImpl(
                Duration.ofSeconds(160),
                0L
            )
        )
        controller.currentZoneIndex = 15
        controller.currentZoneCompletionMillis = 10000L

        val result = controller.onTick()
        verify(controller, never()).brushNextZone()
        assertEquals(LoIncInt, result.zoneToBrush)
        assertEquals(100, result.completionPercent)
        assertTrue(result.brushingGoodZone)
        assertTrue(result.sequenceFinished)
    }

    @Test(expected = IllegalStateException::class)
    fun getAvroTransitionsTable_throwsIllegalStateException() {
        val controller = CoachPlusManualControllerImpl(
            Duration.ofSeconds(160), 0L
        )
        controller.getAvroTransitionsTable()
    }
}
