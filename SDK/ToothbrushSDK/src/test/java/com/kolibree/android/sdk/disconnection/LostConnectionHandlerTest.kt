/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.disconnection

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LostConnectionHandlerTest : BaseUnitTest() {

    private val serviceProvider = mock<ServiceProvider>()
    private lateinit var handler: LostConnectionHandlerImpl

    override fun setup() {
        super.setup()

        handler = spy(LostConnectionHandlerImpl(serviceProvider))
    }

    @Test
    fun `registeredState is initialized to null`() {
        assertNull(handler.registeredState.get())
    }

    /*
    connectionObservable
     */

    @Test
    fun `connectionObservable subscription invokes onServiceStateChanged`() {
        val mac = "00:01:02:03"
        val expectedResult = ServiceConnected(mock())
        whenever(serviceProvider.connectStream()).thenReturn(Observable.just(expectedResult))

        handler.connectionObservable(mac).test()

        verify(handler).onServiceStateChanged(expectedResult, mac)
    }

    @Test
    fun `connectionObservable invokes onServiceStateChanged if connectStream emits a new ServiceConnected`() {
        val mac = "00:01:02:03"
        val subject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(subject)

        handler.connectionObservable(mac).test()

        verify(handler, never()).onServiceStateChanged(any(), any())

        val firstService = ServiceConnected(mock())
        subject.onNext(firstService)

        verify(handler).onServiceStateChanged(firstService, mac)

        val secondService = ServiceConnected(mock())
        subject.onNext(secondService)

        verify(handler).onServiceStateChanged(secondService, mac)
    }

    @Test
    fun `connectionObservable invokes unregisterConnectionState if connectStream is disposed`() {
        val mac = "00:01:02:03"
        val subject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(subject)

        val observable = handler.connectionObservable(mac).test()

        verify(handler, never()).unregisterConnectionState()

        observable.dispose()

        verify(handler).unregisterConnectionState()
    }

    /*
    registerConnectionState
     */

    @Test
    fun `onServiceStateChanged unregisters from previous registeredState if result is ServiceDisconnected`() {
        handler.onServiceStateChanged(ServiceDisconnected, "")

        verify(handler).unregisterConnectionState()
    }

    @Test
    fun `onServiceStateChanged invokes registerConnectionState if result is ServiceConnected`() {
        val previousConnectionState = mock<ConnectionState>()
        handler.registeredState = WeakReference(previousConnectionState)

        val expectedService = mock<KolibreeService>()
        val mac = "dada"
        handler.onServiceStateChanged(ServiceConnected(expectedService), mac)

        verify(handler).registerConnectionState(expectedService, mac)
    }

    /*
    registerConnectionState
     */

    @Test
    fun `registerConnectionState does nothing if connection returned from service is null`() {
        val service = mock<KolibreeService>()
        val mac = "dada"
        handler.registerConnectionState(service, mac)
    }

    @Test
    fun `registerConnectionState registers as connection listener on connection returned from service`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        val service = mock<KolibreeService>()
        val mac = "dada"
        whenever(service.getConnection(mac)).thenReturn(connection)
        handler.registerConnectionState(service, mac)

        verify(connection.state()).register(handler.connectionStateListener)
    }

    @Test
    fun `registerConnectionState stores new ConnectionState`() {
        val previousConnectionState = mock<ConnectionState>()
        handler.registeredState = WeakReference(previousConnectionState)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        val service = mock<KolibreeService>()
        val mac = "dada"
        whenever(service.getConnection(mac)).thenReturn(connection)
        handler.registerConnectionState(service, mac)

        assertEquals(connection.state(), handler.registeredState.get())
    }

    /*
    checkIfTheConnectionIsLost
     */

    @Test
    fun `checkIfTheConnectionIsLost returns true only for state TERMINATED`() {
        assertTrue(handler.checkIfTheConnectionIsLost(KLTBConnectionState.TERMINATED))
        assertFalse(handler.checkIfTheConnectionIsLost(KLTBConnectionState.NEW))
        assertFalse(handler.checkIfTheConnectionIsLost(KLTBConnectionState.ESTABLISHING))
        assertFalse(handler.checkIfTheConnectionIsLost(KLTBConnectionState.ACTIVE))
    }

    @Test
    fun `checkIfTheConnectionHasBeenLostAndItsConnecting returns true if previous state was TERMINATED and current ESTABLISHING`() {
        handler.previousBtStateActivated = KLTBConnectionState.TERMINATED

        assertTrue(handler.checkIfTheConnectionHasBeenLostAndItsConnecting(KLTBConnectionState.ESTABLISHING))
    }

    @Test
    fun `checkIfTheConnectionIsNowActive return true if state is ACTIVE and previous not`() {
        handler.previousBtStateActivated = KLTBConnectionState.TERMINATED
        assertTrue(handler.checkIfTheConnectionIsNowActive(KLTBConnectionState.ACTIVE))

        handler.previousBtStateActivated = KLTBConnectionState.ACTIVE
        assertFalse(handler.checkIfTheConnectionIsNowActive(KLTBConnectionState.ACTIVE))
    }

    @Test
    fun `stateChanged returns ConnectionLost`() {
        val test = handler.currentStatePublishSubject.test()

        handler.stateChanged(KLTBConnectionState.TERMINATED)

        test.assertValue(State.CONNECTION_LOST)
    }

    @Test
    fun `stateChanged returns Connecting`() {
        val test = handler.currentStatePublishSubject.test()
        handler.previousBtStateActivated = KLTBConnectionState.TERMINATED

        handler.stateChanged(KLTBConnectionState.ESTABLISHING)

        test.assertValue(State.CONNECTING)
    }

    @Test
    fun `stateChanged returns Active`() {
        val test = handler.currentStatePublishSubject.test()
        handler.previousBtStateActivated = KLTBConnectionState.TERMINATED

        handler.stateChanged(KLTBConnectionState.ACTIVE)

        test.assertValue(State.CONNECTION_ACTIVE)
    }

    @Test
    fun `stateChanged assign current state to previousBtStateActivated variable`() {
        handler.previousBtStateActivated = KLTBConnectionState.TERMINATED

        handler.stateChanged(KLTBConnectionState.ACTIVE)

        assertEquals(KLTBConnectionState.ACTIVE, handler.previousBtStateActivated)
    }

    /*
    unregisterConnectionState
     */

    @Test
    fun `unregisterConnectionState does nothing if registereState contains null`() {
        handler.unregisterConnectionState()
    }

    @Test
    fun `unregisterConnectionState invokes unregister`() {
        val registeredState = mock<ConnectionState>()
        handler.registeredState = WeakReference(registeredState)

        handler.unregisterConnectionState()
        verify(registeredState).unregister(handler.connectionStateListener)
    }
}
