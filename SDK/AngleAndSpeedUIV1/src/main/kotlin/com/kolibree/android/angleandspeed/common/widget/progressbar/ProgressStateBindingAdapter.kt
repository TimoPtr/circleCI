/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.widget.progressbar

import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import org.threeten.bp.Duration

/**
 * Binds current state of the progress to the progress bar, along with maximum duration.
 *
 * @param progressState state of the progress
 * @param progressMaxDuration amount of time needed for the progress to reach 100%
 * @see ProgressBar
 * @see ProgressState
 * @see Duration
 */
@Keep
@BindingAdapter("progressState", "progressMaxDuration")
fun ProgressBar.bindProgressState(progressState: ProgressState?, progressMaxDuration: Duration) {
    var animator = tag

    // init ProgressAnimator
    if (tag == null) {
        animator = ProgressAnimator(this, progressMaxDuration)
        animator.init()
        tag = animator
    }

    // control ProgressBar animation by State
    progressState?.let {
        if (animator is ProgressAnimator) {
            animator.changeAnimatorState(it)
        }
    }
}
