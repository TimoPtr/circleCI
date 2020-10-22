/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.content.Intent
import android.content.ServiceConnection
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleUseCase
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.Assert.assertEquals
import org.junit.Test

class ServiceProviderImplTest : BaseUnitTest() {

    private val appContext: ApplicationContext = mock()
    private val applicationLifecycleUseCase: ApplicationLifecycleUseCase = mock()
    private val timeScheduler = TestScheduler()
    private val serviceIntent: Intent = mock()

    private val applicationStateStream = PublishProcessor.create<Lifecycle.State>()

    private lateinit var serviceProvider: ServiceProvider

    override fun setup() {
        super.setup()
        serviceProvider = ServiceProviderImpl(
            appContext,
            applicationLifecycleUseCase,
            timeScheduler,
            serviceIntent
        )

        whenever(applicationLifecycleUseCase.observeApplicationState())
            .thenReturn(applicationStateStream)
        whenever(appContext.bindService(any(), any(), any()))
            .thenReturn(true)
    }

    @Test
    fun `binds to service when app is in the foreground`() {
        val testObserver = serviceProvider.connectStream().test()
        verify(appContext, never()).bindService(eq(serviceIntent), any(), any())

        applicationStateStream.offer(Lifecycle.State.STARTED)

        verify(appContext).bindService(eq(serviceIntent), any(), any())
        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
    }

    @Test
    fun `unbinds from service when in the background`() {
        val testObserver = serviceProvider.connectStream().test()
        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext).bindService(eq(serviceIntent), connection.capture(), any())

        applicationStateStream.offer(Lifecycle.State.CREATED)

        // does not unbind immediately after going to the background
        verify(appContext, never()).unbindService(connection.firstValue)
        timeScheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        verify(appContext).unbindService(connection.firstValue)
        testObserver.assertValue(ServiceDisconnected)
        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
    }

    @Test
    fun `unbinds from service when disposed`() {
        val testObserver = serviceProvider.connectStream().test()
        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext).bindService(eq(serviceIntent), connection.capture(), any())

        testObserver.dispose()

        verify(appContext).unbindService(connection.firstValue)
        testObserver.assertNoErrors()
    }

    @Test
    fun `emits new event when service connected`() {
        val testObserver = serviceProvider.connectStream().test()
        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext).bindService(eq(serviceIntent), connection.capture(), any())

        val mockService = mock<KolibreeService>()
        val mockBinder = mock<KolibreeService.KolibreeBinder>().apply {
            whenever(service).thenReturn(mockService)
        }

        connection.firstValue.onServiceConnected(mock(), mockBinder)

        testObserver.assertValue { result ->
            result is ServiceConnected && result.service == mockService
        }
    }

    @Test
    fun `emits new event when service disconnected`() {
        val testObserver = serviceProvider.connectStream().test()
        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext).bindService(eq(serviceIntent), connection.capture(), any())

        connection.firstValue.onServiceDisconnected(mock())

        testObserver.assertValue(ServiceDisconnected)
    }

    @Test
    fun `each stream uses unique service connection`() {
        val testObservers = listOf(
            serviceProvider.connectStream().test(),
            serviceProvider.connectStream().test(),
            serviceProvider.connectStream().test()
        )

        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext, times(testObservers.size)).bindService(
            eq(serviceIntent),
            connection.capture(),
            any()
        )
        assertEquals(testObservers.size, connection.allValues.distinct().size)
    }

    @Test
    fun `returns service when connected`() {
        val testObserver = serviceProvider.connectOnce().test()
        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext).bindService(eq(serviceIntent), connection.capture(), any())

        val mockService = mock<KolibreeService>()
        val mockBinder = mock<KolibreeService.KolibreeBinder>().apply {
            whenever(service).thenReturn(mockService)
        }

        connection.firstValue.onServiceConnected(mock(), mockBinder)

        testObserver.assertValue(mockService)
    }

    @Test
    fun `waits when service is not connected`() {
        val testObserver = serviceProvider.connectOnce().test()
        applicationStateStream.offer(Lifecycle.State.STARTED)

        val connection = argumentCaptor<ServiceConnection>()
        verify(appContext).bindService(eq(serviceIntent), connection.capture(), any())

        connection.firstValue.onServiceDisconnected(mock())

        testObserver.assertNotComplete()
        testObserver.assertNoErrors()

        applicationStateStream.offer(Lifecycle.State.CREATED)
        timeScheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
    }
}
