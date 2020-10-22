/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import com.kolibree.android.angleandspeed.angleDegrees
import com.kolibree.android.angleandspeed.boolean
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.lostConnectionState
import com.kolibree.android.angleandspeed.testangles.model.ToothSide
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.random.Random

class TestAnglesBrushingViewStatePropertyTest : StringSpec({

    "durationPercentage is between 0 and 100, with 1% for each 0.02s duration" {
        assertAll(duration()) { duration ->
            val state = TestAnglesBrushingViewState(duration = duration)
            when {
                duration < 0 -> state.durationPercentage shouldBe 0
                duration > 20000 -> state.durationPercentage shouldBe 100
                else -> state.durationPercentage shouldBe duration * 100 / 20000
            }
        }
    }

    "toothSide is RIGHT for angles.roll >= 0" {
        assertAll(angleDegrees()) { angleDegrees ->
            val feedback =
                AngleAndSpeedFeedback(
                    angleDegrees,
                    SpeedFeedback.CORRECT,
                    true
                )
            val state = TestAnglesBrushingViewState().updateWith(feedback)
            when {
                angleDegrees.roll < 0 -> state.toothSide shouldBe ToothSide.LEFT
                else -> state.toothSide shouldBe ToothSide.RIGHT
            }
        }
    }

    "progressState should be properly calculated" {
        assertAll(
            boolean(),
            boolean(),
            boolean(),
            lostConnectionState()
        ) { vibrationOn, isZoneCorrect, isAnimationAllowed, lostConnectionState ->
            val state = TestAnglesBrushingViewState(
                vibrationOn = vibrationOn,
                isZoneCorrect = isZoneCorrect,
                isAnimationAllowed = isAnimationAllowed,
                lostConnectionState = lostConnectionState
            )

            when {
                lostConnectionState != LostConnectionHandler.State.CONNECTION_ACTIVE -> state.progressState shouldBe ProgressState.PAUSE
                vibrationOn.not() -> state.progressState shouldBe ProgressState.PAUSE
                isZoneCorrect.not() -> state.progressState shouldBe ProgressState.PAUSE
                isAnimationAllowed.not() -> state.progressState shouldBe ProgressState.PAUSE
                else -> state.progressState shouldBe ProgressState.START
            }
        }
    }
})

fun duration(min: Int = -1, max: Int = 20010): Gen<Int> = object : Gen<Int> {

    override fun constants(): Iterable<Int> = listOf(min, max)

    override fun random(): Sequence<Int> = generateSequence {
        min + Random.nextInt(max - min)
    }
}
