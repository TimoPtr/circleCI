/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing.SpeedControlBrushingViewState.Companion.STAGE_DURATION
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.advanceTimeBy
import org.threeten.bp.temporal.ChronoUnit

internal fun advanceToCompletion(): SpeedControlBrushingViewState {
    var state = SpeedControlBrushingViewState.initial()
    repeat(Stage.values().count()) {
        state = advanceToNextStage(state)
    }
    return state
}

internal fun advanceToNextStage(state: SpeedControlBrushingViewState): SpeedControlBrushingViewState {
    val denominator = 10L
    val feedback =
        feedbackWithSpeed(SpeedFeedback.CORRECT)
    var newState = state.updateWith(feedback)
    repeat(0.until(STAGE_DURATION.toMillis() / denominator).count()) {
        TrustedClock.advanceTimeBy(denominator, ChronoUnit.MILLIS)
        newState = newState.updateWith(feedback)
    }
    return newState
}

internal fun feedbackWithSpeed(speed: SpeedFeedback) =
    AngleAndSpeedFeedback(
        angleDegrees = AngleFeedback(0.0f, 0.0f, 0.0f),
        speedFeedback = speed,
        isZoneCorrect = false
    )
