/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import com.kolibree.android.test.LifecycleObserverTester
import com.kolibree.android.test.lifecycleTester
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Completable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LifecycleDisposableScopeOwnerTest {

    private val lifecycle: Lifecycle = mock()
    private val underTest = LifecycleDisposableScopeOwner(lifecycle)
    private val lifecycleTester: LifecycleObserverTester =
        underTest.lifecycleTester()

    @Test
    fun `all disposable scopes are not ready if lifecycle is still in INITIALIZED state`() {
        assertFalse(underTest.onPauseDisposables.isReady.get())
        assertFalse(underTest.onStopDisposables.isReady.get())
        assertFalse(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `onDestroyDisposables are ready if lifecycle hits ON_CREATE`() {
        lifecycleTester.pushLifecycleTo(ON_CREATE)

        assertFalse(underTest.onPauseDisposables.isReady.get())
        assertFalse(underTest.onStopDisposables.isReady.get())
        assertTrue(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `onStopDisposables are ready if lifecycle hits ON_START`() {
        lifecycleTester.pushLifecycleTo(ON_START)

        assertFalse(underTest.onPauseDisposables.isReady.get())
        assertTrue(underTest.onStopDisposables.isReady.get())
        assertTrue(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `onPauseDisposables are ready if lifecycle hits ON_RESUME`() {
        lifecycleTester.pushLifecycleTo(ON_RESUME)

        assertTrue(underTest.onPauseDisposables.isReady.get())
        assertTrue(underTest.onStopDisposables.isReady.get())
        assertTrue(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `onPauseDisposables are cleared if lifecycle hits ON_PAUSE`() {
        lifecycleTester.pushLifecycleTo(ON_RESUME)
        underTest.disposeOnPause { Completable.complete().subscribe() }
        assertEquals(1, underTest.onPauseDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(ON_PAUSE)
        assertEquals(0, underTest.onPauseDisposables.compositeDisposable.size())

        assertFalse(underTest.onPauseDisposables.isReady.get())
        assertTrue(underTest.onStopDisposables.isReady.get())
        assertTrue(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `onStopDisposables are cleared if lifecycle hits ON_STOP`() {
        lifecycleTester.pushLifecycleTo(ON_START)
        underTest.disposeOnStop { Completable.complete().subscribe() }
        assertEquals(1, underTest.onStopDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(ON_STOP)
        assertEquals(0, underTest.onStopDisposables.compositeDisposable.size())

        assertFalse(underTest.onPauseDisposables.isReady.get())
        assertFalse(underTest.onStopDisposables.isReady.get())
        assertTrue(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `onDestroyDisposables are cleared if lifecycle hits ON_DESTROY`() {
        lifecycleTester.pushLifecycleTo(ON_CREATE)
        underTest.disposeOnDestroy { Completable.complete().subscribe() }
        assertEquals(1, underTest.onDestroyDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(ON_DESTROY)
        assertEquals(0, underTest.onDestroyDisposables.compositeDisposable.size())

        assertFalse(underTest.onPauseDisposables.isReady.get())
        assertFalse(underTest.onStopDisposables.isReady.get())
        assertFalse(underTest.onDestroyDisposables.isReady.get())
    }

    @Test
    fun `all disposables are not disposed until lifecycle hits ON_DESTROY`() {
        val nonDisposableStates = listOf(ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP)

        nonDisposableStates.forEach { state ->
            lifecycleTester.pushLifecycleTo(state)

            assertFalse(underTest.onPauseDisposables.compositeDisposable.isDisposed)
            assertFalse(underTest.onStopDisposables.compositeDisposable.isDisposed)
            assertFalse(underTest.onDestroyDisposables.compositeDisposable.isDisposed)
        }

        lifecycleTester.pushLifecycleTo(ON_DESTROY)

        assertTrue(underTest.onPauseDisposables.compositeDisposable.isDisposed)
        assertTrue(underTest.onStopDisposables.compositeDisposable.isDisposed)
        assertTrue(underTest.onDestroyDisposables.compositeDisposable.isDisposed)
    }

    @Test
    fun `lifecycle is observed until it hits ON_DESTROY`() {
        verify(lifecycle).addObserver(underTest)

        lifecycleTester.pushLifecycleTo(ON_DESTROY)

        verify(lifecycle).removeObserver(underTest)
    }
}
