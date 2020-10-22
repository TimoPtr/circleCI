/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.toothbrush

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class GameToothbrushEventProviderTest : BaseUnitTest() {
    private lateinit var connection: KLTBConnection
    private val lifecycle: Lifecycle = mock()

    private lateinit var toothbrushEventProvider: GameToothbrushEventProvider

    /*
    init
     */
    @Test
    fun `eventProvider registers as lifecycle observer on construction`() {
        initEventProvider()

        verify(lifecycle).addObserver(toothbrushEventProvider)
    }

    /*
    onConnectionStateChanged
     */
    @Test
    fun `onConnectionStateChanged invokes onConnectionActive if connection is ACTIVE`() {
        spyEventProvider()

        doNothing().whenever(toothbrushEventProvider).onConnectionActive()

        toothbrushEventProvider.onConnectionStateChanged(mock(), KLTBConnectionState.ACTIVE)

        verify(toothbrushEventProvider).onConnectionActive()
        verify(toothbrushEventProvider, never()).onConnectionLost()
    }

    @Test
    fun `onConnectionStateChanged invokes onConnectionLost if connection is not active`() {
        spyEventProvider()

        doNothing().whenever(toothbrushEventProvider).onConnectionLost()

        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEachIndexed { index, newState ->
                toothbrushEventProvider.onConnectionStateChanged(mock(), newState)

                verify(toothbrushEventProvider, times(index + 1)).onConnectionLost()
                verify(toothbrushEventProvider, never()).onConnectionActive()
            }
    }

    /*
    onConnectionLost
     */
    @Test
    fun `onConnectionLost emits ConnectionLost`() {
        initEventProvider()

        val observer = toothbrushEventProvider.connectionEventStream().test()
            .assertEmpty()

        toothbrushEventProvider.onConnectionLost()

        observer.assertValue(
            GameToothbrushEvent.ConnectionLost(
                connection
            )
        )
    }

    /*
    onConnectionActive
     */
    @Test
    fun `onConnectionActive emits ConnectionActive`() {
        initEventProvider()

        val observer = toothbrushEventProvider.connectionEventStream().test()
            .assertEmpty()

        toothbrushEventProvider.onConnectionActive()

        observer.assertValue(
            GameToothbrushEvent.ConnectionActive(
                connection
            )
        )
    }

    @Test
    fun `onConnectionActive invokes onVibratorOn if connection is vibrating`() {
        spyEventProvider(
            connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(true)
                .build()
        )

        doNothing().whenever(toothbrushEventProvider).onVibratorOn()

        toothbrushEventProvider.onConnectionActive()

        verify(toothbrushEventProvider).onVibratorOn()
    }

    @Test
    fun `onConnectionActive never invokes onVibratorOn if connection is not vibrating`() {
        spyEventProvider(
            connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(false)
                .build()
        )

        toothbrushEventProvider.onConnectionActive()

        verify(toothbrushEventProvider, never()).onVibratorOn()
    }

    /*
    onVibratorStateChanged
     */
    @Test
    fun `onVibratorStateChanged invokes onVibratorOn if connection is vibrating`() {
        spyEventProvider()

        doNothing().whenever(toothbrushEventProvider).onVibratorOn()

        toothbrushEventProvider.onVibratorStateChanged(mock(), true)

        verify(toothbrushEventProvider).onVibratorOn()
        verify(toothbrushEventProvider, never()).onVibratorOff()
    }

    @Test
    fun `onVibratorStateChanged invokes onVibratorOff if connection is NOT vibrating`() {
        spyEventProvider()

        doNothing().whenever(toothbrushEventProvider).onVibratorOff()

        toothbrushEventProvider.onVibratorStateChanged(mock(), false)

        verify(toothbrushEventProvider).onVibratorOff()
        verify(toothbrushEventProvider, never()).onVibratorOn()
    }

    /*
    onVibratorOn
     */
    @Test
    fun `onVibratorOn emits VibratorOn event`() {
        spyEventProvider()

        val observer = toothbrushEventProvider.connectionEventStream().test()
            .assertEmpty()

        toothbrushEventProvider.onVibratorOn()

        observer.assertValue(
            GameToothbrushEvent.VibratorOn(
                connection
            )
        )
    }

    /*
    onVibratorOff
     */
    @Test
    fun `onVibratorOff emits VibratorOff event`() {
        initEventProvider()

        val observer = toothbrushEventProvider.connectionEventStream().test()
            .assertEmpty()

        toothbrushEventProvider.onVibratorOff()

        observer.assertValue(
            GameToothbrushEvent.VibratorOff(
                connection
            )
        )
    }

    /*
    onConnectionEstablished
     */
    @Test
    fun `onConnectionEstablished emits onConnectionEstablished event`() {
        spyEventProvider(
            KLTBConnectionBuilder.createAndroidLess()
                .withState(KLTBConnectionState.TERMINATED)
                .withVibration(false)
                .build()
        )

        val observer = toothbrushEventProvider.connectionEventStream().test()
            .assertEmpty()

        doNothing().whenever(toothbrushEventProvider).onConnectionStateChanged(any(), any())

        toothbrushEventProvider.onConnectionEstablished()

        observer.assertValue(GameToothbrushEvent.ConnectionEstablished(connection))
    }

    @Test
    fun `onConnectionEstablished never registers as connection state listener or vibrator event listener`() {
        spyEventProvider()

        doNothing().whenever(toothbrushEventProvider).onConnectionStateChanged(any(), any())

        toothbrushEventProvider.onConnectionEstablished()

        verify(connection.state(), never()).register(toothbrushEventProvider)
        verify(connection.vibrator(), never()).register(toothbrushEventProvider)
    }

    @Test
    fun `onConnectionEstablished never invokes onConnectionActive`() {
        KLTBConnectionState.values()
            .forEach { state ->
                spyEventProvider(
                    connection = KLTBConnectionBuilder.createAndroidLess()
                        .withState(state)
                        .build()
                )

                doNothing().whenever(toothbrushEventProvider).onConnectionStateChanged(any(), any())

                toothbrushEventProvider.onConnectionEstablished()

                verify(toothbrushEventProvider, never()).onConnectionActive()
            }
    }

    @Test
    fun `onConnectionEstablished never invokes onVibratorOn, even if connection is vibrating`() {
        spyEventProvider(
            connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(true)
                .build()
        )

        doNothing().whenever(toothbrushEventProvider).onVibratorOn()
        doNothing().whenever(toothbrushEventProvider).onConnectionStateChanged(any(), any())

        toothbrushEventProvider.onConnectionEstablished()

        verify(toothbrushEventProvider, never()).onVibratorOn()
    }

    @Test
    fun `onConnectionEstablished never invokes onVibratorOn if connection is not vibrating`() {
        spyEventProvider(
            connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(false)
                .build()
        )

        toothbrushEventProvider.onConnectionEstablished()

        verify(toothbrushEventProvider, never()).onVibratorOn()
    }

    /*
    onStart
     */

    @Test
    fun `onStart does nothing if there's no connection`() {
        initEventProvider()

        toothbrushEventProvider.onStart(mock())
    }

    @Test
    fun `onStart registers as vibrator listener if there's a connection`() {
        initEventProvider(connection = KLTBConnectionBuilder.createAndroidLess().build())

        toothbrushEventProvider.onStart(mock())

        verify(connection.vibrator()).register(toothbrushEventProvider)
    }

    @Test
    fun `onStart registers as state listener if there's a connection`() {
        initEventProvider(connection = KLTBConnectionBuilder.createAndroidLess().build())

        toothbrushEventProvider.onStart(mock())

        verify(connection.state()).register(toothbrushEventProvider)
    }

    /*
    onStop
     */
    @Test
    fun `onStop stops vibration if it's on`() {
        initEventProvider(
            connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(true)
                .withSupportVibrationCommands()
                .build()
        )

        toothbrushEventProvider.onStop(mock())

        verify(connection.vibrator()).off()
    }

    @Test
    fun `onStop does not stop vibration if it's off`() {
        initEventProvider(
            connection = KLTBConnectionBuilder.createAndroidLess()
                .withVibration(false)
                .build()
        )

        toothbrushEventProvider.onStop(mock())

        verify(connection.vibrator(), never()).off()
    }

    @Test
    fun `onStop unregisters as vibrator listener`() {
        initEventProvider(connection = KLTBConnectionBuilder.createAndroidLess().build())

        toothbrushEventProvider.onStop(mock())

        verify(connection.vibrator()).unregister(toothbrushEventProvider)
    }

    @Test
    fun `onStop unregisters as state listener`() {
        initEventProvider(connection = KLTBConnectionBuilder.createAndroidLess().build())

        toothbrushEventProvider.onStop(mock())

        verify(connection.state()).unregister(toothbrushEventProvider)
    }

    /*
    UTILS
     */

    private fun spyEventProvider(connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess().build()) {
        initEventProvider(connection)

        toothbrushEventProvider = spy(toothbrushEventProvider)
    }

    private fun initEventProvider(connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess().build()) {
        this.connection = connection

        toothbrushEventProvider =
            GameToothbrushEventProvider(connection, lifecycle)
    }
}
