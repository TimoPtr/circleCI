/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class BaseNavigatorTest : BaseUnitTest() {
    private val fakeNavigator = FakeNavigator()

    private val lifecycleOwner: LifecycleOwner = FakeLifecycleOwner()

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(TestDelegate)
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    /*
    Lifecycle
     */

    @Test(expected = AssertionError::class)
    fun `withOwner throws exception if the owner never reported onCreate`() {
        fakeNavigator.invokeWithOwner { }
    }

    @Test
    fun `withOwner invokes block if the owner state is ON_CREATE`() {
        pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertBlockInvoked()
    }

    @Test
    fun `withOwner invokes block if the owner state is ON_STOP`() {
        pushLifecycleTo(Lifecycle.Event.ON_STOP)

        assertBlockInvoked()
    }

    @Test
    fun `withOwner invokes block if the owner state is ON_RESUME`() {
        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertBlockInvoked()
    }

    @Test
    fun `withOwner invokes block if the owner state is ON_START`() {
        pushLifecycleTo(Lifecycle.Event.ON_START)

        assertBlockInvoked()
    }

    @Test
    fun `withOwner invokes block if the owner state is ON_PAUSE`() {
        pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertBlockInvoked()
    }

    @Test(expected = AssertionError::class)
    fun `withOwner throws exception if the owner state is ON_DESTROY`() {
        pushLifecycleTo(Lifecycle.Event.ON_DESTROY)

        fakeNavigator.invokeWithOwner { }
    }

    /*
    Utils
     */

    private fun assertBlockInvoked() {
        var invoked = false
        fakeNavigator.invokeWithOwner { invoked = true }
        assertTrue(invoked)
    }

    private fun pushLifecycleTo(event: Lifecycle.Event) {
        fakeNavigator.lifecycleTester(
            lifecycle = lifecycleOwner.lifecycle,
            lifecycleOwner = lifecycleOwner
        )
            .pushLifecycleTo(event)
    }
}

internal class FakeNavigator : BaseNavigator<FakeLifecycleOwner>() {
    fun invokeWithOwner(block: () -> Unit) = withOwner { block() }
}

internal class FakeLifecycleOwner : LifecycleOwner {
    var observers: MutableList<LifecycleObserver> = mutableListOf()

    var state: Lifecycle.State = mock()

    private val lifecycle: Lifecycle = mock()

    override fun getLifecycle(): Lifecycle = lifecycle
}
