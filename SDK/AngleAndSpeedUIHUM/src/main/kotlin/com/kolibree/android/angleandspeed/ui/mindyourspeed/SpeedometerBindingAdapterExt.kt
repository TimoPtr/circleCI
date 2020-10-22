/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import androidx.databinding.BindingAdapter
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.app.widget.SpeedometerView

@BindingAdapter("speedFeedback")
internal fun SpeedometerView.bindSpeedFeedback(previous: SpeedFeedback?, current: SpeedFeedback?) {
    if (current == null || previous == current) return
    smoothPositionTo(
        when (current) {
            SpeedFeedback.UNDERSPEED -> 0f
            SpeedFeedback.OVERSPEED -> 100f
            else -> 50f
        }
    )
}
