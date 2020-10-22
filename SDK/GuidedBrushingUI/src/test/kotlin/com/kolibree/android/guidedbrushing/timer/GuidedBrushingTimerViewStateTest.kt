/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.timer

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GuidedBrushingTimerViewStateTest : BaseUnitTest() {

    @Test
    fun `default values`() {
        val viewState = GuidedBrushingTimerViewState()

        assertEquals(0, viewState.elapsedMillis)
        assertEquals(0, viewState.lastPassedMillis)
    }

    @Test
    fun `elapsed millis should convert to the right seconds`() {
        val millis = 6000L
        val secondExpected = 6L

        val viewState = GuidedBrushingTimerViewState(elapsedMillis = millis)

        assertEquals(secondExpected, viewState.secondsElapsed())
    }

    @Test
    fun `elapsed millis should convert to the right lowered seconds`() {
        val millis = 5999L
        val secondExpected = 5L

        val viewState = GuidedBrushingTimerViewState(elapsedMillis = millis)

        assertEquals(secondExpected, viewState.secondsElapsed())
    }

    @Test
    fun `lastPassedMillis should have the correct value when timer is started`() {
        val wantedValue = 1000L

        val viewState = getConfiguredViewState().withTimerStarted(wantedValue)

        assertEquals(wantedValue, viewState.lastPassedMillis)
    }

    @Test
    fun `lastPassedMillis and elapsedMillis should have the correct values when a time update occurs`() {
        val startTime = 1000L
        val firstUpdate = 1500L
        val secondUpdate = 2400L

        val viewStateFirstUpdate = GuidedBrushingTimerViewState()
            .withTimerStarted(startTime)
            .withTimeUpdate(firstUpdate)

        assertEquals(firstUpdate, viewStateFirstUpdate.lastPassedMillis)
        assertEquals(500, viewStateFirstUpdate.elapsedMillis)

        val viewStateSecondUpdate = viewStateFirstUpdate
            .withTimeUpdate(secondUpdate)

        assertEquals(secondUpdate, viewStateSecondUpdate.lastPassedMillis)
        assertEquals(1400, viewStateSecondUpdate.elapsedMillis)
    }

    @Test
    fun `restart should set a clean copy of TimerView`() {
        val viewState = getConfiguredViewState().withRestart()

        assertEquals(0, viewState.elapsedMillis)
    }

    private fun getConfiguredViewState(): GuidedBrushingTimerViewState {
        return GuidedBrushingTimerViewState(123, 456)
    }
}
