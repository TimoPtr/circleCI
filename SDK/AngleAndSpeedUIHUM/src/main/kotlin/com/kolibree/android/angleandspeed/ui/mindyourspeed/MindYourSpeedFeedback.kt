/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.angleandspeed.ui.R
import com.kolibree.android.app.feedback.FeedbackMessageResource

internal enum class MindYourSpeedFeedback(
    val shouldShow: Boolean,
    @StringRes val message: Int,
    @DrawableRes val imageId: Int
) {
    EMPTY_FEEDBACK(
        shouldShow = false,
        message = R.string.mind_your_speed_no_feedback,
        imageId = R.drawable.ic_feedback_all_good
    ),

    WRONG_ZONE(
        shouldShow = true,
        message = R.string.mind_your_speed_wrong_zone_feedback,
        imageId = R.drawable.ic_feedback_wrong_zone
    ),

    TOO_FAST(
        shouldShow = true,
        message = R.string.mind_your_speed_too_fast_feedback,
        imageId = R.drawable.ic_feedback_too_fast
    ),

    TOO_SLOW(
        shouldShow = true,
        message = R.string.mind_your_speed_too_slow_feedback,
        imageId = R.drawable.ic_feedback_too_slow
    );

    fun asResource() = FeedbackMessageResource(shouldShow, message, imageId)
}
