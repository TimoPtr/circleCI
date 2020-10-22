/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.Animatable2Compat

internal fun Drawable.toAnimatableCompat(): AnimatableCompat<*>? =
    (this as? Animatable)?.let { AnimatableCompat.create(it) }

internal abstract class AnimatableCompat<T> private constructor(private val animatable: Animatable) :
    Animatable by animatable {

    companion object {
        fun create(animatable: Animatable): AnimatableCompat<*>? =
            when (animatable) {
                is Animatable2 -> Animatable2Wrapper(animatable)
                is Animatable2Compat -> Animatable2CompatWrapper(animatable)
                else -> null
            }
    }

    protected val registeredCallbacks = mutableMapOf<AnimationCallback, T>()

    abstract fun registerAnimationCallback(callback: AnimationCallback)

    abstract fun unregisterAnimationCallback(callback: AnimationCallback): Boolean

    abstract fun clearAnimationCallbacks()

    private class Animatable2Wrapper(val animatable2: Animatable2) :
        AnimatableCompat<Animatable2.AnimationCallback>(animatable2) {
        override fun registerAnimationCallback(callback: AnimationCallback) {
            Animatable2Callback(callback).also { innerCallback ->
                animatable2.registerAnimationCallback(innerCallback)
                registeredCallbacks += callback to innerCallback
            }
        }

        override fun unregisterAnimationCallback(callback: AnimationCallback): Boolean {
            return registeredCallbacks.remove(callback)?.let { innerCallback ->
                animatable2.unregisterAnimationCallback(innerCallback)
            } ?: false
        }

        override fun clearAnimationCallbacks() {
            animatable2.clearAnimationCallbacks()
            registeredCallbacks.clear()
        }
    }

    private class Animatable2CompatWrapper(val animatable2: Animatable2Compat) :
        AnimatableCompat<Animatable2Compat.AnimationCallback>(animatable2) {
        override fun registerAnimationCallback(callback: AnimationCallback) {
            Animatable2CompatCallback(callback).also { innerCallback ->
                animatable2.registerAnimationCallback(innerCallback)
                registeredCallbacks += callback to innerCallback
            }
        }

        override fun unregisterAnimationCallback(callback: AnimationCallback): Boolean {
            return registeredCallbacks.remove(callback)?.let { innerCallback ->
                animatable2.unregisterAnimationCallback(innerCallback)
            } ?: false
        }

        override fun clearAnimationCallbacks() {
            animatable2.clearAnimationCallbacks()
            registeredCallbacks.clear()
        }
    }

    interface AnimationCallback {
        fun onAnimationStart(drawable: Drawable) {}
        fun onAnimationEnd(drawable: Drawable) {}
    }

    private class Animatable2Callback(private val animatable2Callback: AnimationCallback) :
        Animatable2.AnimationCallback(), AnimationCallback {
        override fun onAnimationStart(drawable: Drawable) {
            super<Animatable2.AnimationCallback>.onAnimationStart(drawable)
            animatable2Callback.onAnimationStart(drawable)
        }

        override fun onAnimationEnd(drawable: Drawable) {
            super<Animatable2.AnimationCallback>.onAnimationEnd(drawable)
            animatable2Callback.onAnimationEnd(drawable)
        }
    }

    private class Animatable2CompatCallback(private val animatable2Callback: AnimationCallback) :
        Animatable2Compat.AnimationCallback(), AnimationCallback {
        override fun onAnimationStart(drawable: Drawable) {
            super<Animatable2Compat.AnimationCallback>.onAnimationStart(drawable)
            animatable2Callback.onAnimationStart(drawable)
        }

        override fun onAnimationEnd(drawable: Drawable) {
            super<Animatable2Compat.AnimationCallback>.onAnimationEnd(drawable)
            animatable2Callback.onAnimationEnd(drawable)
        }
    }
}
