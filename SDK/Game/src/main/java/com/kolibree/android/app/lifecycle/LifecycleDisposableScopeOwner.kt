/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.lifecycle

import android.annotation.SuppressLint
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.app.lifecycle.LifecycleMonitor.Companion.TAG_LIFECYCLE
import com.kolibree.android.commons.Weak
import com.kolibree.android.utils.DisposableScope
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * Responsible for association of [Lifecycle] events with corresponding disposable bag,
 * Each lifecycle pair has its dedicated [DisposableScope].
 */
@SuppressLint("ExperimentalClassUse")
@Keep
@KolibreeExperimental
class LifecycleDisposableScopeOwner(
    lifecycle: Lifecycle
) : LifecycleDisposableScope, DefaultLifecycleObserver {

    var monitoredClassName: String = javaClass.simpleName

    @VisibleForTesting
    val onPauseDisposables: DisposableScope =
        DisposableScope("onPause")

    @VisibleForTesting
    val onStopDisposables: DisposableScope =
        DisposableScope("onStop")

    @VisibleForTesting
    val onDestroyDisposables: DisposableScope =
        DisposableScope("onDestroy")

    private val lifecycleReference by Weak { lifecycle }

    private val lifecycleMonitor = LifecycleMonitor()

    init {
        lifecycleReference?.addObserver(this)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun disposeOnPause(block: () -> Disposable?) {
        onPauseDisposables += block.invoke()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun disposeOnStop(block: () -> Disposable?) {
        onStopDisposables += block.invoke()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun disposeOnDestroy(block: () -> Disposable?) {
        onDestroyDisposables += block.invoke()
    }

    override fun onCreate(owner: LifecycleOwner) {
        monitorLifecycle("onCreate", owner)
        onDestroyDisposables.ready()
    }

    override fun onStart(owner: LifecycleOwner) {
        monitorLifecycle("onStart", owner)
        onStopDisposables.ready()
    }

    override fun onResume(owner: LifecycleOwner) {
        monitorLifecycle("onResume", owner)
        onPauseDisposables.ready()
    }

    override fun onPause(owner: LifecycleOwner) {
        onPauseDisposables.clear()
        monitorLifecycle("onPause", owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        onStopDisposables.clear()
        monitorLifecycle("onStop", owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        onPauseDisposables.dispose()
        onStopDisposables.dispose()
        onDestroyDisposables.dispose()

        monitorLifecycle("onDestroy", owner)
        lifecycleReference?.removeObserver(this)
    }

    private fun monitorLifecycle(transitionName: String, owner: LifecycleOwner) {
        val state = owner.lifecycle.currentState
        Timber.tag(TAG_LIFECYCLE).v("$monitoredClassName - $transitionName")
        lifecycleMonitor.markTransitionToState(state)
    }
}
