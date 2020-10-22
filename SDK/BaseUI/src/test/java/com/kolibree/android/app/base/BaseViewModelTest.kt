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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.LifecycleObserverTester
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.times
import junit.framework.TestCase
import org.junit.Test

class BaseViewModelTest : BaseUnitTest() {
    private lateinit var viewModelLifecycleTester: LifecycleObserverTester

    private lateinit var viewModel: FakeBaseViewModel

    override fun setup() {
        super.setup()

        viewModel = FakeBaseViewModel()

        viewModelLifecycleTester = viewModel.lifecycleTester()
    }

    /*
    isResumed
     */

    @Test
    fun `isResumed returns true only while owner is resumed`() {
        pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        TestCase.assertFalse(viewModel.testIsResumed())

        pushLifecycleTo(Lifecycle.Event.ON_START)

        TestCase.assertFalse(viewModel.testIsResumed())

        pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        TestCase.assertTrue(viewModel.testIsResumed())

        pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        TestCase.assertFalse(viewModel.testIsResumed())

        pushLifecycleTo(Lifecycle.Event.ON_STOP)

        TestCase.assertFalse(viewModel.testIsResumed())

        pushLifecycleTo(Lifecycle.Event.ON_DESTROY)

        TestCase.assertFalse(viewModel.testIsResumed())
    }

    @Test
    fun `pushes enqueued action when resumed`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.pushActionWhenResumed(FakeActions.ActionA)
        viewModel.pushActionWhenResumed(FakeActions.ActionB)
        viewModel.pushActionWhenResumed(FakeActions.ActionC)

        testObserver.assertNoValues()

        // We want to push lifecycle twice, to see if actions are being cleared
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        testObserver.assertValues(
            FakeActions.ActionA,
            FakeActions.ActionB,
            FakeActions.ActionC
        )
    }

    @Test
    fun `pushes action immediately if already resumed`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.pushActionWhenResumed(FakeActions.ActionA)
        testObserver.assertValue(FakeActions.ActionA)
    }

    /*
    Utils
     */

    private fun pushLifecycleTo(
        newState: Lifecycle.Event
    ) {
        viewModelLifecycleTester.pushLifecycleTo(newState)
    }
}

private class FakeBaseViewModel : BaseViewModel<EmptyBaseViewState, FakeActions>(EmptyBaseViewState) {
    fun testIsResumed() = isResumed()
}

private sealed class FakeActions : BaseAction {
    object ActionA : FakeActions()
    object ActionB : FakeActions()
    object ActionC : FakeActions()
}
