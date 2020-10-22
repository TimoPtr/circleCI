/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.ongoing

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.game.mvi.BaseGameViewState
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.testbrushing.R
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class OngoingBrushingViewState(
    val turnOffToothbrushMessageVisible: Boolean = false,
    val pauseScreenVisible: Boolean = false,
    override val lostConnectionState: LostConnectionHandler.State? = null,
    val brushingAnimation: LottieDelayedLoop
) : BaseGameViewState {

    fun withPausedAnimations() = copy(
        brushingAnimation = brushingAnimation.copy(isPlaying = false)
    )

    fun withResumedAnimations() = copy(
        brushingAnimation = brushingAnimation.copy(isPlaying = true)
    )

    @VisibleForApp
    companion object {
        fun initial() = OngoingBrushingViewState(
            brushingAnimation = LottieDelayedLoop(
                rawRes = R.raw.animation_brushing,
                loopStartFrameRes = R.integer.test_brushing_animation_start_frame,
                loopEndFrameRes = R.integer.test_brushing_animation_end_frame
            )
        )
    }
}
