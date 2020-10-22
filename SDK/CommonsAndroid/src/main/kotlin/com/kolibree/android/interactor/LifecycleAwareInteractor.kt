/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.interactor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.collection.UniquePool
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.lifecycle.InternalLifecycle
import java.lang.ref.WeakReference

/**
 * Base class for reusable, observable lifecycle-aware logic components. Hooks itself to provide LifecycleOwner,
 * so lifecycle-related methods will called automatically. Brings common solution to the problem of bloated
 * activities and fragments and long inheritance chains (LifecycleAwareInteractors can be composed together
 * and used in both activities and fragments, making their code much thinner).
 *
 * Please keep those classes stateless.
 *
 * Its public interface should be exposed via interface, which should be passed as concretisation of generic
 * parameter T.
 *
 * @author lookashc
 * @date 28/03/19
 * @param T listener interface type
 */
@SuppressWarnings("TooManyFunctions") // Due to lifecycle methods
@VisibleForApp
abstract class LifecycleAwareInteractor<T> : InternalLifecycle {

    @PublishedApi
    internal val listeners = UniquePool<T>()

    private var lifecycleObserver: InteractorLifecycleObserver? = null

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) = setLifecycleOwnerInternal(lifecycleOwner)

    /**
     * Adds listener to the pool
     * @param listener listener to add
     */
    @SuppressLint("BinaryOperationInTimber")
    fun addListener(listener: T) {
        if (lifecycleObserver == null) {
            FailEarly.fail(
                "You forgot to call setLifecycleOwner() on this interactor!\n" +
                    "If you use composition of interactors, please remember to pass the owner to interactors you hold."
            ) {
                if (listener is LifecycleAwareInteractor<*>) {
                    // Fallback - take the owner from parent)
                    listener.lifecycleObserver?.lifecycleOwner?.get()?.let { setLifecycleOwner(it) }
                }
            }
        }
        listeners.add(listener)
    }

    /**
     * Removes listener from the pool
     * @param listener listener to remove
     */
    fun removeListener(listener: T) {
        listeners.remove(listener)
    }

    protected inline fun forEachListener(toCall: (T) -> Unit) {
        listeners.forEach { listener -> toCall(listener) }
    }

    /**
     * Sets the [LifecycleOwner] for this interactor, to make this lifecycle aware.
     *
     * Please note: when composing interactors together, this method should be overridden - owner should
     * be passed to composed objects first, and later to our own interactors.
     * See [GameInteractor] for an example.
     *
     * @param lifecycleOwner owner for this interactor
     */
    @CallSuper
    protected open fun setLifecycleOwnerInternal(lifecycleOwner: LifecycleOwner) {
        lifecycleObserver = InteractorLifecycleObserver(WeakReference(lifecycleOwner))
    }

    @CallSuper
    override fun onCreateInternal(savedInstanceState: Bundle?) {
        // reserved for future use
    }

    @CallSuper
    override fun onStartInternal() {
        // reserved for future use
    }

    @CallSuper
    override fun onResumeInternal() {
        // reserved for future use
    }

    @CallSuper
    override fun onPauseInternal() {
        // reserved for future use
    }

    @CallSuper
    override fun onStopInternal() {
        // reserved for future use
    }

    @CallSuper
    override fun onDestroyInternal() {
        listeners.clear()
    }

    @Suppress("unused")
    private inner class InteractorLifecycleObserver(
        internal val lifecycleOwner: WeakReference<LifecycleOwner>
    ) : LifecycleObserver {

        init {
            lifecycleOwner.get()?.lifecycle?.addObserver(this)
            onCreateInternal(null)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            onStartInternal()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            onResumeInternal()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            onPauseInternal()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            onStopInternal()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroyInternal()
            lifecycleOwner.get()?.lifecycle?.removeObserver(this)
        }
    }
}
