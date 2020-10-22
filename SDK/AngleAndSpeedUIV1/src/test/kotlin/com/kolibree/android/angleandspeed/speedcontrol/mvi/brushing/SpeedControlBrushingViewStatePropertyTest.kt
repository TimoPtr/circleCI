/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import com.kolibree.android.angleandspeed.boolean
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.lostConnectionState
import com.kolibree.android.angleandspeed.speedFeedback
import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing.SpeedControlBrushingViewState.Companion.STAGE_DURATION
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import io.kotlintest.matchers.beGreaterThan
import io.kotlintest.matchers.beLessThanOrEqualTo
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.random.Random
import org.threeten.bp.Duration

internal class SpeedControlBrushingViewStatePropertyTest : StringSpec({

    "remainingTimeSeconds is between 0s and 30s, with 10s per each stage, and includes stage duration" {
        assertAll(stageDuration(), stage()) { duration, currentStage ->
            val state = SpeedControlBrushingViewState(
                stageDuration = Duration.ofMillis(duration),
                currentStage = currentStage
            )
            when (currentStage) {
                Stage.OUTER_MOLARS -> {
                    state.remainingTime shouldBe beGreaterThan(Duration.ofSeconds(20))
                    state.remainingTime shouldBe beLessThanOrEqualTo(Duration.ofSeconds(30))
                    state.remainingTime shouldBe Duration.ofSeconds(30).minus(state.stageDuration)
                }
                Stage.CHEWING_MOLARS -> {
                    state.remainingTime shouldBe beGreaterThan(Duration.ofSeconds(10))
                    state.remainingTime shouldBe beLessThanOrEqualTo(Duration.ofSeconds(20))
                    state.remainingTime shouldBe Duration.ofSeconds(20).minus(state.stageDuration)
                }
                Stage.FRONT_INCISORS -> {
                    state.remainingTime shouldBe beGreaterThan(Duration.ofSeconds(0))
                    state.remainingTime shouldBe beLessThanOrEqualTo(Duration.ofSeconds(10))
                    state.remainingTime shouldBe Duration.ofSeconds(10).minus(state.stageDuration)
                }
                Stage.COMPLETED -> state.remainingTime == Duration.ZERO
            }
        }
    }

    "progressState should be properly calculated" {
        assertAll(
            stage(),
            stage(),
            boolean(),
            boolean(),
            speedFeedback(),
            lostConnectionState()
        ) { currentStage, previousStage, vibrationOn, isAnimationAllowed, speedFeedback, lostConnectionState ->
            val state = SpeedControlBrushingViewState(
                currentStage = currentStage,
                previousStage = previousStage,
                vibrationOn = vibrationOn,
                speedFeedback = speedFeedback,
                lostConnectionState = lostConnectionState,
                isAnimationAllowed = isAnimationAllowed
            )

            when {
                previousStage != currentStage -> state.progressState shouldBe ProgressState.RESET
                lostConnectionState != CONNECTION_ACTIVE -> state.progressState shouldBe ProgressState.PAUSE
                vibrationOn.not() -> state.progressState shouldBe ProgressState.PAUSE
                isAnimationAllowed.not() -> state.progressState shouldBe ProgressState.PAUSE
                speedFeedback != SpeedFeedback.CORRECT -> state.progressState shouldBe ProgressState.PAUSE
                else -> state.progressState shouldBe ProgressState.START
            }
        }
    }
})

private fun stageDuration(min: Long = 0L, max: Long = STAGE_DURATION.toMillis() - 1): Gen<Long> =
    object : Gen<Long> {

        override fun constants(): Iterable<Long> = listOf(min, max)

        override fun random(): Sequence<Long> = generateSequence {
            min + Random.nextLong(max - min + 1)
        }
    }

private fun stage(
    min: Stage = Stage.OUTER_MOLARS,
    max: Stage = Stage.COMPLETED
): Gen<Stage> =
    object : Gen<Stage> {

        override fun constants(): Iterable<Stage> = Stage.values().toList()

        override fun random(): Sequence<Stage> = generateSequence {
            var stage = min
            repeat((min.completedStages..Random.nextInt(max.completedStages - min.completedStages + 1)).count()) {
                stage = stage.nextStage()
            }
            return@generateSequence stage
        }
    }
