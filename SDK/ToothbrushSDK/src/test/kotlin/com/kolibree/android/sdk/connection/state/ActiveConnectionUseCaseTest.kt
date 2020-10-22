/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.state

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ActiveConnectionUseCaseTest : BaseUnitTest() {
    private val serviceProvider: ServiceProvider = mock()

    private val service: KolibreeService = mock()

    private lateinit var useCase: ActiveConnectionUseCase

    private val serviceKnownConnectionsProcessor = BehaviorProcessor.create<List<KLTBConnection>>()

    /*
    flowable subscription
     */

    @Test
    fun `Nothing crashes if connectStream only emits ServiceDisconnected`() {
        val connectServiceSubject = PublishSubject.create<ServiceProvisionResult>()
        initUseCase(serviceConnectStream = connectServiceSubject)

        useCase.onConnectionsUpdatedStream().test()

        assertNull(useCase.serviceWeak.get())

        connectServiceSubject.onNext(ServiceDisconnected)

        assertNull(useCase.serviceWeak.get())
    }

    @Test
    fun `Service is stored when it's connected`() {
        val connectServiceSubject = PublishSubject.create<ServiceProvisionResult>()
        initUseCase(serviceConnectStream = connectServiceSubject)

        useCase.onConnectionsUpdatedStream().test().assertEmpty()

        assertNull(useCase.serviceWeak.get())

        connectServiceSubject.onNext(ServiceConnected(service))

        assertEquals(service, useCase.serviceWeak.get())
    }

    @Test
    fun `Service is nullified if we receive a ServiceDisconnected after ServiceConnected`() {
        val connectServiceSubject = PublishSubject.create<ServiceProvisionResult>()
        initUseCase(serviceConnectStream = connectServiceSubject)

        useCase.onConnectionsUpdatedStream().test()

        connectServiceSubject.onNext(ServiceConnected(service))

        assertEquals(service, useCase.serviceWeak.get())

        connectServiceSubject.onNext(ServiceDisconnected)

        assertNull(useCase.serviceWeak.get())
    }

    @Test
    fun `We register as listeners to service known connections and unregister on stream dispose`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        initUseCase(initialServiceKnownConnections = listOf(connection))

        val observer = useCase.onConnectionsUpdatedStream().test()

        assertEquals(connection, useCase.connectionsNotifyingStateChanges.single().get())

        observer.dispose()

        assertTrue(useCase.connectionsNotifyingStateChanges.isEmpty())
    }

    /*
    onConnectionStateChanged
     */
    @Test
    fun `onConnectionStateChanged emits expected connection when connection becomes ACTIVE`() {
        initUseCase()

        val observer = useCase.onConnectionsUpdatedStream().test().assertEmpty()

        val expectedMac = "dasda"
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(expectedMac)
            .build()
        useCase.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        observer.assertValue(connection).assertNotComplete()
    }

    @Test
    fun `onConnectionStateChanged emits nothing when connection enters any other state`() {
        initUseCase()

        val observer = useCase.onConnectionsUpdatedStream().test().assertEmpty()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                useCase.onConnectionStateChanged(connection, state)
            }

        observer.assertEmpty()
    }

    /*
    listenToConnectionsState
     */
    @Test
    fun `listenToConnectionsState registers as connection state listener on each connection`() {
        initUseCase()

        val connection1 = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess()
            .withMac("dadadadacc")
            .build()

        useCase.listenToConnectionsState(listOf(connection1, connection2))

        verify(connection1.state()).register(useCase)
        verify(connection2.state()).register(useCase)
    }

    @Test
    fun `listenToConnectionsState immediately notifies if connection is already active`() {
        initUseCase()

        val nonActiveConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()

        val expectedMac = "dadadadacc"
        val activeConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ACTIVE)
            .withMac(expectedMac)
            .build()

        val observer = useCase.onConnectionsUpdatedStream().test().assertEmpty()

        useCase.listenToConnectionsState(listOf(nonActiveConnection, activeConnection))

        observer.assertValue(activeConnection)
    }

    @Test
    fun `listenToConnectionsState does not register as connection state listener if we are already listening to it`() {
        initUseCase()

        val connection1 = KLTBConnectionBuilder.createAndroidLess().build()

        useCase.connectionsNotifyingStateChanges.add(WeakReference(connection1))

        useCase.listenToConnectionsState(listOf(connection1))

        verify(connection1.state(), never()).register(useCase)
    }

    /*
    registerAsConnectionStateListener
     */
    @Test
    fun `registerAsConnectionStateListener unregisters and registers as connection state listener`() {
        initUseCase()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        useCase.registerAsConnectionStateListener(connection)

        inOrder(connection.state()) {
            verify(connection.state()).unregister(useCase)
            verify(connection.state()).register(useCase)
        }
    }

    @Test
    fun `registerAsConnectionStateListener stores a weakreference to the connection`() {
        initUseCase()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        assertTrue(useCase.connectionsNotifyingStateChanges.isEmpty())

        useCase.registerAsConnectionStateListener(connection)

        assertEquals(connection, useCase.connectionsNotifyingStateChanges.single().get())
    }

    /*
    stopListeningToConnectionStates
     */
    @Test
    fun `stopListeningToConnectionStates does nothing if we aren't listening to any connection`() {
        initUseCase()

        assertTrue(useCase.connectionsNotifyingStateChanges.isEmpty())

        useCase.stopListeningToConnectionStates()
    }

    @Test
    fun `stopListeningToConnectionStates does nothing if the KLTBConnection references has been nullified`() {
        initUseCase()

        useCase.connectionsNotifyingStateChanges.add(WeakReference<KLTBConnection>(null))

        useCase.stopListeningToConnectionStates()
    }

    @Test
    fun `stopListeningToConnectionStates unregisters as listener`() {
        initUseCase()

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        useCase.connectionsNotifyingStateChanges.add(WeakReference(connection))

        useCase.stopListeningToConnectionStates()

        verify(connection.state()).unregister(useCase)
    }

    @Test
    fun `stopListeningToConnectionStates clears connectionsNotifyingStateChanges`() {
        initUseCase()

        useCase.connectionsNotifyingStateChanges.add(
            WeakReference(
                KLTBConnectionBuilder.createAndroidLess().build()
            )
        )

        useCase.stopListeningToConnectionStates()

        assertTrue(useCase.connectionsNotifyingStateChanges.isEmpty())
    }

    /*
    Utils
     */

    private fun initUseCase(
        serviceConnectStream: Observable<ServiceProvisionResult> = BehaviorSubject.createDefault(
            ServiceConnected(service)
        ),
        initialServiceKnownConnections: List<KLTBConnection> = listOf()
    ) {
        setupServiceProvider(serviceConnectStream, initialServiceKnownConnections)

        useCase = ActiveConnectionUseCase(serviceProvider)
    }

    private fun setupServiceProvider(
        serviceConnectStream: Observable<ServiceProvisionResult>,
        initialServiceKnownConnections: List<KLTBConnection>
    ) {
        whenever(serviceProvider.connectStream()).thenReturn(serviceConnectStream)

        setupServiceConnections(initialServiceKnownConnections)
    }

    private fun setupServiceConnections(connections: List<KLTBConnection> = listOf()) {
        serviceKnownConnectionsProcessor.onNext(connections)

        whenever(service.knownConnectionsOnceAndStream).thenReturn(serviceKnownConnectionsProcessor)
    }
}
