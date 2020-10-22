/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.testangles.model.ToothSide
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.mvi.BaseGameViewState
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import kotlin.math.max
import kotlin.math.min
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Duration

private const val TARGET_DURATION_PERCENTAGE = 100
private const val FEEDBACK_LAG = 500 // The interval between each data update is about 500ms

@Parcelize
internal data class TestAnglesBrushingViewState(
    val angleDegrees: AngleFeedback? = null,
    val toothSide: ToothSide? = null,
    val isZoneCorrect: Boolean = false,
    override val lostConnectionState: LostConnectionHandler.State? = null,
    val vibrationOn: Boolean? = null,
    @VisibleForTesting val duration: Int = 0,
    @VisibleForTesting val lastUpdateTimestamp: Long? = null,
    val isAnimationAllowed: Boolean = true
) : BaseGameViewState {

    val durationPercentage: Int
        get() = max(
            0,
            min(TARGET_DURATION_PERCENTAGE, (duration * 100f / TARGET_DURATION.toMillis()).toInt())
        )

    val progressState: ProgressState
        get() = when {
            isZoneCorrect &&
                isAnimationAllowed &&
                vibrationOn == true &&
                lostConnectionState == CONNECTION_ACTIVE -> ProgressState.START
            else -> ProgressState.PAUSE
        }

    @CallSuper
    fun updateWith(newFeedback: AngleAndSpeedFeedback): TestAnglesBrushingViewState {
        val now = TrustedClock.utcClock.millis()
        val duration =
            if (newFeedback.isZoneCorrect && lastUpdateTimestamp != null)
                max(0, now.minus(lastUpdateTimestamp))
            else 0

        return copy(
            duration = this.duration + duration.toInt(),
            angleDegrees = newFeedback.angleDegrees,
            toothSide = if (newFeedback.angleDegrees.roll >= 0) ToothSide.RIGHT else ToothSide.LEFT,
            isZoneCorrect = newFeedback.isZoneCorrect,
            lastUpdateTimestamp = if (newFeedback.isZoneCorrect) now else null
        )
    }

    // Reduce the lag because the last feedback was later than the end of the progress.
    fun shouldMoveToTheNextStage() = duration >= TARGET_DURATION.toMillis().minus(FEEDBACK_LAG)

    companion object {

        fun initial() = TestAnglesBrushingViewState()

        @JvmStatic
        val TARGET_DURATION: Duration = Duration.ofSeconds(20)
    }
}
