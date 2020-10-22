/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.transition.TransitionManager

/**
 * Simple binding to show/hide the view with alpha animation.
 * @param visible sets visibility to VISIBLE if true, INVISIBLE otherwise
 */
@Keep
@BindingAdapter("visibilityAnim")
fun View.setVisibleWithAnimation(visible: Boolean) {
    when {
        visible && visibility == View.VISIBLE -> alpha = 1f
        !visible && visibility != View.VISIBLE -> alpha = 0f
        visible && visibility != View.VISIBLE -> {
            visibility = View.VISIBLE
            animate().alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
        }
        !visible && visibility == View.VISIBLE -> {
            animate().alpha(0f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { visibility = View.INVISIBLE }
        }
    }
}

/**
 * Alternative binding to show/hide the view with alpha animation, handling GONE case.
 * @param targetVisibility sets visibility to VISIBLE, INVISIBLE or GONE with animation
 */
@Keep
@BindingAdapter("visibilityAnim")
fun View.setVisibilityWithAnimation(targetVisibility: Int) {
    when {
        targetVisibility == View.VISIBLE && visibility == View.VISIBLE -> alpha = 1f
        targetVisibility != View.VISIBLE && visibility != View.VISIBLE -> {
            alpha = 0f
            visibility = targetVisibility
        }
        targetVisibility == View.VISIBLE && visibility != View.VISIBLE -> {
            visibility = View.VISIBLE
            animate().alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
        }
        targetVisibility != View.VISIBLE && visibility == View.VISIBLE -> {
            animate().alpha(0f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { visibility = targetVisibility }
        }
    }
}

@Keep
@BindingAdapter("visibilityTransition")
fun View.setVisibilityWithTransition(newVisibility: Int) {
    if (visibility != newVisibility) {
        parent?.viewGroup()?.also { viewGroup ->
            TransitionManager.beginDelayedTransition(viewGroup)
        }
        visibility = newVisibility
    }
}

private fun ViewParent.viewGroup(): ViewGroup? =
    if (this is ViewGroup) this else parent?.viewGroup()
