/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.kolibree.android.homeui.hum.R

@Keep
@BindingAdapter("toolbarIcon")
fun AppCompatImageView.setToolbarIcon(toolbarIcon: ToolbarIcon) {
    val currentIcon = getTag(R.id.toothbrush_icon) as? ToolbarIcon

    runEndTransition(currentIcon, toolbarIcon)
}

private fun AppCompatImageView.runEndTransition(
    currentIcon: ToolbarIcon?,
    newIcon: ToolbarIcon
) {
    currentIcon?.takeIf { it.endTransition != 0 }?.endTransition?.also { endTransition ->
        startAnimation(endTransition) {
            showIcon(newIcon)
        }
    } ?: showIcon(newIcon)
}

private fun AppCompatImageView.showIcon(newIcon: ToolbarIcon) {
    setTag(R.id.toothbrush_icon, newIcon)
    if (newIcon.startTransition != 0) {
        startAnimation(newIcon.startTransition) {
            startAnimation(newIcon.mainIcon)
        }
    } else {
        startAnimation(newIcon.mainIcon)
    }
}

private fun AppCompatImageView.startAnimation(
    @DrawableRes drawableId: Int,
    endCallback: () -> Unit = {}
) {
    drawable?.toAnimatableCompat()?.run {
        clearAnimationCallbacks()
        stop()
    }
    setImageResource(drawableId)
    drawable?.toAnimatableCompat()?.let { animatable ->
        animatable.registerAnimationCallback(object : AnimatableCompat.AnimationCallback {
            override fun onAnimationEnd(drawable: Drawable) {
                animatable.unregisterAnimationCallback(this)
                endCallback()
            }
        })
        animatable.start()
    }
}
