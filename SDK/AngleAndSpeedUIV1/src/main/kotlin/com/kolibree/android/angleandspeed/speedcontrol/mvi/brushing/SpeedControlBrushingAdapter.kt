/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import android.widget.ImageView
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback

@Keep
@BindingAdapter(value = ["speedFeedback"])
fun ImageView.bindSpeed(speedFeedback: SpeedFeedback?) {
    if (speedFeedback == null) {
        return
    }

    var animator = tag
    if (tag == null) {
        animator = SpeedMeterAnimator(this)
        animator.init()
        tag = animator
    }

    if (animator is SpeedMeterAnimator) {
        when (speedFeedback) {
            SpeedFeedback.CORRECT -> animator.showNormalSpeed()
            SpeedFeedback.OVERSPEED -> animator.showHighSpeed()
            SpeedFeedback.UNDERSPEED -> animator.showLowSpeed()
        }
    }
}
