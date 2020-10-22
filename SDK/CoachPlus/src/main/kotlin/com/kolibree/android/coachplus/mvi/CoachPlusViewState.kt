/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl.Companion.SEQUENCE
import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.coachplus.controller.CoachPlusControllerResult
import com.kolibree.android.coachplus.controller.kml.CoachPlusKmlControllerImpl
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.game.mvi.BaseGameViewState
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.kml.MouthZone16
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CoachPlusViewState(
    val isManual: Boolean,
    val isInit: Boolean = false,
    val isPlaying: Boolean = false,
    val isEnd: Boolean = false,
    val isBrushingMovementEnabled: Boolean = false,
    val isHelpTextEnabled: Boolean = false,
    val currentZone: MouthZone16? = null,
    val currentZoneProgress: Int = 0,
    val isBrushingGoodZone: Boolean = false,
    val feedBackMessage: FeedBackMessage = FeedBackMessage.EmptyFeedback,
    val outOfMouth: Boolean = false,
    override val lostConnectionState: LostConnectionHandler.State? = null,
    @ColorInt val ringLedColor: Int = Color.BLUE,
    val zoneProgressData: ZoneProgressData = guidedBrushingZones()
) : BaseGameViewState {

    val shouldShowToothbrushHead: Boolean
        get() = isPlaying && isBrushingMovementEnabled && !outOfMouth

    // We don't show the pause screen when the feedback warning level is critical
    val shouldShowPause: Boolean
        get() = isInit &&
            !isPlaying &&
            !isEnd &&
            !outOfMouth

    val optionalFeedback: FeedBackMessage?
        get() = takeIf { isPlaying }?.feedBackMessage

    companion object {
        fun initial(isManual: Boolean) =
            CoachPlusViewState(isManual)
    }

    internal fun updateWith(result: CoachPlusControllerResult) =
        copy(
            currentZone = result.zoneToBrush,
            currentZoneProgress = result.completionPercent,
            isBrushingGoodZone = result.brushingGoodZone,
            feedBackMessage = result.feedBackMessage,
            // only possible when not in pause
            outOfMouth = isPlaying && result.feedBackMessage == FeedBackMessage.OutOfMouthFeedback,
            zoneProgressData = updateZoneProgress(result)
        )

    @VisibleForTesting
    internal fun updateZoneProgress(result: CoachPlusControllerResult): ZoneProgressData {
        val zoneIndex = SEQUENCE.indexOfFirst { it == result.zoneToBrush }
        val progress = result.completionPercent / 100f
        return zoneProgressData.updateProgressOnZone(zoneIndex, progress)
    }

    @StringRes
    internal fun getHint(controller: CoachPlusController, zoneHintProvider: ZoneHintProvider): Int? = when {
        feedBackMessage.shouldShow -> null
        !isHelpTextEnabled -> null
        isBrushingGoodZone -> currentZone?.let { zoneHintProvider.provideHintForZone(it) }
        controller is CoachPlusKmlControllerImpl -> null
        else -> zoneHintProvider.provideHintForWrongZone()
    }
}

internal fun guidedBrushingZones() = ZoneProgressData.create(MouthZone16.values().size)
