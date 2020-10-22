/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.createRawSensorState
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class RawSensorInteractorTest : BaseSensorInteractorTestHelper<RawSensorInteractor>() {
    private val sensorListener = mock<GameSensorListener>()

    /*
    registerListeners
     */
    @Test
    fun `registerListeners invokes setupRawDataRecorder`() {
        val connection = createSensorTestConnection()
        spySensorInteractor(connection = connection)

        doNothing().whenever(sensorInteractor).maybeRegisterRawDetectorListener()

        sensorInteractor.registerListeners()

        verify(sensorInteractor).setupRawDataListener()
    }

    /*
    registerDelayableListeners
     */
    @Test
    fun `registerDelayableListeners does nothing`() {
        spySensorInteractor()

        sensorInteractor.registerDelayableListeners()

        verify(sensorInteractor).registerDelayableListeners()

        verifyNoMoreInteractions(sensorInteractor)
    }

    /*
    unregisterListeners
     */
    @Test
    fun `unregisterListeners invokes unregisterRawDetectorListener`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).unregisterRawDetectorListener()

        sensorInteractor.unregisterListeners()

        verify(sensorInteractor).unregisterRawDetectorListener()
    }

    /*
    unregisterRawDetectorListener
     */
    @Test
    fun `unregisterRawDetectorListener does nothing if rawDetectorListener is null`() {
        initSensorInteractor()

        assertNull(sensorInteractor.rawDetectorListener)

        sensorInteractor.unregisterRawDetectorListener()
    }

    @Test
    fun `unregisterRawDetectorListener unregisters as raw data listener if rawDetectorListener is not null`() {
        val connection = createSensorTestConnection()
        initSensorInteractor(connection = connection)

        val rawDetectorListener = mock<RawDetectorListener>()
        sensorInteractor.rawDetectorListener = rawDetectorListener

        sensorInteractor.unregisterRawDetectorListener()

        verify(connection.detectors().rawData()).unregister(rawDetectorListener)
    }

    @Test
    fun `unregisterRawDetectorListener invokes disableRawDataNotifications if rawDetectorListener is not null`() {
        spySensorInteractor(connection = createSensorTestConnection())

        sensorInteractor.rawDetectorListener = mock()

        sensorInteractor.unregisterRawDetectorListener()

        verify(sensorInteractor).disableRawDataNotifications()
    }

    @Test
    fun `unregisterRawDetectorListener nullifies rawDetectorListener`() {
        initSensorInteractor(connection = createSensorTestConnection())

        sensorInteractor.rawDetectorListener = mock()

        sensorInteractor.unregisterRawDetectorListener()

        assertNull(sensorInteractor.rawDetectorListener)
    }

    @Test
    fun `unregisterRawDetectorListener sets registeredRawDetectorListener to false`() {
        initSensorInteractor(connection = createSensorTestConnection())

        sensorInteractor.registeredRawDetectorListener = true

        sensorInteractor.unregisterRawDetectorListener()

        assertFalse(sensorInteractor.registeredRawDetectorListener)
    }

    /*
    enableRawDataNotifications
     */

    @Test
    fun `enableRawDataNotifications invokes enableRawDataNotifications on Detectors if connetion is ACTIVE`() {
        val connection = createSensorTestConnection(KLTBConnectionState.ACTIVE)
        initSensorInteractor(connection = connection)

        sensorInteractor.enableRawDataNotifications()

        verify(connection.detectors()).enableRawDataNotifications()
    }

    @Test
    fun `enableRawDataNotifications never invokes enableRawDataNotifications on Detectors if connetion is not ACTIVE`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val connection = createSensorTestConnection(state)
                initSensorInteractor(connection = connection)

                sensorInteractor.enableRawDataNotifications()

                verify(connection.detectors(), never()).enableRawDataNotifications()
            }
    }

    /*
    disableRawDataNotifications
     */

    @Test
    fun `disableRawDataNotifications invokes disableRawDataNotifications on Detectors if connetion is ACTIVE`() {
        val connection = createSensorTestConnection(KLTBConnectionState.ACTIVE)
        initSensorInteractor(connection = connection)

        sensorInteractor.disableRawDataNotifications()

        verify(connection.detectors()).disableRawDataNotifications()
    }

    @Test
    fun `disableRawDataNotifications never invokes disableRawDataNotifications on Detectors if connetion is not ACTIVE`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val connection = createSensorTestConnection(state)
                initSensorInteractor(connection = connection)

                sensorInteractor.disableRawDataNotifications()

                verify(connection.detectors(), never()).disableRawDataNotifications()
            }
    }

    /*
    SETUP RAW DATA RECORDER
     */

    @Test
    fun `setupRawDataRecorder invokes initRawDataListener`() {
        val connection = createSensorTestConnection()
        spySensorInteractor(connection = connection)

        sensorInteractor.setupRawDataListener()

        verify(sensorInteractor).initRawDataListener()
    }

    @Test
    fun `setupRawDataRecorder invokes maybeRegisterRawDetectorListener`() {
        val connection = createSensorTestConnection()
        spySensorInteractor(connection = connection)

        sensorInteractor.setupRawDataListener()

        verify(sensorInteractor).maybeRegisterRawDetectorListener()
    }

    @Test
    fun `setupRawDataRecorder invokes enableRawDataNotifications`() {
        val connection = createSensorTestConnection()
        initSensorInteractor(connection = connection)

        sensorInteractor.setupRawDataListener()

        verify(connection.detectors()).enableRawDataNotifications()
    }

    /*
    MAYBE REGISTER RAW DETECTOR LISTENER
     */
    @Test
    fun `maybeRegisterRawDetectorListener registers as mouth zone listeners if registeredRawDetectorListener=false and rawDetectorListener is not null, and then sets registeredRawDetectorListener=true`() {
        val connection = createSensorTestConnection()
        initSensorInteractor(connection = connection)

        assertFalse(sensorInteractor.registeredRawDetectorListener)

        sensorInteractor.rawDetectorListener = mock()

        sensorInteractor.maybeRegisterRawDetectorListener()

        verify(connection.detectors().rawData()).register(sensorInteractor.rawDetectorListener!!)

        assertTrue(sensorInteractor.registeredRawDetectorListener)
    }

    @Test
    fun `maybeRegisterRawDetectorListener does not register as listeners if registeredRawDetectorListener=false and rawDetectorListener is null, but still sets registeredRawDetectorListener=true`() {
        val connection = createSensorTestConnection()
        initSensorInteractor(connection = connection)

        assertFalse(sensorInteractor.registeredRawDetectorListener)
        assertNull(sensorInteractor.rawDetectorListener)

        sensorInteractor.maybeRegisterRawDetectorListener()

        verify(connection.detectors().rawData(), never()).register(any())

        assertTrue(sensorInteractor.registeredRawDetectorListener)
    }

    @Test
    fun `maybeRegisterRawDetectorListener does nothing if registeredRawDetectorListener=true`() {
        val connection = createSensorTestConnection()
        initSensorInteractor(connection = connection)

        sensorInteractor.registeredRawDetectorListener = true

        sensorInteractor.maybeRegisterRawDetectorListener()

        verify(connection.detectors().rawData(), never()).register(any())
        assertTrue(sensorInteractor.registeredRawDetectorListener)
    }

    @Test
    fun `maybeRegisterRawDetectorListener does nothing if connection is not active`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val connection = createSensorTestConnection(state)
                initSensorInteractor(connection = connection)

                assertFalse(sensorInteractor.registeredRawDetectorListener)

                sensorInteractor.maybeRegisterRawDetectorListener()

                verify(connection.detectors().rawData(), never()).register(any())

                assertFalse(sensorInteractor.registeredRawDetectorListener)
            }
    }

    /*
    INIT RAW DATA LISTENER
     */
    @Test
    fun `initRawDataListener stores rawDetectorListener`() {
        initSensorInteractor()

        assertNull(sensorInteractor.rawDetectorListener)

        sensorInteractor.initRawDataListener()

        assertNotNull(sensorInteractor.rawDetectorListener)
    }

    /*
    RAW DATA LISTENER
     */

    @Test
    fun `rawDataListener sends data to listener if isPlaying is false`() {
        initSensorInteractor()

        setIsPlaying(false)

        whenever(sensorListener.currentZone()).thenReturn(null)

        sensorInteractor.initRawDataListener()
        val expectedSensorState = createRawSensorState()
        sensorInteractor.rawDetectorListener!!.onRawData(mock(), expectedSensorState)

        verify(sensorListener).onRawData(false, expectedSensorState)
    }

    @Test
    fun `rawDataListener sends data to listener if isPlaying is true`() {
        initSensorInteractor()

        setIsPlaying(true)

        whenever(sensorListener.currentZone()).thenReturn(null)

        sensorInteractor.initRawDataListener()
        val expectedSensorState = createRawSensorState()
        sensorInteractor.rawDetectorListener!!.onRawData(mock(), expectedSensorState)

        verify(sensorListener).onRawData(true, expectedSensorState)
    }

    /*
    utils
     */

    override fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ): RawSensorInteractor {
        return RawSensorInteractor(
            sensorListener = sensorListener,
            gameLifecycleProvider = stageProvider,
            gameToothbrushEventProvider = brushingEventProvider
        )
    }
}
