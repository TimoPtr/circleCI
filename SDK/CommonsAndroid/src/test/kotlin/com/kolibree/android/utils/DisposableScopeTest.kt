/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import com.kolibree.android.failearly.FailEarly
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class DisposableScopeTest {

    class FailEarlyException : Exception()

    @Before
    fun setUp() {
        FailEarly.overrideDelegateWith { _, _ ->
            throw FailEarlyException()
        }
    }

    @Test
    fun `invokes ready set isReady to true`() {
        val scope = DisposableScope("")

        assertFalse(scope.isReady.get())
        scope.ready()
        assertTrue(scope.isReady.get())
    }

    @Test
    fun `invokes clear does not throw when ready not called`() {
        val scope = DisposableScope("")

        scope.clear()
    }

    @Test
    fun `invokes clear set isReady to false and invokes clear on compositeDisposable`() {
        val scope = DisposableScope("")

        scope.ready()
        scope.addSafely(Single.never<Boolean>().subscribe({}, {}))
        scope.clear()

        assertFalse(scope.isReady.get())
        assertEquals(0, scope.compositeDisposable.size())
        assertFalse(scope.compositeDisposable.isDisposed)
    }

    @Test
    fun `invokes dispose does not throw when ready not called`() {
        val scope = DisposableScope("")

        scope.dispose()
    }

    @Test
    fun `invokes dispose set isReady to false and invokes dispose on compositeDisposable`() {
        val scope = DisposableScope("")

        scope.ready()
        scope.addSafely(Single.never<Boolean>().subscribe({}, {}))
        scope.dispose()

        assertFalse(scope.isReady.get())
        assertEquals(0, scope.compositeDisposable.size())
        assertTrue(scope.compositeDisposable.isDisposed)
    }

    @Test(expected = FailEarlyException::class)
    fun `invokes addSafely with not ready throw`() {
        val scope = DisposableScope("")

        scope.addSafely(Single.never<Boolean>().subscribe({}, {}))
    }

    @Test
    fun `invokes addSafely add disposable to compositeDisposable`() {
        val scope = DisposableScope("")

        scope.ready()

        scope.addSafely(Single.never<Boolean>().subscribe({}, {}))
        assertEquals(1, scope.compositeDisposable.size())
        scope.clear()
    }

    @Test(expected = FailEarlyException::class)
    fun `invokes plusAssign with not ready throw`() {
        val scope = DisposableScope("")

        scope += Single.never<Boolean>().subscribe({}, {})
    }

    @Test
    fun `invokes plusAssign add disposable to compositeDisposable`() {
        val scope = DisposableScope("")

        scope.ready()

        scope += Single.never<Boolean>().subscribe({}, {})
        assertEquals(1, scope.compositeDisposable.size())
        scope.clear()
    }
}
