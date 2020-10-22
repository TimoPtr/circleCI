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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Test

class ApplicationLifecyclePublisherImplTest : BaseUnitTest() {

    private val mockHandler = mock<Handler>().apply {
        whenever(post(any())).thenAnswer {
            (it.arguments[0] as Runnable).run()
            return@thenAnswer true
        }
    }
    private val mockObservers = setOf<ApplicationLifecycleObserver>(
        mock(),
        mock(),
        mock()
    )

    private lateinit var mockLifecycle: LifecycleRegistry
    private lateinit var publisher: ApplicationLifecyclePublisher

    override fun setup() {
        super.setup()
        mockLifecycle = LifecycleRegistry(mock())
        publisher = ApplicationLifecyclePublisherImpl(
            mockLifecycle,
            mockHandler,
            mockObservers
        )
    }

    @Test
    fun `adds lifecycle observer after initialized`() {
        assertEquals(0, mockLifecycle.observerCount)

        publisher.initialize()
        assertEquals(1, mockLifecycle.observerCount)
    }

    @Test
    fun `publishes events to observers`() {
        publisher.initialize()

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        for (observer in mockObservers) {
            verify(observer).onApplicationCreated()
        }

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        for (observer in mockObservers) {
            verify(observer).onApplicationStarted()
        }

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        for (observer in mockObservers) {
            verify(observer).onApplicationResumed()
        }

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        for (observer in mockObservers) {
            verify(observer).onApplicationPaused()
        }

        mockLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        for (observer in mockObservers) {
            verify(observer).onApplicationStopped()
        }
    }
}
