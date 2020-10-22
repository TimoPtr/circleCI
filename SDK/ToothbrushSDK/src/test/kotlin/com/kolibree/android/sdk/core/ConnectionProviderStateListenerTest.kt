package com.kolibree.android.sdk.core

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.SingleEmitter
import org.junit.Test
import org.mockito.Mock

class ConnectionProviderStateListenerTest : BaseUnitTest() {
    private lateinit var stateListener: KLTBConnectionProviderImpl.ConnectionProviderStateListener

    @Mock
    lateinit var emitter: SingleEmitter<KLTBConnection>

    @Test
    fun `Unregisters as state listener if emitter is disposed after new state`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(true)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, defaultAcceptedState)

        verify(connection.state()).unregister(stateListener)
    }

    @Test
    fun `Emits success if state is expected and emitter is not disposed`() {
        val expectedState = KLTBConnectionState.OTA
        createStateListener(listOf(expectedState))

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, expectedState)

        verify(emitter).onSuccess(connection)
    }

    @Test
    fun `Emits success if state is any of the expected and emitter is not disposed`() {
        val expectedStates = listOf(KLTBConnectionState.OTA, KLTBConnectionState.ACTIVE)
        createStateListener(expectedStates)

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, expectedStates.last())

        verify(emitter).onSuccess(connection)
    }

    @Test
    fun `Unregisters as state listener if state is ACTIVE and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, defaultAcceptedState)

        verify(connection.state()).unregister(stateListener)
    }

    @Test
    fun `Does not emit error after TERMINATED if expected state is TERMINATED`() {
        createStateListener(listOf(KLTBConnectionState.TERMINATED))

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, KLTBConnectionState.TERMINATED)

        verify(emitter).onSuccess(connection)
    }

    @Test
    fun `Emits error if state is TERMINATED and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, KLTBConnectionState.TERMINATED)

        verify(emitter).tryOnError(any())
    }

    @Test
    fun `Unregisters as state listener if state is TERMINATED and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, KLTBConnectionState.TERMINATED)

        verify(connection.state()).unregister(stateListener)
    }

    @Test
    fun `Emits error if state is TERMINATING and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, KLTBConnectionState.TERMINATING)

        verify(emitter).tryOnError(any())
    }

    @Test
    fun `Unregisters as state listener if state is TERMINATING and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        stateListener.onConnectionStateChanged(connection, KLTBConnectionState.TERMINATING)

        verify(connection.state()).unregister(stateListener)
    }

    @Test
    fun `Does not emit for other states and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        KLTBConnectionState.values()
            .filterNot { it == defaultAcceptedState || it == KLTBConnectionState.TERMINATED || it == KLTBConnectionState.TERMINATING }
            .forEach {
                stateListener.onConnectionStateChanged(connection, it)
            }

        verify(emitter, never()).tryOnError(any())
        verify(emitter, never()).onSuccess(any())
    }

    @Test
    fun `Does not unregister as state listener for other states and emitter is not disposed`() {
        createStateListener()

        whenever(emitter.isDisposed).thenReturn(false)

        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        KLTBConnectionState.values()
            .filterNot { it == defaultAcceptedState || it == KLTBConnectionState.TERMINATED || it == KLTBConnectionState.TERMINATING }
            .forEach {
                stateListener.onConnectionStateChanged(connection, it)
            }

        verify(connection.state(), never()).unregister(stateListener)
    }

    fun createStateListener(acceptedStates: List<KLTBConnectionState> = listOf(defaultAcceptedState)) {
        stateListener = KLTBConnectionProviderImpl.ConnectionProviderStateListener(emitter, acceptedStates)
    }

    private val defaultAcceptedState = KLTBConnectionState.ACTIVE
}
