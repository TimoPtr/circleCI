/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import androidx.annotation.FloatRange
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.ui.mindyourspeed.MindYouSpeedConstants.STAGE_DURATION
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.mvi.BaseGameViewState
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.kml.MouthZone16
import kotlin.math.max
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Duration
import org.threeten.bp.Instant

// Zone mapping is defined in
// https://kolibree.atlassian.net/wiki/spaces/PROD/pages/312770589/Activities+Hum#Brushing-zones.1
internal enum class Stage(val index: Int, val prescribedZones: Array<MouthZone16>) {
    WAITING_FOR_START(-1, MouthZone16.values()),
    STAGE_1(0, arrayOf(MouthZone16.UpMolRiExt)),
    STAGE_2(1, arrayOf(MouthZone16.LoMolRiInt)),
    STAGE_3(2, arrayOf(MouthZone16.LoMolLeInt)),
    COMPLETED(3, emptyArray());

    fun nextStage(): Stage = when (this) {
        WAITING_FOR_START -> STAGE_1
        STAGE_1 -> STAGE_2
        STAGE_2 -> STAGE_3
        else -> COMPLETED
    }

    companion object {

        const val NUMBER_OF_ONGOING_STAGES = 3
    }
}

@Parcelize
internal data class MindYourSpeedViewState(
    override val lostConnectionState: LostConnectionHandler.State? = null,
    val disableSpeedometerOnWrongZone: Boolean = true,
    val isPaused: Boolean = false,
    val stage: Stage = Stage.WAITING_FOR_START,
    val speedFeedback: SpeedFeedback? = null,
    val lastUpdateTime: Instant = TrustedClock.getNowInstant(),
    val zoneProgressData: ZoneProgressData = ZoneProgressData.create(Stage.NUMBER_OF_ONGOING_STAGES),
    val feedbackMessage: MindYourSpeedFeedback = MindYourSpeedFeedback.EMPTY_FEEDBACK
) : BaseGameViewState {

    @IgnoredOnParcel
    val isWaitingForStart = stage == Stage.WAITING_FOR_START

    @IgnoredOnParcel
    val isFinished = stage == Stage.COMPLETED

    @IgnoredOnParcel
    val enableSpeedometer = !disableSpeedometerOnWrongZone ||
        isWaitingForStart ||
        (!isPaused && feedbackMessage != MindYourSpeedFeedback.WRONG_ZONE)

    fun withProgressReset() = copy(
        stage = Stage.STAGE_1,
        zoneProgressData = ZoneProgressData.create(Stage.NUMBER_OF_ONGOING_STAGES),
        lastUpdateTime = TrustedClock.getNowInstant()
    )

    fun withPausedState() = copy(
        stage = if (stage == Stage.WAITING_FOR_START) stage.nextStage() else stage,
        isPaused = true,
        lastUpdateTime = TrustedClock.getNowInstant()
    )

    fun withUnpausedState() = copy(
        stage = if (stage == Stage.WAITING_FOR_START) stage.nextStage() else stage,
        isPaused = false,
        lastUpdateTime = TrustedClock.getNowInstant()
    )

    fun updateWith(newFeedback: AngleAndSpeedFeedback): MindYourSpeedViewState {
        if (stage == Stage.COMPLETED || isPaused) return this

        val now = TrustedClock.getNowInstant()

        val currentStageProgress =
            if (stage == Stage.WAITING_FOR_START) 1f
            else zoneProgressData.zones[stage.index].progress +
                calculateProgressDelta(newFeedback, lastUpdateTime, now)

        val canAdvanceToNextStage = currentStageProgress >= 1f

        return if (canAdvanceToNextStage) {
            updateForTheNextStage(newFeedback, now)
        } else {
            updateForTheSameStage(currentStageProgress, newFeedback, now)
        }
    }

    private fun updateForTheNextStage(
        feedback: AngleAndSpeedFeedback,
        updateTime: Instant
    ): MindYourSpeedViewState {
        val nextStage = stage.nextStage()
        val updatedProgress = zoneProgressData.updateProgressOnZone(stage.index, 1f)
        return copy(
            stage = nextStage,
            zoneProgressData =
            if (nextStage == Stage.COMPLETED) updatedProgress.brushingFinished() else updatedProgress,
            speedFeedback = feedback.speedFeedback,
            feedbackMessage = getFeedbackMessage(feedback),
            lastUpdateTime = updateTime
        )
    }

    private fun updateForTheSameStage(
        progress: Float,
        feedback: AngleAndSpeedFeedback,
        updateTime: Instant
    ): MindYourSpeedViewState = copy(
        speedFeedback = feedback.speedFeedback,
        zoneProgressData = zoneProgressData.updateProgressOnZone(stage.index, progress),
        lastUpdateTime = updateTime,
        feedbackMessage = getFeedbackMessage(feedback)
    )

    @FloatRange(from = 0.0, to = 1.0)
    private fun calculateProgressDelta(
        feedback: AngleAndSpeedFeedback,
        lastUpdateTime: Instant,
        currentTime: Instant
    ): Float {
        val delta = max(0, Duration.between(lastUpdateTime, currentTime).toMillis())
        return if (feedback.isZoneCorrect && feedback.speedFeedback == SpeedFeedback.CORRECT)
            delta.toFloat() / STAGE_DURATION.toMillis() else 0f
    }

    private fun getFeedbackMessage(feedback: AngleAndSpeedFeedback): MindYourSpeedFeedback = when {
        !feedback.isZoneCorrect -> MindYourSpeedFeedback.WRONG_ZONE
        feedback.speedFeedback == SpeedFeedback.OVERSPEED -> MindYourSpeedFeedback.TOO_FAST
        feedback.speedFeedback == SpeedFeedback.UNDERSPEED -> MindYourSpeedFeedback.TOO_SLOW
        else -> MindYourSpeedFeedback.EMPTY_FEEDBACK
    }

    companion object {

        fun initial(disableSpeedometerOnWrongZone: Boolean = true) =
            MindYourSpeedViewState(disableSpeedometerOnWrongZone = disableSpeedometerOnWrongZone)
    }
}
