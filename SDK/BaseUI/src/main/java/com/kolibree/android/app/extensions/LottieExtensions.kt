/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.annotation.Keep
import com.airbnb.lottie.LottieAnimationView

@Keep
fun LottieAnimationView.onAnimationEnd(action: () -> Unit) {
    addAnimatorListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animator: Animator?) {
            removeAnimatorListener(this)
            action()
        }
    })
}
