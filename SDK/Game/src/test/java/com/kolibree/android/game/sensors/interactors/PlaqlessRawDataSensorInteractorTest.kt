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
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.randomUnsigned8
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class PlaqlessRawDataSensorInteractorTest :
    BaseSensorInteractorTestHelper<PlaqlessRawDataSensorInteractor>() {

    private val sensorListener = mock<GameSensorListener>()

    /*
    registerListeners
     */
    @Test
    fun `registerListeners does not subscribe to plaqlessRawdata if there's a previous subscription`() {
        val connection = mock<KLTBConnection>()
        initSensorInteractor(connection = connection)

        sensorInteractor.sensorDisposable = mock()
        whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(false)

        sensorInteractor.registerListeners()
    }

    @Test
    fun `registerListeners subscribes to plaqlessRawdata if sensorDisposable is null`() {
        val rawDataSubject = PublishProcessor.create<PlaqlessRawSensorState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withPlaqlessRawDetectorSupport(rawDataSubject)
                .build()
        initSensorInteractor(connection = connection)

        assertNull(sensorInteractor.sensorDisposable)

        assertFalse(rawDataSubject.hasSubscribers())

        sensorInteractor.registerListeners()

        assertTrue(rawDataSubject.hasSubscribers())
    }

    @Test
    fun `registerListeners doesn't subscribes to plaqlessRawdata if connection is not active`() {
        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val connection =
                    KLTBConnectionBuilder
                        .createAndroidLess()
                        .withState(state)
                        .build()
                initSensorInteractor(connection = connection)

                sensorInteractor.sensorDisposable = mock()
                whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(true)

                sensorInteractor.registerListeners()

                verify(connection.detectors(), never()).plaqlessRawDataNotifications()
            }
    }

    @Test
    fun `registerListeners subscribes to plaqlessRawdata if there isn't a previous subscription`() {
        val rawDataSubject = PublishProcessor.create<PlaqlessRawSensorState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withPlaqlessRawDetectorSupport(rawDataSubject)
                .build()
        initSensorInteractor(connection = connection)

        sensorInteractor.sensorDisposable = mock()
        whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(true)

        assertFalse(rawDataSubject.hasSubscribers())

        sensorInteractor.registerListeners()

        assertTrue(rawDataSubject.hasSubscribers())
    }

    @Test
    fun `registerListeners new plaqlessRawdata invokes onPlaqlessRawData`() {
        val rawDataSubject = PublishProcessor.create<PlaqlessRawSensorState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withPlaqlessRawDetectorSupport(rawDataSubject)
                .build()
        spySensorInteractor(connection = connection)

        doNothing().whenever(sensorInteractor).onPlaqlessRawData(any())

        sensorInteractor.registerListeners()

        verify(sensorInteractor, never()).onPlaqlessRawData(any())

        val sensorState = plaqlessRawSensorState()

        rawDataSubject.onNext(sensorState)

        verify(sensorInteractor).onPlaqlessRawData(sensorState)
    }

    /*
    unregisterListeners
     */

    @Test
    fun `unregisterListeners does nothing if there's no plaqlessRawDataSubscription`() {
        initSensorInteractor()

        sensorInteractor.unregisterListeners()
    }

    @Test
    fun `unregisterListeners disposes plaqlessRawDataSubscription`() {
        initSensorInteractor()

        sensorInteractor.sensorDisposable = mock()

        sensorInteractor.unregisterListeners()

        verify(sensorInteractor.sensorDisposable!!).dispose()
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
    onPlaqlessRawData
     */

    @Test
    fun `onPlaqlessRawData invokes sensorListener onPlaqlessRawData with isPlaying state`() {
        initSensorInteractor()

        val sensorState = plaqlessRawSensorState()

        setIsPlaying(true)

        sensorInteractor.onPlaqlessRawData(sensorState)

        verify(sensorListener).onPlaqlessRawData(true, sensorState)
    }

    /*
    utils
     */

    override fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ): PlaqlessRawDataSensorInteractor =
        PlaqlessRawDataSensorInteractor(
            sensorListener = sensorListener,
            gameLifecycleProvider = stageProvider,
            gameToothbrushEventProvider = brushingEventProvider
        )

    private fun plaqlessRawSensorState(): PlaqlessRawSensorState =
        PlaqlessRawSensorState(
            453L,
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8()
        )
}
