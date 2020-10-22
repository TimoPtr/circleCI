/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils.lifecycle

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.extensions.assertLastValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Test

class ApplicationLifecycleUseCaseImplTest : BaseUnitTest() {

    private lateinit var mockLifecycle: LifecycleRegistry
    private lateinit var useCase: ApplicationLifecycleUseCase
    private val mockHandler = mock<Handler>().apply {
        whenever(postAtFrontOfQueue(any())).thenAnswer {
            (it.arguments[0] as Runnable).run()
            return@thenAnswer true
        }
    }

    override fun setup() {
        super.setup()
        mockLifecycle = LifecycleRegistry(mock())
        useCase = ApplicationLifecycleUseCaseImpl(mockLifecycle, mockHandler)
    }

    @Test
    fun `emits correct state after lifecycle events`() {
        val observer = useCase.observeApplicationState().test()
        observer.assertLastValue(Lifecycle.State.INITIALIZED)

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observer.assertLastValue(Lifecycle.State.CREATED)

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observer.assertLastValue(Lifecycle.State.STARTED)

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        observer.assertLastValue(Lifecycle.State.RESUMED)

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        observer.assertLastValue(Lifecycle.State.STARTED)

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        observer.assertLastValue(Lifecycle.State.CREATED)

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        observer.assertLastValue(Lifecycle.State.DESTROYED)
    }

    @Test
    fun `removes lifecycle observer when stream is not active`() {
        val observer = useCase.observeApplicationState().test()
        assertEquals(1, mockLifecycle.observerCount)

        observer.dispose()
        assertEquals(0, mockLifecycle.observerCount)
    }
}
