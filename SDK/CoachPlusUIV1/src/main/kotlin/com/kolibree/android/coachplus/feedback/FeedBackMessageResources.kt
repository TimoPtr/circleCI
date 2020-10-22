/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.kolibree.android.coachplus.R

internal enum class FeedBackMessageResources(
    @StringRes val message: Int,
    @RawRes val imageId: Int
) {
    EmptyFeedback(
        message = R.string.empty,
        imageId = R.raw.wrong_zone_detected
    ),
    OutOfMouthFeedback(
        message = R.string.coach_plus_feedback_message_out_of_mouth,
        imageId = R.raw.animated_gif_out_of_mouth
    ),
    WrongZoneFeedback(
        message = R.string.mouth_zone_wrong,
        imageId = R.raw.wrong_zone_detected
    ),
    RinseBrushHeadFeedback(
        message = R.string.coach_plus_feedback_message_rinse_brush_head,
        imageId = R.raw.animated_gif_rinse_brush_head
    ),
    WrongHandleFeedback(
        message = R.string.coach_plus_feedback_message_wrong_handle,
        imageId = R.raw.animated_gif_plaqless
    ),
    OverpressureFeedback(
        message = R.string.coach_plus_feedback_message_overpressure,
        imageId = R.raw.animated_gif_overpressure
    ),
    WrongIncisorsIntAngleFeedback(
        message = R.string.coach_plus_feedback_message_wrong_angle_incisors_interior,
        imageId = R.raw.animated_gif_incisors
    ),
    Wrong45AngleFeedback(
        message = R.string.coach_plus_feedback_message_wrong_angle_other_zones,
        imageId = R.raw.animated_gif_molars
    ),
    UnderSpeedFeedback(
        message = R.string.coach_plus_feedback_message_speed_slow,
        imageId = R.raw.animated_gif_speed_slow
    ),
    OverSpeedFeedback(
        message = R.string.coach_plus_feedback_message_speed_fast,
        imageId = R.raw.animated_gif_speed
    );

    companion object {

        fun from(message: FeedBackMessage): FeedBackMessageResources = when (message) {
            FeedBackMessage.EmptyFeedback -> EmptyFeedback
            FeedBackMessage.OutOfMouthFeedback -> OutOfMouthFeedback
            FeedBackMessage.WrongZoneFeedback -> WrongZoneFeedback
            FeedBackMessage.RinseBrushHeadFeedback -> RinseBrushHeadFeedback
            FeedBackMessage.WrongHandleFeedback -> WrongHandleFeedback
            FeedBackMessage.WrongIncisorsIntAngleFeedback -> WrongIncisorsIntAngleFeedback
            FeedBackMessage.Wrong45AngleFeedback -> Wrong45AngleFeedback
            FeedBackMessage.UnderSpeedFeedback -> UnderSpeedFeedback
            FeedBackMessage.OverSpeedFeedback -> OverSpeedFeedback
            FeedBackMessage.OverpressureFeedback -> OverpressureFeedback
        }
    }
}
