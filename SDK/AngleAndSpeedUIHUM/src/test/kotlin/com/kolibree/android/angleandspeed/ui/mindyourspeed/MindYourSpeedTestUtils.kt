/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback

internal fun Float.toPercentInt(): Int = (this * 100).toInt()

internal fun feedback(
    speedFeedback: SpeedFeedback = SpeedFeedback.CORRECT,
    isZoneCorrect: Boolean = true
): AngleAndSpeedFeedback {
    return AngleAndSpeedFeedback(
        angleDegrees = AngleFeedback(0f, 0f, 0f),
        speedFeedback = speedFeedback,
        isZoneCorrect = isZoneCorrect
    )
}
