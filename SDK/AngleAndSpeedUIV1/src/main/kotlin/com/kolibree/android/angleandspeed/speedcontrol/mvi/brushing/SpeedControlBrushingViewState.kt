/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.mvi.BaseGameViewState
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import kotlin.math.max
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Duration

internal enum class Stage(val completedStages: Int) {
    OUTER_MOLARS(0),
    CHEWING_MOLARS(1),
    FRONT_INCISORS(2),
    COMPLETED(3);

    fun nextStage(): Stage = when (this) {
        OUTER_MOLARS -> CHEWING_MOLARS
        CHEWING_MOLARS -> FRONT_INCISORS
        else -> COMPLETED
    }
}

@Parcelize
internal data class SpeedControlBrushingViewState(
    val speedFeedback: SpeedFeedback? = null,
    @VisibleForTesting val currentStage: Stage = Stage.OUTER_MOLARS,
    private val previousStage: Stage = Stage.OUTER_MOLARS,
    @VisibleForTesting val stageDuration: Duration = Duration.ZERO,
    override val lostConnectionState: LostConnectionHandler.State? = null,
    val vibrationOn: Boolean? = null,
    @VisibleForTesting val lastUpdateTimestamp: Long? = null,
    val isAnimationAllowed: Boolean = true
) : BaseGameViewState {

    /*
     * Time = 30s (total game time) - 10s * number of completed stages - current stage duration (s)
     */
    val remainingTime: Duration
        get() = TOTAL_DURATION
            .minus(STAGE_DURATION.multipliedBy(currentStage.completedStages.toLong()))
            .minus(stageDuration)
            .let { if (it.isNegative) Duration.ZERO else it }

    val progressState: ProgressState
        get() = when {
            previousStage !== currentStage -> ProgressState.RESET
            speedFeedback == SpeedFeedback.CORRECT &&
                isAnimationAllowed &&
                vibrationOn == true &&
                lostConnectionState == CONNECTION_ACTIVE -> ProgressState.START
            else -> ProgressState.PAUSE
        }

    @CallSuper
    fun updateWith(newFeedback: AngleAndSpeedFeedback): SpeedControlBrushingViewState {
        val now = TrustedClock.utcClock.millis()
        val isSpeedCorrect = newFeedback.speedFeedback == SpeedFeedback.CORRECT

        val duration = this.stageDuration + calculateDurationDelta(
            isSpeedCorrect,
            lastUpdateTimestamp,
            now
        )

        val canAdvanceToNextStage = duration >= STAGE_DURATION

        return if (canAdvanceToNextStage) {
            val nextStage = currentStage.nextStage()
            val stageDuration =
                if (nextStage == Stage.COMPLETED) Duration.ZERO else duration.minus(STAGE_DURATION)
            copy(
                currentStage = nextStage,
                previousStage = currentStage,
                stageDuration = stageDuration,
                speedFeedback = newFeedback.speedFeedback,
                lastUpdateTimestamp = now
            )
        } else
            copy(
                previousStage = currentStage,
                stageDuration = duration,
                speedFeedback = newFeedback.speedFeedback,
                lastUpdateTimestamp = now
            )
    }

    private fun calculateDurationDelta(
        isSpeedCorrect: Boolean,
        lastUpdateTimestamp: Long?,
        currentTimeMillis: Long
    ): Duration {
        val delta = lastUpdateTimestamp?.let { max(0, currentTimeMillis.minus(it)) } ?: 0
        return if (isSpeedCorrect) Duration.ofMillis(delta) else Duration.ZERO
    }

    fun isCompleted(): Boolean = currentStage == Stage.COMPLETED

    companion object {

        fun initial() = SpeedControlBrushingViewState()

        @VisibleForTesting
        const val NUMBER_OF_STAGES = 3

        @JvmStatic
        @VisibleForTesting
        val STAGE_DURATION: Duration = Duration.ofSeconds(10)

        @JvmStatic
        @VisibleForTesting
        val TOTAL_DURATION: Duration = STAGE_DURATION.multipliedBy(NUMBER_OF_STAGES.toLong())
    }
}
