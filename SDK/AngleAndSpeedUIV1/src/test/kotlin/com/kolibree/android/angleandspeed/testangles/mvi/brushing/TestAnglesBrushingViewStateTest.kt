/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.testangles.model.ToothSide
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedEpochInstant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestAnglesBrushingViewStateTest : BaseUnitTest() {

    private val initialState = TestAnglesBrushingViewState()

    private lateinit var stateAfterFirstUpdate: TestAnglesBrushingViewState

    override fun setup() {
        super.setup()
        assertEquals(0, initialState.duration)
        assertEquals(0, initialState.durationPercentage)
        assertEquals(null, initialState.lastUpdateTimestamp)

        val feedbackChunk =
            AngleAndSpeedFeedback(
                AngleFeedback(
                    0.0f,
                    0.0f,
                    0.0f
                ), SpeedFeedback.CORRECT, true
            )

        TrustedClock.setFixedEpochInstant(TrustedClock.utcClock.millis())

        stateAfterFirstUpdate = initialState.updateWith(feedbackChunk)
    }

    @Test
    fun `duration and durationPercentage are incremented based on time flow`() {
        assertEquals(0, stateAfterFirstUpdate.duration)
        assertEquals(0, stateAfterFirstUpdate.durationPercentage)
        assertEquals(TrustedClock.utcClock.millis(), stateAfterFirstUpdate.lastUpdateTimestamp)

        val feedbackChunk =
            AngleAndSpeedFeedback(
                AngleFeedback(
                    0.0f,
                    0.0f,
                    0.0f
                ), SpeedFeedback.CORRECT, true
            )

        TrustedClock.setFixedEpochInstant(TrustedClock.utcClock.millis() + 5000)
        val stateAfterSecondUpdate = stateAfterFirstUpdate.updateWith(feedbackChunk)

        assertTrue(stateAfterSecondUpdate.duration >= 5000)
        assertEquals(25, stateAfterSecondUpdate.durationPercentage)
        assertEquals(TrustedClock.utcClock.millis(), stateAfterSecondUpdate.lastUpdateTimestamp)
    }

    @Test
    fun `duration and durationPercentage are not incremented if zone is incorrect`() {
        TrustedClock.setFixedEpochInstant(TrustedClock.utcClock.millis() + 5000)

        val feedbackChunk =
            AngleAndSpeedFeedback(
                AngleFeedback(
                    1.0f,
                    2.0f,
                    5.0f
                ),
                SpeedFeedback.CORRECT,
                false
            )
        val stateAfterSecondUpdate = stateAfterFirstUpdate.updateWith(feedbackChunk)

        assertEquals(0, stateAfterSecondUpdate.duration)
        assertEquals(0, stateAfterSecondUpdate.durationPercentage)
        assertEquals(null, stateAfterSecondUpdate.lastUpdateTimestamp)
    }

    @Test
    fun `angles are updated even if zone is incorrect`() {
        val angles = AngleFeedback(1.0f, 2.0f, 5.0f)
        val feedbackChunk =
            AngleAndSpeedFeedback(
                angles,
                SpeedFeedback.CORRECT,
                false
            )
        val stateAfterSecondUpdate = stateAfterFirstUpdate.updateWith(feedbackChunk)

        assertEquals(angles, stateAfterSecondUpdate.angleDegrees)
        assertEquals(ToothSide.RIGHT, stateAfterSecondUpdate.toothSide)
        assertEquals(false, stateAfterSecondUpdate.isZoneCorrect)
    }

    @Test
    fun `shouldMoveToTheNextStage return true when duration is larger then TARGET_DURATION(20s) - LAG(500ms)`() {
        val viewState = TestAnglesBrushingViewState(duration = 19501)
        assert(viewState.shouldMoveToTheNextStage())
    }
}
