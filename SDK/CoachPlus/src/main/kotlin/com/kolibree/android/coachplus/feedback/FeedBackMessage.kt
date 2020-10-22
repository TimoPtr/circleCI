/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.SpeedKPI
import kotlinx.android.parcel.Parcelize

@Keep
sealed class FeedBackMessage(
    val priorityLevel: Int,
    val shouldShow: Boolean,
    val warningLevel: FeedbackWarningLevel
) : Parcelable {

    @Parcelize
    @Keep
    object EmptyFeedback : FeedBackMessage(
        priorityLevel = Int.MAX_VALUE,
        shouldShow = false,
        warningLevel = FeedbackWarningLevel.None
    )

    @Parcelize
    @Keep
    object OutOfMouthFeedback : FeedBackMessage(
        priorityLevel = 0,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Critical
    )

    @Parcelize
    @Keep
    object WrongZoneFeedback : FeedBackMessage(
        priorityLevel = 1,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Normal
    )

    @Parcelize
    @Keep
    object RinseBrushHeadFeedback : FeedBackMessage(
        priorityLevel = 2,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Critical
    )

    @Parcelize
    @Keep
    object WrongHandleFeedback : FeedBackMessage(
        priorityLevel = 3,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Severe
    )

    @Parcelize
    @Keep
    object OverpressureFeedback : FeedBackMessage(
        priorityLevel = 3,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Severe
    )

    @Parcelize
    @Keep
    object WrongIncisorsIntAngleFeedback : FeedBackMessage(
        priorityLevel = 4,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Normal
    )

    @Parcelize
    @Keep
    object Wrong45AngleFeedback : FeedBackMessage(
        priorityLevel = 4,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Normal
    )

    @Parcelize
    @Keep
    object UnderSpeedFeedback : FeedBackMessage(
        priorityLevel = 5,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Normal
    )

    @Parcelize
    @Keep
    object OverSpeedFeedback : FeedBackMessage(
        priorityLevel = 5,
        shouldShow = true,
        warningLevel = FeedbackWarningLevel.Normal
    )
}

@VisibleForTesting
val incisorsIntZone = arrayListOf(
    MouthZone16.UpIncInt,
    MouthZone16.LoIncInt
)

@VisibleForTesting
val wrongAngle45PossibleZone = arrayListOf(
    MouthZone16.LoMolLeExt,
    MouthZone16.LoMolLeInt,
    MouthZone16.LoMolRiExt,
    MouthZone16.LoMolRiInt,
    MouthZone16.LoIncExt,
    MouthZone16.UpMolLeExt,
    MouthZone16.UpMolLeInt,
    MouthZone16.UpMolRiExt,
    MouthZone16.UpMolRiInt,
    MouthZone16.UpIncExt
)

internal fun getAngleFeedback(isOrientationCorrect: Boolean, currentZone: MouthZone16): FeedBackMessage? = when {
    !isOrientationCorrect && incisorsIntZone.contains(currentZone) -> FeedBackMessage.WrongIncisorsIntAngleFeedback
    !isOrientationCorrect && wrongAngle45PossibleZone.contains(currentZone) -> FeedBackMessage.Wrong45AngleFeedback
    else -> null
}

internal fun getSpeedFeedback(speedKPI: SpeedKPI): FeedBackMessage? = when (speedKPI) {
    SpeedKPI.Overspeed -> FeedBackMessage.OverSpeedFeedback
    SpeedKPI.Underspeed -> FeedBackMessage.UnderSpeedFeedback
    else -> null
}

internal fun getPlaqlessFeedback(plaqlessError: PlaqlessError): FeedBackMessage? = when (plaqlessError) {
    PlaqlessError.OUT_OF_MOUTH -> FeedBackMessage.OutOfMouthFeedback
    PlaqlessError.WRONG_HANDLE -> FeedBackMessage.WrongHandleFeedback
    PlaqlessError.RINSE_BRUSH_HEAD -> FeedBackMessage.RinseBrushHeadFeedback
    else -> null
}

/** [FeedBackMessage]'s warning level */
@Keep
enum class FeedbackWarningLevel {
    /**
     * The feedback is not a warning feedback
     */
    None,

    /**
     * The user will be warned about this feedback
     */
    Normal,

    /**
     * The user will be actively warned about this urgent feedback
     */
    Severe,

    /**
     * The user will be actively warned about this urgent feedback
     *
     * This warning level requires the activity to be paused
     */
    Critical
}
