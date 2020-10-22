/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ESTABLISHING
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.NEW
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.OTA
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.values
import com.kolibree.android.sdk.core.notification.ListenerNotifier
import com.kolibree.android.sdk.core.notification.ListenerPool
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ConnectionStateImplTest : BaseUnitTest() {
    private val listenerPool: ListenerPool<ConnectionStateListener> = mock()
    private val handler: Handler = mock()

    private val connection: KLTBConnection = mock()

    private val connectionState = ConnectionStateImpl(connection, listenerPool, handler)

    override fun setup() {
        super.setup()

        whenever(handler.post(any())).thenAnswer {
            it.getArgument<Runnable>(0).run()
            true
        }
    }

    /*
    init
     */
    @Test
    fun `stream is init with state value`() {
        connectionState.stateStream.test().assertValue(connectionState.state.get())
    }

    /*
    register
     */

    @Test
    fun `register adds listener to listenerPool`() {
        val listener = mock<ConnectionStateListener>()
        connectionState.register(listener)

        verify(listenerPool).add(listener)
    }

    @Test
    fun `register only notifies the listener parameter of current state if listener is added to the pool`() {
        val expectedState = ESTABLISHING
        connectionState.state.set(expectedState)

        val listener = mock<ConnectionStateListener>()

        val sizePreAdd = 1
        val sizePostAdd = 2
        whenever(listenerPool.size()).thenReturn(sizePreAdd)
        whenever(listenerPool.add(listener)).thenReturn(sizePostAdd)

        connectionState.register(listener)

        val otherListenerInPool = mock<ConnectionStateListener>()

        argumentCaptor<ListenerNotifier<ConnectionStateListener>> {
            verify(listenerPool).notifyListeners(capture())

            firstValue.notifyListener(listener)
            firstValue.notifyListener(otherListenerInPool)
        }

        verify(listenerPool).notifyListeners(any<ListenerNotifier<ConnectionStateListener>>())
        verify(listener).onConnectionStateChanged(connection, expectedState)
        verify(otherListenerInPool, never()).onConnectionStateChanged(connection, expectedState)
    }

    @Test
    fun `register doesn't notify the listener parameter if it wasn't added to the pool`() {
        val expectedState = ESTABLISHING
        connectionState.state.set(expectedState)

        val listener = mock<ConnectionStateListener>()

        val sizePreAdd = 1
        whenever(listenerPool.size()).thenReturn(sizePreAdd)
        whenever(listenerPool.add(listener)).thenReturn(sizePreAdd)

        connectionState.register(listener)

        verify(listenerPool, never())
            .notifyListeners(any<ListenerNotifier<ConnectionStateListener>>())
        verify(listener, never()).onConnectionStateChanged(connection, expectedState)
    }

    /*
    unregister
     */

    @Test
    fun `unregister removes listener from listenerPool`() {
        val listener = mock<ConnectionStateListener>()
        connectionState.unregister(listener)

        verify(listenerPool).remove(listener)
    }

    /*
    set
     */

    @Test
    fun `set notifies listeners of new state`() {
        val expectedState = ESTABLISHING
        connectionState.set(expectedState)

        val listener = mock<ConnectionStateListener>()

        argumentCaptor<ListenerNotifier<ConnectionStateListener>> {
            verify(listenerPool).notifyListeners(capture())

            firstValue.notifyListener(listener)

            verify(listener).onConnectionStateChanged(connection, expectedState)
        }
    }

    @Test
    fun `set emits new state in stream`() {
        val expectedState = ESTABLISHING
        connectionState.set(expectedState)

        connectionState.stateStream.test().assertValue(expectedState)
    }

    /*
    clearCache
     */

    @Test
    fun `clearCache sets state NEW if it's not OTA`() {
        values()
            .filterNot { it == OTA }
            .forEach { state ->
                connectionState.state.set(state)

                connectionState.clearCache()

                assertEquals(NEW, connectionState.state.get())
            }
    }

    @Test
    fun `clearCache doesn't change current state if it's OTA`() {
        connectionState.state.set(OTA)

        connectionState.clearCache()

        assertEquals(OTA, connectionState.current)
    }
}
