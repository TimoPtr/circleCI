/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.feedback

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.app.feedback.FeedbackMessageResource
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.guidedbrushing.R

internal enum class GuidedBrushingFeedBackMessageResources(
    @StringRes val message: Int,
    @DrawableRes val imageId: Int
) {
    EmptyFeedback(
        message = R.string.guided_brushing_no_feedback,
        imageId = R.drawable.ic_feedback_all_good
    ),
    WrongZoneFeedback(
        message = R.string.guided_brushing_wrong_zone_feedback,
        imageId = R.drawable.ic_feedback_wrong_zone
    ),
    WrongIncisorsIntAngleFeedback(
        message = R.string.guided_brushing_wrong_angle_feedback,
        imageId = R.drawable.ic_feedback_wrong_angle
    ),
    Wrong45AngleFeedback(
        message = R.string.guided_brushing_wrong_angle_feedback,
        imageId = R.drawable.ic_feedback_wrong_angle
    ),
    UnderSpeedFeedback(
        message = R.string.guided_brushing_too_slow_feedback,
        imageId = R.drawable.ic_feedback_too_slow
    ),
    OverSpeedFeedback(
        message = R.string.guided_brushing_too_fast_feedback,
        imageId = R.drawable.ic_feedback_too_fast
    );

    companion object {

        @JvmStatic
        fun from(message: FeedBackMessage): FeedbackMessageResource = when (message) {
            is FeedBackMessage.EmptyFeedback -> create(message, EmptyFeedback)
            is FeedBackMessage.WrongZoneFeedback -> create(message, WrongZoneFeedback)
            is FeedBackMessage.WrongIncisorsIntAngleFeedback -> create(message, WrongIncisorsIntAngleFeedback)
            is FeedBackMessage.Wrong45AngleFeedback -> create(message, Wrong45AngleFeedback)
            is FeedBackMessage.UnderSpeedFeedback -> create(message, UnderSpeedFeedback)
            is FeedBackMessage.OverSpeedFeedback -> create(message, OverSpeedFeedback)
            else -> {
                FailEarly.fail(
                    "Message $message is not supported by guided brushing - " +
                        "maybe you tried to use plaqless?"
                )
                create(FeedBackMessage.EmptyFeedback, EmptyFeedback)
            }
        }

        private fun create(
            message: FeedBackMessage,
            feedback: GuidedBrushingFeedBackMessageResources
        ) = FeedbackMessageResource(
            shouldShow = message.shouldShow,
            imageId = feedback.imageId,
            message = feedback.message
        )
    }
}
