/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.interactor

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleOwner
import com.google.common.base.Optional
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GameInteractorTest : BaseUnitTest() {
    private val serviceInteractor = mock<KolibreeServiceInteractor>()

    @Test
    fun `preFilledToothbrushMac absent set toothbrushMac to null`() {
        val gameInteractor = createGameInteractor()

        assertNull(gameInteractor.toothbrushMac)
    }

    @Test
    fun `preFilledToothbrushMac present set toothbrushMac to mac`() {
        val expectedMac = "hello"
        val gameInteractor = createGameInteractor(expectedMac)

        assertEquals(expectedMac, gameInteractor.toothbrushMac)
    }

    @Test
    fun `invokes shouldProceedWithVibrationDelegate return true when isDestroy false`() {
        val gameInteractor = createGameInteractor()
        assertTrue(gameInteractor.shouldProceedWithVibrationDelegate.invoke())
    }

    @Test
    fun `invokes shouldProceedWithVibrationDelegate return false when isDestroy true`() {
        val gameInteractor = createGameInteractor()
        gameInteractor.isDestroyed.set(true)
        assertFalse(gameInteractor.shouldProceedWithVibrationDelegate.invoke())
    }

    @Test
    fun `onCreateInternal invokes service add listener and maybeRegisterToMainConnection and maybeRegisterToAllConnections`() {
        val gameInteractor = createGameInteractor()
        doNothing().whenever(gameInteractor).maybeRegisterToAllConnections()
        doNothing().whenever(gameInteractor).maybeRegisterToMainConnection()
        doNothing().whenever(serviceInteractor).addListener(gameInteractor)

        gameInteractor.onCreateInternal(mock())

        verify(serviceInteractor).addListener(gameInteractor)
        verify(gameInteractor).maybeRegisterToMainConnection()
        verify(gameInteractor).maybeRegisterToAllConnections()
    }

    @Test
    fun `onDestroyInternal invokes unregisterFromAllConnections unregisterFromMainConnection and remove listener from service and set isDestroy to true`() {
        val gameInteractor = createGameInteractor()
        doNothing().whenever(gameInteractor).unregisterFromAllConnections()
        doNothing().whenever(gameInteractor).unregisterFromMainConnection()

        doNothing().whenever(serviceInteractor).removeListener(gameInteractor)

        gameInteractor.onDestroyInternal()

        verify(serviceInteractor).removeListener(gameInteractor)
        verify(gameInteractor).unregisterFromMainConnection()
        verify(gameInteractor).unregisterFromAllConnections()
        assertTrue(gameInteractor.isDestroyed.get())
    }

    @Test
    fun `setLifecycleOwnerInternal invokes setLifecycleOwner on the service`() {
        val gameInteractor = createGameInteractor()
        val lifecycleOwner: LifecycleOwner = mock()
        doNothing().whenever(serviceInteractor).setLifecycleOwner(lifecycleOwner)

        gameInteractor.setLifecycleOwner(lifecycleOwner)

        verify(serviceInteractor).setLifecycleOwner(lifecycleOwner)
    }

    @Test
    fun `onKolibreeServiceConnected invokes onToothbrushMacReceived when toothbrushMac not null and notify each listener`() {
        val expectedMac = "SuperMac"
        val gameInteractor = createGameInteractor(expectedMac)
        val service: KolibreeService = mock()
        val listener1: GameInteractor.Listener = mock()
        val listener2: GameInteractor.Listener = mock()

        gameInteractor.setLifecycleOwner(mock())

        gameInteractor.addListener(listener1)
        gameInteractor.addListener(listener2)

        doNothing().whenever(listener1).onKolibreeServiceConnected(service)
        doNothing().whenever(listener2).onKolibreeServiceConnected(service)

        doNothing().whenever(gameInteractor).onToothbrushMacReceived(expectedMac)

        whenever(service.knownConnections).thenReturn(emptyList())

        gameInteractor.onKolibreeServiceConnected(service)

        verify(gameInteractor).onToothbrushMacReceived(expectedMac)
        verify(listener1).onKolibreeServiceConnected(service)
        verify(listener2).onKolibreeServiceConnected(service)
    }

    @Test
    fun `onKolibreeServiceConnected set vibratorOn when service has the connection we care about`() {
        val expectedMac = "SuperMac"
        val gameInteractor = createGameInteractor(expectedMac)
        val service = mock<KolibreeService>()

        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()

        whenever(service.knownConnections).thenReturn(listOf(connection))

        doNothing().whenever(gameInteractor).onToothbrushMacReceived(expectedMac)

        gameInteractor.onKolibreeServiceConnected(service)

        assertTrue(gameInteractor.vibratorOn.get())
    }

    @Test
    fun `onKolibreeServiceConnected does not set vibratorOn when service has not the connection we care about`() {
        val expectedMac = "SuperMac"
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()

        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac("otherMac").withVibration(true)
                .build()

        whenever(service.knownConnections).thenReturn(listOf(connection))

        doNothing().whenever(gameInteractor).onToothbrushMacReceived(expectedMac)

        gameInteractor.onKolibreeServiceConnected(service)

        assertFalse(gameInteractor.vibratorOn.get())
    }

    @Test
    fun `onKolibreeServiceConnected invokes onToothbrushMacReceived when no mac and service only knows one connection and set vibrator when vibration on`() {
        val expectedMac = "SuperMac"
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection))

        gameInteractor.onKolibreeServiceConnected(service)

        verify(gameInteractor, times(2)).onToothbrushMacReceived(expectedMac)
        assertTrue(gameInteractor.vibratorOn.get())
    }

    @Test
    fun `onKolibreeServiceConnected invokes maybeRegisterToAllConnections when no mac and service has multiple connection`() {
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection1 = KLTBConnectionBuilder.createAndroidLess().withMac("1").build()
        val connection2 =
            KLTBConnectionBuilder.createAndroidLess().withMac("2").withVibration(true).build()

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection1, connection2))

        doNothing().whenever(gameInteractor).maybeRegisterToAllConnections()

        gameInteractor.onKolibreeServiceConnected(service)

        verify(gameInteractor).maybeRegisterToAllConnections()
    }

    @Test
    fun `onKolibreeServiceDisconnected invokes unregisterFromAllConnections and unregisterFromMainConnection and nullify connection and notify listeners`() {
        val gameInteractor = createGameInteractor()
        val listener1: GameInteractor.Listener = mock()
        val listener2: GameInteractor.Listener = mock()

        gameInteractor.setLifecycleOwner(mock())

        gameInteractor.addListener(listener1)
        gameInteractor.addListener(listener2)

        gameInteractor.connection = mock()

        doNothing().whenever(listener1).onKolibreeServiceDisconnected()
        doNothing().whenever(listener2).onKolibreeServiceDisconnected()

        doNothing().whenever(gameInteractor).unregisterFromAllConnections()
        doNothing().whenever(gameInteractor).unregisterFromMainConnection()

        gameInteractor.onKolibreeServiceDisconnected()

        verify(listener1).onKolibreeServiceDisconnected()
        verify(listener2).onKolibreeServiceDisconnected()

        verify(gameInteractor).unregisterFromMainConnection()
        verify(gameInteractor).unregisterFromAllConnections()

        assertNull(gameInteractor.connection)
    }

    @Test
    fun `onVibratorStateChanged do nothing when shouldProceedWithVibrationDelegate return false`() {
        val gameInteractor = createGameInteractor()
        val connection = KLTBConnectionBuilder.createAndroidLess().withMac("").build()

        gameInteractor.shouldProceedWithVibrationDelegate = { false }

        gameInteractor.onVibratorStateChanged(connection, true)

        verify(gameInteractor, never()).onToothbrushMacReceived(any())
        verify(gameInteractor, never()).weCareAboutThisConnection(any())
    }

    @Test
    fun `onVibratorStateChanged invokes onToothbrushMacReceived when shouldProceedWithVibrationDelegate return true and connection null and vibrator on`() {
        val expectedMac = "hello"
        val gameInteractor = createGameInteractor()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()

        gameInteractor.shouldProceedWithVibrationDelegate = { true }

        doReturn(false).whenever(gameInteractor).weCareAboutThisConnection(connection)

        gameInteractor.onVibratorStateChanged(connection, true)

        verify(gameInteractor).onToothbrushMacReceived(expectedMac)
        verify(gameInteractor).weCareAboutThisConnection(connection)
    }

    @Test
    fun `onVibratorStateChanged does not invokes onToothbrushMacReceived when shouldProceedWithVibrationDelegate return true and connection not null and vibrator on`() {
        val expectedMac = "hello"
        val gameInteractor = createGameInteractor()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()

        gameInteractor.shouldProceedWithVibrationDelegate = { true }
        gameInteractor.connection = connection

        doReturn(false).whenever(gameInteractor).weCareAboutThisConnection(connection)

        gameInteractor.onVibratorStateChanged(connection, true)

        verify(gameInteractor, never()).onToothbrushMacReceived(expectedMac)
        verify(gameInteractor).weCareAboutThisConnection(connection)
    }

    @Test
    fun `onVibratorStateChanged does not invokes onToothbrushMacReceived when shouldProceedWithVibrationDelegate return true and connection null and vibrator off`() {
        val expectedMac = "hello"
        val gameInteractor = createGameInteractor()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()

        gameInteractor.shouldProceedWithVibrationDelegate = { true }

        doReturn(false).whenever(gameInteractor).weCareAboutThisConnection(connection)

        gameInteractor.onVibratorStateChanged(connection, false)

        verify(gameInteractor, never()).onToothbrushMacReceived(expectedMac)
        verify(gameInteractor).weCareAboutThisConnection(connection)
    }

    @Test
    fun `onVibratorStateChanged notify listener onVibratorOn when vibratorOn is false and is connection we care`() {
        val expectedMac = "hello"
        val gameInteractor = createGameInteractor()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()
        val listener1: GameInteractor.Listener = mock()
        val listener2: GameInteractor.Listener = mock()

        gameInteractor.setLifecycleOwner(mock())

        gameInteractor.addListener(listener1)
        gameInteractor.addListener(listener2)

        doNothing().whenever(listener1).onVibratorOn(connection)
        doNothing().whenever(listener2).onVibratorOn(connection)

        gameInteractor.shouldProceedWithVibrationDelegate = { true }
        gameInteractor.vibratorOn.set(false)

        doReturn(true).whenever(gameInteractor).weCareAboutThisConnection(connection)

        gameInteractor.onVibratorStateChanged(connection, true)

        verify(gameInteractor).weCareAboutThisConnection(connection)

        verify(listener1).onVibratorOn(connection)
        verify(listener2).onVibratorOn(connection)
    }

    @Test
    fun `onVibratorStateChanged notify listener onVibratorOff when vibratorOn is true and is connection we care`() {
        val expectedMac = "hello"
        val gameInteractor = createGameInteractor()
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).withVibration(true)
                .build()
        val listener1: GameInteractor.Listener = mock()
        val listener2: GameInteractor.Listener = mock()

        gameInteractor.setLifecycleOwner(mock())

        gameInteractor.addListener(listener1)
        gameInteractor.addListener(listener2)

        doNothing().whenever(listener1).onVibratorOff(connection)
        doNothing().whenever(listener2).onVibratorOff(connection)

        gameInteractor.shouldProceedWithVibrationDelegate = { true }
        gameInteractor.vibratorOn.set(true)

        doReturn(true).whenever(gameInteractor).weCareAboutThisConnection(connection)

        gameInteractor.onVibratorStateChanged(connection, false)

        verify(gameInteractor).weCareAboutThisConnection(connection)

        verify(listener1).onVibratorOff(connection)
        verify(listener2).onVibratorOff(connection)
    }

    @Test
    fun `onConnectionStateChanged notify listeners with onConnectionStateChanged`() {
        val gameInteractor = createGameInteractor()
        val connection = mock<KLTBConnection>()
        val state: KLTBConnectionState = KLTBConnectionState.ACTIVE

        val listener1: GameInteractor.Listener = mock()
        val listener2: GameInteractor.Listener = mock()

        gameInteractor.setLifecycleOwner(mock())

        gameInteractor.addListener(listener1)
        gameInteractor.addListener(listener2)

        doNothing().whenever(listener1).onConnectionStateChanged(connection, state)
        doNothing().whenever(listener2).onConnectionStateChanged(connection, state)

        gameInteractor.onConnectionStateChanged(connection, state)

        verify(listener1).onConnectionStateChanged(connection, state)
        verify(listener2).onConnectionStateChanged(connection, state)
    }

    @Test
    fun `onConnectionStateChanged sets connectionWasLost to true if newState is different than ACTIVE`() {
        val gameInteractor = createGameInteractor()
        val connection = mock<KLTBConnection>()

        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { newState ->
                gameInteractor.connectionWasLost.set(false)

                gameInteractor.onConnectionStateChanged(connection, newState)

                assertTrue("Failed for $newState", gameInteractor.connectionWasLost.get())
            }
    }

    @Test
    fun `onConnectionStateChanged doesn't touch connectionWasLost if newState is ACTIVE`() {
        val gameInteractor = createGameInteractor()
        val connection = mock<KLTBConnection>()

        gameInteractor.vibratorOn.set(true)

        gameInteractor.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        assertTrue(gameInteractor.vibratorOn.get())
    }

    @Test
    fun `getToothbrushMacSingle return mac from toothbrushMacGetter and set toothbrushMac`() {
        val gameInteractor = createGameInteractor()
        val expectedMac = "mac"
        gameInteractor.toothbrushMacGetter = { expectedMac }

        gameInteractor.getToothbrushMacSingle().test().assertValue(expectedMac)

        assertEquals(expectedMac, gameInteractor.toothbrushMac)
    }

    @Test
    fun `getToothbrushMacSingle return first mac from macSubject when toothbrushMacGetter is null and set toothbrushMac`() {
        val gameInteractor = createGameInteractor()
        val expectedMac = "mac"
        gameInteractor.toothbrushMacGetter = null

        val testObserver = gameInteractor.getToothbrushMacSingle().test()
        gameInteractor.macSubject.accept(expectedMac)
        gameInteractor.macSubject.accept("garbage")
        testObserver.assertValue(expectedMac)
        testObserver.assertComplete()

        assertEquals(expectedMac, gameInteractor.toothbrushMac)
    }

    @Test
    fun `weCareAboutThisConnection return true when toothbrushMac equals to connection mac`() {
        val expectedMac = "BigMac"
        val gameInteractor = createGameInteractor(expectedMac)
        val connection = KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).build()
        assertTrue(gameInteractor.weCareAboutThisConnection(connection))
    }

    @Test
    fun `weCareAboutThisConnection return false when toothbrushMac is null`() {
        val expectedMac = "BigMac"
        val gameInteractor = createGameInteractor()
        val connection = KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).build()
        assertFalse(gameInteractor.weCareAboutThisConnection(connection))
    }

    @Test
    fun `weCareAboutThisConnection return false when toothbrushMac not equals to connection mac`() {
        val expectedMac = "BigMac"
        val gameInteractor = createGameInteractor("hello")
        val connection = KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).build()
        assertFalse(gameInteractor.weCareAboutThisConnection(connection))
    }

    @Test
    fun `onToothbrushMacReceived doNothing when service is null`() {
        val gameInteractor = createGameInteractor("hello")

        gameInteractor.onToothbrushMacReceived("")

        verify(gameInteractor, never()).maybeRegisterToMainConnection()
    }

    @Test
    fun `onToothbrushMacReceived invokes getConnection and maybeRegisterToMainConnection and set toothbrushMac and emit on macSubject and invokes unregisterFromAllConnections and notify listeners`() {
        val gameInteractor = createGameInteractor()
        val expectedMac = "littleMac"
        val service = mock<KolibreeService>()
        val connection = KLTBConnectionBuilder.createAndroidLess().withMac(expectedMac).build()
        val testObserver = gameInteractor.macSubject.test()
        val listener1: GameInteractor.Listener = mock()
        val listener2: GameInteractor.Listener = mock()

        gameInteractor.setLifecycleOwner(mock())

        gameInteractor.addListener(listener1)
        gameInteractor.addListener(listener2)

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.getConnection(expectedMac)).thenReturn(connection)

        doNothing().whenever(gameInteractor).maybeRegisterToMainConnection()
        doNothing().whenever(listener1).onConnectionEstablished()
        doNothing().whenever(listener2).onConnectionEstablished()

        gameInteractor.onToothbrushMacReceived(expectedMac)

        testObserver.assertValue(expectedMac)
        verify(gameInteractor, times(2)).maybeRegisterToMainConnection()
        verify(gameInteractor).unregisterFromAllConnections(expectedMac)
        assertEquals(connection, gameInteractor.connection)
        verify(listener1).onConnectionEstablished()
        verify(listener2).onConnectionEstablished()
    }

    @Test
    fun `maybeRegisterToMainConnection register to connection state and vibrator`() {
        val gameInteractor = createGameInteractor()
        val connection = mock<KLTBConnection>()
        val state = mock<ConnectionState>()
        val vibrator = mock<Vibrator>()

        gameInteractor.connection = connection

        whenever(connection.state()).thenReturn(state)
        whenever(connection.vibrator()).thenReturn(vibrator)

        doNothing().whenever(state).register(gameInteractor)
        doNothing().whenever(vibrator).register(gameInteractor)

        gameInteractor.maybeRegisterToMainConnection()

        verify(connection.state()).register(gameInteractor)
        verify(connection.vibrator()).register(gameInteractor)
    }

    @Test
    fun `maybeRegisterToMainConnection doNothing when connection null`() {
        val gameInteractor = createGameInteractor()

        gameInteractor.maybeRegisterToMainConnection()
    }

    /*
    onStop
     */

    @Test
    fun `onStop unregisters as vibrator listener from mainConnection if it's not null`() {
        val gameInteractor = GameInteractor(serviceInteractor, Optional.absent())

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        gameInteractor.connection = connection

        gameInteractor.onStopInternal()

        verify(connection.vibrator()).unregister(gameInteractor)
    }

    @Test
    fun `onStop sets vibratorOn to false`() {
        val gameInteractor = GameInteractor(serviceInteractor, Optional.absent())

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        gameInteractor.connection = connection

        gameInteractor.vibratorOn.set(true)

        gameInteractor.onStopInternal()

        assertFalse(gameInteractor.vibratorOn.get())
    }

    @Test
    fun `onStop doesn't crash if mainConnection is null`() {
        GameInteractor(serviceInteractor, Optional.absent()).onStopInternal()
    }

    /*
    onStart
     */

    @Test
    fun `onStart registers as vibrator listener from mainConnection if it's not null`() {
        val gameInteractor = GameInteractor(serviceInteractor, Optional.absent())

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        gameInteractor.connection = connection

        gameInteractor.onStartInternal()

        verify(connection.vibrator()).register(gameInteractor)
    }

    @Test
    fun `onStart doesn't crash if mainConnection is null`() {
        GameInteractor(serviceInteractor, Optional.absent()).onStartInternal()
    }

    /*
    maybeRegisterToAllConnections
     */

    @Test
    fun `maybeRegisterToAllConnections invokes onToothbrushMacReceived when one of the connection has vibrator on`() {
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection1 = KLTBConnectionBuilder.createAndroidLess().withMac("1").build()
        val connection2 =
            KLTBConnectionBuilder.createAndroidLess().withMac("2").withVibration(true).build()

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection1, connection2))

        doNothing().whenever(gameInteractor).onToothbrushMacReceived(any())

        gameInteractor.maybeRegisterToAllConnections()

        verify(gameInteractor).onToothbrushMacReceived(eq("2"))
    }

    @Test
    fun `maybeRegisterToAllConnections doesnt invokes onToothbrushMacReceived if connection already set`() {
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection1 = KLTBConnectionBuilder.createAndroidLess().withMac("1").build()
        val connection2 =
            KLTBConnectionBuilder.createAndroidLess().withMac("2").withVibration(true).build()

        gameInteractor.connection = connection1

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection1, connection2))

        doNothing().whenever(gameInteractor).onToothbrushMacReceived(any())

        gameInteractor.maybeRegisterToAllConnections()

        verify(gameInteractor, never()).onToothbrushMacReceived(any())
    }

    @Test
    fun `maybeRegisterToAllConnections doesnt invokes onToothbrushMacReceived if all connection not vibrating`() {
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection1 =
            KLTBConnectionBuilder.createAndroidLess().withMac("1").withVibration(false).build()
        val connection2 =
            KLTBConnectionBuilder.createAndroidLess().withMac("2").withVibration(false).build()

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection1, connection2))

        doNothing().whenever(gameInteractor).onToothbrushMacReceived(any())

        gameInteractor.maybeRegisterToAllConnections()

        verify(gameInteractor, never()).onToothbrushMacReceived(any())
    }

    /*
    unregisterFromAllConnections
     */

    @Test
    fun `unregisterFromMainConnection unregister to connection state and vibrator`() {
        val gameInteractor = createGameInteractor()
        val connection = mock<KLTBConnection>()
        val state = mock<ConnectionState>()
        val vibrator = mock<Vibrator>()
        gameInteractor.connection = connection

        whenever(connection.state()).thenReturn(state)
        whenever(connection.vibrator()).thenReturn(vibrator)

        doNothing().whenever(state).unregister(gameInteractor)
        doNothing().whenever(vibrator).unregister(gameInteractor)

        gameInteractor.unregisterFromMainConnection()

        verify(connection.state()).unregister(gameInteractor)
        verify(connection.vibrator()).unregister(gameInteractor)
    }

    @Test
    fun `unregisterFromMainConnection doNothing when connection null`() {
        val gameInteractor = createGameInteractor()

        gameInteractor.unregisterFromMainConnection()
    }

    @Test
    fun `unregisterFromAllConnections unregister all connection if exceptThisMac doesnt match any`() {
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection1 = KLTBConnectionBuilder.createAndroidLess().withMac("1").build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().withMac("2").build()

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection1, connection2))

        gameInteractor.unregisterFromAllConnections()

        verify(connection1.state()).unregister(eq((gameInteractor)))
        verify(connection1.vibrator()).unregister(eq((gameInteractor)))
        verify(connection2.state()).unregister(eq((gameInteractor)))
        verify(connection2.vibrator()).unregister(eq((gameInteractor)))
    }

    @Test
    fun `unregisterFromAllConnections unregister only connection which is not exceptThisMac`() {
        val gameInteractor = createGameInteractor()
        val service = mock<KolibreeService>()
        val connection1 = KLTBConnectionBuilder.createAndroidLess().withMac("1").build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().withMac("2").build()

        whenever(serviceInteractor.service).thenReturn(service)
        whenever(service.knownConnections).thenReturn(listOf(connection1, connection2))

        gameInteractor.unregisterFromAllConnections("1")

        verify(connection1.state(), never()).unregister(eq((gameInteractor)))
        verify(connection1.vibrator(), never()).unregister(eq((gameInteractor)))
        verify(connection2.state()).unregister(eq((gameInteractor)))
        verify(connection2.vibrator()).unregister(eq((gameInteractor)))
    }

    @Test
    fun `resetToothbrushConnection nullify toothbrushMac and connection`() {
        val gameInteractor = createGameInteractor("hello")
        gameInteractor.connection = mock()

        gameInteractor.resetToothbrushConnection()

        assertNull(gameInteractor.connection)
        assertNull(gameInteractor.toothbrushMac)
    }

    @Test
    fun `onKolibreeServiceConnected invokes onVibratorStateChanged`() {
        val mac = "mac:tb:001"
        val gameInteractor = createGameInteractor(mac)
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(mac)
            .withVibration(true)
            .build()
        val service = mock<KolibreeService>()
        whenever(service.knownConnections).thenReturn(listOf(connection))
        gameInteractor.onKolibreeServiceConnected(service)

        verify(gameInteractor).onVibratorStateChanged(connection, true)
    }

    @SuppressLint("ExperimentalClassUse")
    fun createGameInteractor(macAddress: String? = null): GameInteractor =
        spy(GameInteractor(serviceInteractor, Optional.fromNullable(macAddress)))
}
