/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.SpeedKPI
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FeedbackMessageTest : BaseUnitTest() {

    @Test
    fun `EmptyFeedbackMessage is well define`() {
        assertEquals(Int.MAX_VALUE, FeedBackMessage.EmptyFeedback.priorityLevel)
        assertFalse(FeedBackMessage.EmptyFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.None, FeedBackMessage.EmptyFeedback.warningLevel)
    }

    @Test
    fun `OutOfMouthFeedback is well define`() {
        assertEquals(0, FeedBackMessage.OutOfMouthFeedback.priorityLevel)
        assertTrue(FeedBackMessage.OutOfMouthFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Critical, FeedBackMessage.OutOfMouthFeedback.warningLevel)
    }

    @Test
    fun `WrongZoneFeedback is well define`() {
        assertEquals(1, FeedBackMessage.WrongZoneFeedback.priorityLevel)
        assertTrue(FeedBackMessage.WrongZoneFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Normal, FeedBackMessage.WrongZoneFeedback.warningLevel)
    }

    @Test
    fun `RinseBrushHeadFeedback is well define`() {
        assertEquals(2, FeedBackMessage.RinseBrushHeadFeedback.priorityLevel)
        assertTrue(FeedBackMessage.RinseBrushHeadFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Critical, FeedBackMessage.RinseBrushHeadFeedback.warningLevel)
    }

    @Test
    fun `WrongHandleFeedback is well define`() {
        assertEquals(3, FeedBackMessage.WrongHandleFeedback.priorityLevel)
        assertTrue(FeedBackMessage.WrongHandleFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Severe, FeedBackMessage.WrongHandleFeedback.warningLevel)
    }

    @Test
    fun `OverpressureFeedback is well define`() {
        assertEquals(3, FeedBackMessage.OverpressureFeedback.priorityLevel)
        assertTrue(FeedBackMessage.OverpressureFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Severe, FeedBackMessage.OverpressureFeedback.warningLevel)
    }

    @Test
    fun `WrongIncisorsIntAngleFeedback is well define`() {
        assertEquals(4, FeedBackMessage.WrongIncisorsIntAngleFeedback.priorityLevel)
        assertTrue(FeedBackMessage.WrongIncisorsIntAngleFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Normal, FeedBackMessage.WrongIncisorsIntAngleFeedback.warningLevel)
    }

    @Test
    fun `Wrong45AngleFeedback is well define`() {
        assertEquals(4, FeedBackMessage.Wrong45AngleFeedback.priorityLevel)
        assertTrue(FeedBackMessage.Wrong45AngleFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Normal, FeedBackMessage.Wrong45AngleFeedback.warningLevel)
    }

    @Test
    fun `UnderSpeedFeedback is well define`() {
        assertEquals(5, FeedBackMessage.UnderSpeedFeedback.priorityLevel)
        assertTrue(FeedBackMessage.UnderSpeedFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Normal, FeedBackMessage.UnderSpeedFeedback.warningLevel)
    }

    @Test
    fun `OverSpeedFeedback is well define`() {
        assertEquals(5, FeedBackMessage.OverSpeedFeedback.priorityLevel)
        assertTrue(FeedBackMessage.OverSpeedFeedback.shouldShow)
        assertEquals(FeedbackWarningLevel.Normal, FeedBackMessage.OverSpeedFeedback.warningLevel)
    }

    @Test
    fun `12 zone for wrong angle are covered`() {
        val setOfZones = mutableSetOf<MouthZone16>()

        setOfZones.addAll(incisorsIntZone)
        setOfZones.addAll(wrongAngle45PossibleZone)

        assertEquals(12, setOfZones.size)

        assertTrue(setOfZones.none {
            it == MouthZone16.LoMolLeOcc ||
                it == MouthZone16.LoMolRiOcc ||
                it == MouthZone16.UpMolLeOcc ||
                it == MouthZone16.UpMolRiOcc
        })
    }

    @Test
    fun `getAngleFeedback returns WrongIncisorsIntAngleFeedback when orientation is not correct`() {
        incisorsIntZone.forEach {
            assertEquals(FeedBackMessage.WrongIncisorsIntAngleFeedback, getAngleFeedback(false, it))
        }
    }

    @Test
    fun `getAngleFeedback returns Wrong45AngleFeedback when orientation is not correct`() {
        wrongAngle45PossibleZone.forEach {
            assertEquals(FeedBackMessage.Wrong45AngleFeedback, getAngleFeedback(false, it))
        }
    }

    @Test
    fun `getAngleFeedback returns null when orientation is correct`() {
        MouthZone16.values().forEach {
            assertNull(getAngleFeedback(true, it))
        }
    }

    @Test
    fun `getSpeedFeedback returns null when speed is correct`() {
        assertNull(getSpeedFeedback(SpeedKPI.Correct))
    }

    @Test
    fun `getSpeedFeedback returns OverSpeedFeedback when speed is too fast`() {
        assertEquals(FeedBackMessage.OverSpeedFeedback, getSpeedFeedback(SpeedKPI.Overspeed))
    }

    @Test
    fun `getSpeedFeedback returns UnderSpeedFeedback when speed is too slow`() {
        assertEquals(FeedBackMessage.UnderSpeedFeedback, getSpeedFeedback(SpeedKPI.Underspeed))
    }

    @Test
    fun `getPlaqlessFeedback returns OutOfMouthFeedback when plaqlessError is OUT_OF_MOUTH`() {
        assertEquals(FeedBackMessage.OutOfMouthFeedback, getPlaqlessFeedback(PlaqlessError.OUT_OF_MOUTH))
    }

    @Test
    fun `getPlaqlessFeedback returns RinseBrushHeadFeedback when plaqlessError is RINSE_BRUSH_HEAD`() {
        assertEquals(FeedBackMessage.RinseBrushHeadFeedback, getPlaqlessFeedback(PlaqlessError.RINSE_BRUSH_HEAD))
    }

    @Test
    fun `getPlaqlessFeedback returns WrongHandleFeedback when plaqlessError is WRONG_HANDLE`() {
        assertEquals(
            FeedBackMessage.WrongHandleFeedback, getPlaqlessFeedback(PlaqlessError.WRONG_HANDLE)
        )
    }

    @Test
    fun `getPlaqlessFeedback returns null when plaqlessError is NONE`() {
        assertNull(getPlaqlessFeedback(PlaqlessError.NONE))
    }

    @Test
    fun `getPlaqlessFeedback returns null when plaqlessError is UNKNOWN`() {
        assertNull(getPlaqlessFeedback(PlaqlessError.UNKNOWN))
    }

    @Test
    fun `getPlaqlessFeedback returns null when plaqlessError is REPLACE_BRUSH_HEAD`() {
        assertNull(getPlaqlessFeedback(PlaqlessError.REPLACE_BRUSH_HEAD))
    }
}
