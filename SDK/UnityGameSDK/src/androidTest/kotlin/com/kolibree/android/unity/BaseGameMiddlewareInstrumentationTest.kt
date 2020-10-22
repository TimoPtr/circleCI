/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.lifecycle.LifecycleDisposableScope
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.LifecycleObserverTester
import com.kolibree.android.test.lifecycleTester
import com.kolibree.game.middleware.GameMiddleware
import com.kolibree.kml.Kml
import io.reactivex.Completable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

abstract class BaseGameMiddlewareInstrumentationTest : BaseInstrumentationTest() {

    override fun setUp() {
        super.setUp()
        Kml.init()
        GameMiddleware.init()
    }

    protected fun testInteractorLifecycle(
        interactorUnderTest: LifecycleDisposableScope,
        owner: LifecycleDisposableScopeOwner
    ) {
        val lifecycleTester: LifecycleObserverTester = owner.lifecycleTester()

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        interactorUnderTest.disposeOnDestroy { Completable.complete().subscribe() }
        assertEquals(0, owner.onPauseDisposables.compositeDisposable.size())
        assertEquals(0, owner.onStopDisposables.compositeDisposable.size())
        assertEquals(1, owner.onDestroyDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_START)
        interactorUnderTest.disposeOnStop { Completable.complete().subscribe() }
        assertEquals(0, owner.onPauseDisposables.compositeDisposable.size())
        assertEquals(1, owner.onStopDisposables.compositeDisposable.size())
        assertEquals(1, owner.onDestroyDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        interactorUnderTest.disposeOnPause { Completable.complete().subscribe() }
        assertEquals(1, owner.onPauseDisposables.compositeDisposable.size())
        assertEquals(1, owner.onStopDisposables.compositeDisposable.size())
        assertEquals(1, owner.onDestroyDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)
        assertEquals(0, owner.onPauseDisposables.compositeDisposable.size())
        assertEquals(1, owner.onStopDisposables.compositeDisposable.size())
        assertEquals(1, owner.onDestroyDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        assertEquals(0, owner.onPauseDisposables.compositeDisposable.size())
        assertEquals(0, owner.onStopDisposables.compositeDisposable.size())
        assertEquals(1, owner.onDestroyDisposables.compositeDisposable.size())

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_DESTROY)
        assertEquals(0, owner.onPauseDisposables.compositeDisposable.size())
        assertEquals(0, owner.onStopDisposables.compositeDisposable.size())
        assertEquals(0, owner.onDestroyDisposables.compositeDisposable.size())

        assertTrue(owner.onPauseDisposables.compositeDisposable.isDisposed)
        assertTrue(owner.onStopDisposables.compositeDisposable.isDisposed)
        assertTrue(owner.onDestroyDisposables.compositeDisposable.isDisposed)
    }
}
