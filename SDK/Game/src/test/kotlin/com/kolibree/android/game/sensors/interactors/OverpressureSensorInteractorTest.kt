/*
 * Copyright (c) 2020 Kolibree. All rights reserved
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
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** [OverpressureSensorInteractor] unit tests */
internal class OverpressureSensorInteractorTest :
    BaseSensorInteractorTestHelper<OverpressureSensorInteractor>() {

    private val sensorListener = mock<GameSensorListener>()

    /*
    registerListeners
     */
    @Test
    fun `registerListeners does not subscribe to overpressureStateFlowable if there's a previous subscription`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        initSensorInteractor(connection = connection)

        sensorInteractor.sensorDisposable = mock()
        whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(false)

        sensorInteractor.registerListeners()

        verify(connection.detectors(), never()).overpressureStateFlowable()
    }

    @Test
    fun `registerListeners subscribes to overpressureStateFlowable if sensorDisposable is null`() {
        val subject = PublishProcessor.create<OverpressureState>()
        val connection = KLTBConnectionBuilder
            .createAndroidLess().withOverpressureSensor(subject.hide()).build()
        initSensorInteractor(connection = connection)

        assertNull(sensorInteractor.sensorDisposable)

        assertFalse(subject.hasSubscribers())

        sensorInteractor.registerListeners()

        assertTrue(subject.hasSubscribers())
    }

    @Test
    fun `registerListeners doesn't subscribe to overpressureStateFlowable if connection is not active`() {
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

                verify(connection.detectors(), never()).overpressureStateFlowable()
            }
    }

    @Test
    fun `registerListeners subscribes to overpressureStateFlowable if there isn't a previous subscription`() {
        val subject = PublishProcessor.create<OverpressureState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withOverpressureSensor(subject.hide())
                .build()
        initSensorInteractor(connection = connection)

        sensorInteractor.sensorDisposable = mock()
        whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(true)

        assertFalse(subject.hasSubscribers())

        sensorInteractor.registerListeners()

        assertTrue(subject.hasSubscribers())
        verify(connection.detectors()).overpressureStateFlowable()
    }

    @Test
    fun `registerListeners new overpressure state invokes listener's onOverpressureState`() {
        val subject = PublishProcessor.create<OverpressureState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withOverpressureSensor(subject)
                .build()
        spySensorInteractor(connection = connection)

        sensorInteractor.registerListeners()

        val expectedState = OverpressureState(true, true)

        subject.onNext(expectedState)

        verify(sensorListener).onOverpressureState(expectedState)
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
    fun `unregisterListeners disposes overpressureStateFlowable`() {
        initSensorInteractor()

        sensorInteractor.sensorDisposable = mock()

        sensorInteractor.unregisterListeners()

        verify(sensorInteractor.sensorDisposable!!).dispose()
    }

    /*
    Utils
     */

    override fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ) = OverpressureSensorInteractor(
        sensorListener = sensorListener,
        gameToothbrushEventProvider = brushingEventProvider,
        gameLifecycleProvider = stageProvider
    )
}
