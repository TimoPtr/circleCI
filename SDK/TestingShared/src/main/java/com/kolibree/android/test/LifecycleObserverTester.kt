/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleOwner
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.mockingDetails
import com.nhaarman.mockitokotlin2.whenever

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
class LifecycleObserverTester(
    private val underTest: DefaultLifecycleObserver,
    val lifecycleOwner: LifecycleOwner = mock(),
    lifecycle: Lifecycle
) {

    private var lastLifecycleEvent: Lifecycle.Event? = null

    init {
        if (mockingDetails(lifecycleOwner).isMock) {
            whenever(lifecycleOwner.lifecycle).thenReturn(lifecycle)
        }
    }

    fun pushLifecycleTo(target: Lifecycle.Event) {
        executeIfNotAlreadyDone(ON_CREATE, target) { underTest.onCreate(lifecycleOwner) }
        executeIfNotAlreadyDone(ON_START, target) { underTest.onStart(lifecycleOwner) }
        executeIfNotAlreadyDone(ON_RESUME, target) { underTest.onResume(lifecycleOwner) }
        executeIfNotAlreadyDone(ON_PAUSE, target) { underTest.onPause(lifecycleOwner) }
        executeIfNotAlreadyDone(ON_STOP, target) { underTest.onStop(lifecycleOwner) }
        executeIfNotAlreadyDone(ON_DESTROY, target) { underTest.onDestroy(lifecycleOwner) }

        lastLifecycleEvent = target
    }

    private fun executeIfNotAlreadyDone(
        source: Lifecycle.Event,
        target: Lifecycle.Event,
        execute: () -> Unit
    ) {
        if (!alreadyDone(source) && target >= source) {
            whenever(lifecycleOwner.lifecycle.currentState).thenReturn(source.toState())
            execute()
        }
    }

    private fun alreadyDone(lifecycleEvent: Lifecycle.Event): Boolean {
        return lastLifecycleEvent?.let { it >= lifecycleEvent } ?: false
    }
}

private fun Lifecycle.Event.toState(): Lifecycle.State? = when (this) {
    ON_CREATE -> Lifecycle.State.CREATED
    ON_START -> Lifecycle.State.STARTED
    ON_RESUME -> Lifecycle.State.RESUMED
    ON_PAUSE -> Lifecycle.State.STARTED
    ON_STOP -> Lifecycle.State.CREATED
    ON_DESTROY -> Lifecycle.State.DESTROYED
    else -> null
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun DefaultLifecycleObserver.lifecycleTester(
    lifecycle: Lifecycle = mock(),
    lifecycleOwner: LifecycleOwner = mock()
) = LifecycleObserverTester(
    underTest = this,
    lifecycle = lifecycle,
    lifecycleOwner = lifecycleOwner
)

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun DefaultLifecycleObserver.pushLifecycleTo(target: Lifecycle.Event) {
    lifecycleTester().pushLifecycleTo(target)
}
