/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.angleandspeed.ui.mindyourspeed

import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.extensions.setFixedDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

class MindYourSpeedViewStateTest : BaseUnitTest() {

    override fun setup() {
        super.setup()
        TrustedClock.setFixedDate()
    }

    @Test
    fun `game starts from the first state`() {
        assertEquals(
            Stage.WAITING_FOR_START,
            MindYourSpeedViewState.initial().stage
        )
    }

    @Test
    fun `first update immediately moves to first stage, regardless of data`() {
        assertEquals(
            Stage.STAGE_1,
            MindYourSpeedViewState.initial()
                .updateWith(feedback(speedFeedback = SpeedFeedback.OVERSPEED))
                .stage
        )
        assertEquals(
            Stage.STAGE_1,
            MindYourSpeedViewState.initial()
                .updateWith(feedback(speedFeedback = SpeedFeedback.UNDERSPEED))
                .stage
        )
        assertEquals(
            Stage.STAGE_1,
            MindYourSpeedViewState.initial()
                .updateWith(feedback(isZoneCorrect = false))
                .stage
        )
    }

    @Test
    fun `on every ongoing stage, progress is updated from 0 to 100% in 10s`() {
        testStateProgressUpdates(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_1))
        testStateProgressUpdates(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_2))
        testStateProgressUpdates(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_3))
    }

    private fun testStateProgressUpdates(initialState: MindYourSpeedViewState) {
        var state = initialState
        val index = initialState.stage.index
        assertEquals(0.0f, state.zoneProgressData.zones[index].progress)
        assertFalse(state.zoneProgressData.zones[index].isOngoing)

        (1..9).forEach {
            TrustedClock.advanceTimeBy(1, ChronoUnit.SECONDS)
            state = state.updateWith(feedback())
            assertEquals(
                (it * 0.1f).toPercentInt(),
                state.zoneProgressData.zones[index].progress.toPercentInt()
            )
            assertEquals(MindYourSpeedFeedback.EMPTY_FEEDBACK, state.feedbackMessage)
        }

        TrustedClock.advanceTimeBy(1, ChronoUnit.SECONDS)
        state = state.updateWith(feedback())
        assertEquals(
            1f.toPercentInt(),
            state.zoneProgressData.zones[index].progress.toPercentInt()
        )
        assertEquals(MindYourSpeedFeedback.EMPTY_FEEDBACK, state.feedbackMessage)
    }

    @Test
    fun `on every ongoing stage, progress advanced to next stage after reaching 100%`() {
        testAdvancementToNextZone(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_1))
        testAdvancementToNextZone(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_2))
        testAdvancementToNextZone(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_3))
    }

    private fun testAdvancementToNextZone(initialState: MindYourSpeedViewState) {
        var state = initialState

        TrustedClock.advanceTimeBy(10, ChronoUnit.SECONDS)
        state = state.updateWith(feedback())

        assertEquals(
            initialState.stage.nextStage(),
            state.stage
        )
    }

    @Test
    fun `on every ongoing stage, progress is not updated for underspeed`() {
        testUnderspeed(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_1))
        testUnderspeed(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_2))
        testUnderspeed(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_3))
    }

    private fun testUnderspeed(initialState: MindYourSpeedViewState) {
        var state = initialState
        val index = initialState.stage.index
        assertEquals(0.0f, state.zoneProgressData.zones[index].progress)
        assertFalse(state.zoneProgressData.zones[index].isOngoing)

        repeat((1..10).count()) {
            TrustedClock.advanceTimeBy(1, ChronoUnit.SECONDS)
            state = state.updateWith(feedback(SpeedFeedback.UNDERSPEED))
            assertEquals(
                0f.toPercentInt(),
                state.zoneProgressData.zones[index].progress.toPercentInt()
            )
            assertEquals(MindYourSpeedFeedback.TOO_SLOW, state.feedbackMessage)
        }
    }

    @Test
    fun `on every ongoing stage, progress is not updated for overspeed`() {
        testOverspeed(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_1))
        testOverspeed(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_2))
        testOverspeed(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_3))
    }

    private fun testOverspeed(initialState: MindYourSpeedViewState) {
        var state = initialState
        val index = initialState.stage.index
        assertEquals(0.0f, state.zoneProgressData.zones[index].progress)
        assertFalse(state.zoneProgressData.zones[index].isOngoing)

        repeat((1..10).count()) {
            TrustedClock.advanceTimeBy(1, ChronoUnit.SECONDS)
            state = state.updateWith(feedback(SpeedFeedback.OVERSPEED))
            assertEquals(
                0f.toPercentInt(),
                state.zoneProgressData.zones[index].progress.toPercentInt()
            )
            assertEquals(MindYourSpeedFeedback.TOO_FAST, state.feedbackMessage)
        }
    }

    @Test
    fun `on every ongoing stage, progress is not updated for wrong zone`() {
        textWrongZone(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_1))
        textWrongZone(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_2))
        textWrongZone(MindYourSpeedViewState.initial().copy(stage = Stage.STAGE_3))
    }

    private fun textWrongZone(initialState: MindYourSpeedViewState) {
        var state = initialState
        val index = initialState.stage.index
        assertEquals(0.0f, state.zoneProgressData.zones[index].progress)
        assertFalse(state.zoneProgressData.zones[index].isOngoing)

        repeat((1..10).count()) {
            TrustedClock.advanceTimeBy(1, ChronoUnit.SECONDS)
            state = state.updateWith(feedback(isZoneCorrect = false))
            assertEquals(
                0f.toPercentInt(),
                state.zoneProgressData.zones[index].progress.toPercentInt()
            )
            assertEquals(MindYourSpeedFeedback.WRONG_ZONE, state.feedbackMessage)
        }
    }

    @Test
    fun `progress can be reset on upon user request`() {
        val time = TrustedClock.getNowInstant()
        val halfWayProgress = ZoneProgressData.create(Stage.NUMBER_OF_ONGOING_STAGES)
            .updateProgressOnZone(1, 1f)
            .updateProgressOnZone(2, 0.5f)
        val halfWayState = MindYourSpeedViewState.initial()
            .copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE,
                isPaused = true,
                stage = Stage.STAGE_2,
                speedFeedback = SpeedFeedback.OVERSPEED,
                lastUpdateTime = time,
                zoneProgressData = halfWayProgress
            )

        TrustedClock.advanceTimeBy(2, ChronoUnit.SECONDS)
        val stateAfterReset = halfWayState.withProgressReset()

        assertEquals(halfWayState.lostConnectionState, stateAfterReset.lostConnectionState)
        assertEquals(halfWayState.isPaused, stateAfterReset.isPaused)
        assertEquals(halfWayState.speedFeedback, stateAfterReset.speedFeedback)

        assertEquals(TrustedClock.getNowInstant(), stateAfterReset.lastUpdateTime)
        assertEquals(
            halfWayState.lastUpdateTime.plus(2, ChronoUnit.SECONDS),
            stateAfterReset.lastUpdateTime
        )
        assertEquals(Stage.STAGE_1, stateAfterReset.stage)
        assertEquals(
            ZoneProgressData.create(Stage.NUMBER_OF_ONGOING_STAGES),
            stateAfterReset.zoneProgressData
        )
    }

    @Test
    fun `pausing state saves the last update time`() {
        val state = MindYourSpeedViewState.initial().copy(isPaused = false)

        TrustedClock.advanceTimeBy(2, ChronoUnit.SECONDS)
        val pausedState = state.withPausedState()

        assertEquals(
            state.copy(
                stage = state.stage.nextStage(),
                isPaused = true,
                lastUpdateTime = state.lastUpdateTime.plus(2, ChronoUnit.SECONDS)
            ),
            pausedState
        )
    }

    @Test
    fun `unpausing state saves the last update time`() {
        val state = MindYourSpeedViewState.initial().copy(isPaused = false)

        TrustedClock.advanceTimeBy(2, ChronoUnit.SECONDS)
        val unpausedState = state.withUnpausedState()

        assertEquals(
            state.copy(
                stage = state.stage.nextStage(),
                isPaused = false,
                lastUpdateTime = state.lastUpdateTime.plus(2, ChronoUnit.SECONDS)
            ),
            unpausedState
        )
    }

    @Test
    fun `time during pause is not included in progress calculation`() {
        var state = MindYourSpeedViewState.initial().copy(
            isPaused = false,
            stage = Stage.STAGE_1,
            speedFeedback = SpeedFeedback.CORRECT
        )

        TrustedClock.advanceTimeBy(1, ChronoUnit.SECONDS)
        state = state.updateWith(feedback())
        assertEquals(
            0.1f.toPercentInt(),
            state.zoneProgressData.zones[0].progress.toPercentInt()
        )

        state = state.withPausedState()
        TrustedClock.advanceTimeBy(5, ChronoUnit.SECONDS)
        state = state.updateWith(feedback())
        assertEquals(
            0.1f.toPercentInt(),
            state.zoneProgressData.zones[0].progress.toPercentInt()
        )

        state = state.withUnpausedState()
        TrustedClock.advanceTimeBy(5, ChronoUnit.SECONDS)
        state = state.updateWith(feedback())
        assertEquals(
            0.6f.toPercentInt(),
            state.zoneProgressData.zones[0].progress.toPercentInt()
        )
    }

    @Test
    fun `advancing completed stage is a no-op operation`() {
        val initialState = MindYourSpeedViewState.initial().copy(stage = Stage.COMPLETED)

        TrustedClock.advanceTimeBy(10, ChronoUnit.SECONDS)
        val updatedState = initialState.updateWith(feedback())

        assertEquals(updatedState, updatedState)
    }
}
