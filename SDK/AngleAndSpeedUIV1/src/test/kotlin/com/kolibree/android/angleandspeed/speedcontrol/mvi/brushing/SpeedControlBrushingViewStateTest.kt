/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.extensions.setFixedDate
import java.time.Duration
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

class SpeedControlBrushingViewStateTest : BaseUnitTest() {

    private val initialState = SpeedControlBrushingViewState.initial()

    @Test
    fun `initialState contains OUTER_MOLARS stage`() {
        assertEquals(Stage.OUTER_MOLARS, initialState.currentStage)
    }

    @Test
    fun `updateWith keeps the feedback value after update`() {
        assertEquals(
            SpeedFeedback.UNDERSPEED,
            initialState.updateWith(
                feedbackWithSpeed(
                    SpeedFeedback.UNDERSPEED
                )
            ).speedFeedback
        )
        assertEquals(
            SpeedFeedback.OVERSPEED,
            initialState.updateWith(
                feedbackWithSpeed(
                    SpeedFeedback.OVERSPEED
                )
            ).speedFeedback
        )
        assertEquals(
            SpeedFeedback.CORRECT,
            initialState.updateWith(
                feedbackWithSpeed(
                    SpeedFeedback.CORRECT
                )
            ).speedFeedback
        )
    }

    @Test
    fun `updateWith doesnt increase duration if speed is OVERSPEED`() {
        val feedback =
            feedbackWithSpeed(SpeedFeedback.OVERSPEED)
        val newState = initialState.updateWith(feedback)

        assertEquals(Duration.ZERO.toMillis(), newState.stageDuration.toMillis())
    }

    @Test
    fun `updateWith doesnt increase duration if speed is UNDERSPEED`() {
        val feedback =
            feedbackWithSpeed(SpeedFeedback.UNDERSPEED)
        val newState = initialState.updateWith(feedback)

        assertEquals(Duration.ZERO.toMillis(), newState.stageDuration.toMillis())
    }

    @Test
    fun `updateWith increases duration if speed is CORRECT`() {
        TrustedClock.setFixedDate()
        val feedback =
            feedbackWithSpeed(SpeedFeedback.CORRECT)

        var newState = initialState.updateWith(feedback)
        assertEquals(Duration.ZERO.toMillis(), newState.stageDuration.toMillis())
        assertEquals(TrustedClock.utcClock.millis(), newState.lastUpdateTimestamp)

        val oneSecondLater = 1L
        TrustedClock.advanceTimeBy(oneSecondLater, ChronoUnit.SECONDS)
        newState = newState.updateWith(feedback)
        assertEquals(TimeUnit.SECONDS.toMillis(oneSecondLater), newState.stageDuration.toMillis())
        assertEquals(TrustedClock.utcClock.millis(), newState.lastUpdateTimestamp)
    }

    @Test
    fun `updateWith advances to next stage if we receive CORRECT feedback for 10 consecutive seconds`() {
        TrustedClock.setFixedDate()

        var newState = advanceToNextStage(initialState)
        assertEquals(Duration.ZERO.toMillis(), newState.stageDuration.toMillis())
        assertEquals(false, newState.isCompleted())
        assertEquals(Stage.CHEWING_MOLARS, newState.currentStage)

        newState = advanceToNextStage(newState)
        assertEquals(Duration.ZERO.toMillis(), newState.stageDuration.toMillis())
        assertEquals(false, newState.isCompleted())
        assertEquals(Stage.FRONT_INCISORS, newState.currentStage)

        newState = advanceToNextStage(newState)
        assertEquals(Duration.ZERO.toMillis(), newState.stageDuration.toMillis())
        assertEquals(true, newState.isCompleted())
        assertEquals(Stage.COMPLETED, newState.currentStage)
    }

    @Test
    fun `updateWith with completion stage marks itself as completed`() {
        TrustedClock.setFixedDate()

        val completionStage = advanceToCompletion()

        assertEquals(true, completionStage.isCompleted())
        assertEquals(Stage.COMPLETED, completionStage.currentStage)
    }
}
