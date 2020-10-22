/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pulsingdot

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.kolibree.android.homeui.hum.R

/**
 * Handler for Pulsing Dot which integrate a LottieAnimationView
 *
 * We cannot use ViewStub because this class is already final, instead we are inflating the animation
 * when needed into a FrameLayout
 */
internal class PulsingDotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LottieAnimationView(context, attrs, defStyleAttr) {

    init {
        repeatCount = ValueAnimator.INFINITE
        setAnimation(R.raw.pulsing_dot)
        playAnimation()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        playAnimation()
    }
}
