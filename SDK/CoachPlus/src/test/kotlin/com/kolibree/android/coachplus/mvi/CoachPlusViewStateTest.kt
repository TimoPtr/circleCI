/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import android.graphics.Color
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.coachplus.controller.CoachPlusControllerResult
import com.kolibree.android.coachplus.controller.kml.CoachPlusKmlControllerImpl
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CoachPlusViewStateTest : BaseUnitTest() {

    @Test
    fun `default values`() {
        val default = CoachPlusViewState(true)
        assertFalse(default.isInit)
        assertFalse(default.isPlaying)
        assertFalse(default.isBrushingMovementEnabled)
        assertFalse(default.isHelpTextEnabled)
        assertNull(default.currentZone)
        assertEquals(0, default.currentZoneProgress)
        assertFalse(default.isBrushingGoodZone)
        assertEquals(FeedBackMessage.EmptyFeedback, default.feedBackMessage)
        assertNull(default.lostConnectionState)
        assertEquals(Color.BLUE, default.ringLedColor)
    }

    /*
    initial
     */

    @Test
    fun `initial with isManual true returns state with isPlaying false, isInit false and isManual true`() {
        val cpv = CoachPlusViewState.initial(true)
        assertFalse(cpv.isPlaying)
        assertFalse(cpv.isInit)
        assertTrue(cpv.isManual)
    }

    @Test
    fun `initial with isManual false returns state with isPlaying false, isInit false and isManual false`() {
        val cpv = CoachPlusViewState.initial(false)
        assertFalse(cpv.isPlaying)
        assertFalse(cpv.isInit)
        assertFalse(cpv.isManual)
    }

    /*
    shouldShowToothbrushHead
     */

    @Test
    fun `shouldShowToothbrushHead returns false when isPlaying false or isBrushingMovementEnabled false`() {
        CoachPlusViewState(true, isPlaying = false).apply { assertFalse(shouldShowToothbrushHead) }
        CoachPlusViewState(true, isPlaying = false, isBrushingMovementEnabled = true)
            .apply { assertFalse(shouldShowToothbrushHead) }
        CoachPlusViewState(true, isPlaying = true, isBrushingMovementEnabled = false)
            .apply { assertFalse(shouldShowToothbrushHead) }
    }

    @Test
    fun `shouldShowToothbrushHead returns true when isPlaying and isBrushingMovementEnabled are true`() {
        CoachPlusViewState(true, isPlaying = true, isBrushingMovementEnabled = true)
            .apply { assertTrue(shouldShowToothbrushHead) }
    }

    @Test
    fun `shouldShowPause returns false when isInit and not(isPlaying) returns true`() {
        CoachPlusViewState(true, isInit = false, isPlaying = false).apply { assertFalse(shouldShowPause) }
        CoachPlusViewState(true, isInit = false, isPlaying = true).apply { assertFalse(shouldShowPause) }
        CoachPlusViewState(true, isInit = true, isPlaying = true).apply { assertFalse(shouldShowPause) }
    }

    /*
    shouldShowPause
     */

    @Test
    fun `shouldShowPause returns true when isInit true and isPlaying false and isEnd is false`() {
        CoachPlusViewState(true, isInit = true, isPlaying = false, isEnd = false).apply {
            assertTrue(shouldShowPause)
        }
    }

    @Test
    fun `shouldShowPause returns false when isInit true, isPlaying true isEnd false and out of mouth`() {
        CoachPlusViewState(
            isManual = true,
            isInit = true,
            isPlaying = false,
            isEnd = false,
            outOfMouth = true
        ).apply { assertFalse(shouldShowPause) }
    }

    @Test
    fun `shouldShowPause returns true when isInit true, isPlaying false isEnd false and not out of mouth`() {
        CoachPlusViewState(
            isManual = true,
            isInit = true,
            isPlaying = false,
            isEnd = false
        ).apply { assertTrue(shouldShowPause) }
    }

    @Test
    fun `shouldShowPause returns false when isInit true and isPlaying false and isEnd is true`() {
        CoachPlusViewState(true, isInit = true, isPlaying = false, isEnd = true).apply {
            assertFalse(shouldShowPause)
        }
    }

    /*
    optionalFeedback
     */

    @Test
    fun `optionalFeedback returns null when isPlaying false`() {
        assertNull(CoachPlusViewState(true, isPlaying = false).optionalFeedback)
    }

    @Test
    fun `optionalFeedback returns feedBackMessage when isPlaying true`() {
        CoachPlusViewState(true, isPlaying = true).apply {
            assertEquals(feedBackMessage, optionalFeedback)
        }
    }

    /*
    updateWith
     */

    @Test
    fun `updateWith updates zone, zoneProgress, isBrushingGoodZone, feedback`() {
        CoachPlusViewState(true).let { old ->
            val new = old.updateWith(
                CoachPlusControllerResult(
                    zoneToBrush = MouthZone16.LoIncInt,
                    completionPercent = 55,
                    brushingGoodZone = true,
                    sequenceFinished = false,
                    feedBackMessage = FeedBackMessage.WrongIncisorsIntAngleFeedback
                )
            )
            assertEquals(MouthZone16.LoIncInt, new.currentZone)
            assertEquals(55, new.currentZoneProgress)
            assertTrue(new.isBrushingGoodZone)
            assertEquals(FeedBackMessage.WrongIncisorsIntAngleFeedback, new.feedBackMessage)

            assertNotSame(old, new)
        }
    }

    @Test
    fun `updateWith set outOfMouth to true if playing and feedback is OutOfMouthFeedback`() {
        CoachPlusViewState(true, isPlaying = true).let { old ->
            val new = old.updateWith(
                CoachPlusControllerResult(
                    MouthZone16.LoIncInt,
                    55,
                    true,
                    false,
                    FeedBackMessage.OutOfMouthFeedback
                )
            )
            assertTrue(new.outOfMouth)
        }
    }

    @Test
    fun `updateWith set outOfMouth to false if not playing and feedback is OutOfMouthFeedback`() {
        CoachPlusViewState(true, isPlaying = false).let { old ->
            val new = old.updateWith(
                CoachPlusControllerResult(
                    MouthZone16.LoIncInt,
                    55,
                    true,
                    false,
                    FeedBackMessage.OutOfMouthFeedback
                )
            )
            assertFalse(new.outOfMouth)
        }
    }

    /*
    updateZoneProgress
     */

    @Test
    fun `updateZoneProgress updates zoneProgressData`() {

        val viewState = CoachPlusViewState(true)
        val coachPlusResult = CoachPlusControllerResult(
            zoneToBrush = MouthZone16.UpMolLeExt,
            brushingGoodZone = true,
            sequenceFinished = false,
            completionPercent = 50,
            feedBackMessage = FeedBackMessage.EmptyFeedback
        )
        val newViewState = viewState.updateWith(coachPlusResult)

        val currentZone = newViewState.zoneProgressData.zones[0]
        assertTrue(currentZone.isOngoing)
        assertEquals(0.5f, currentZone.progress)
    }

    /*
    getHint
     */

    @Test
    fun `getHint returns null when feedback should show true`() {
        CoachPlusViewState(true, feedBackMessage = FeedBackMessage.WrongIncisorsIntAngleFeedback).apply {
            assertNull(getHint(mock(), mock()))
        }
    }

    @Test
    fun `getHint returns null when isHelpTextEnables false`() {
        CoachPlusViewState(true, isHelpTextEnabled = false).apply {
            assertNull(getHint(mock(), mock()))
        }
    }

    @Test
    fun `getHint returns null when isBrushingGoodZone and isHelpTextEnabled are true and currentZone null`() {
        CoachPlusViewState(true, isHelpTextEnabled = true, isBrushingGoodZone = true, currentZone = null).apply {
            assertNull(getHint(mock(), mock()))
        }
    }

    @Test
    fun `getHint returns null when isHelpTextEnabled true ans isBrushingGoodZone false and controller is using KML`() {
        val controller = mock<CoachPlusKmlControllerImpl>()
        CoachPlusViewState(true, isHelpTextEnabled = true, isBrushingGoodZone = false).apply {
            assertNull(getHint(controller, mock()))
        }
    }

    @Test
    fun `getHint returns result of provideHintForZone when isBrushingGoodZone and isHelpTextEnabled are true and currentZone not null null`() {
        val zone = MouthZone16.LoIncInt
        val zoneHintProvider = mock<ZoneHintProvider>()
        val expectedHint = 22

        whenever(zoneHintProvider.provideHintForZone(zone)).thenReturn(expectedHint)
        CoachPlusViewState(true, isHelpTextEnabled = true, isBrushingGoodZone = true, currentZone = zone).apply {
            assertEquals(expectedHint, getHint(mock(), zoneHintProvider))
        }
    }

    @Test
    fun `getHint return mouth_zone_wrong when isHelpTextEnable true, isBrushingGoodZone false and not using KML`() {
        val controller = mock<CoachPlusController>()
        CoachPlusViewState(true, isHelpTextEnabled = true, isBrushingGoodZone = false).apply {
            val zoneHintProvider: ZoneHintProvider = mock()
            val expectedResId = 1234
            doReturn(expectedResId).whenever(zoneHintProvider).provideHintForWrongZone()
            assertEquals(expectedResId, getHint(controller, zoneHintProvider))
        }
    }
}
