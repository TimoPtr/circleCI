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
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.randomUnsigned8
import com.kolibree.android.test.utils.randomUnsignedSigned16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase
import org.junit.Test

internal class PlaqlessSensorInteractorTest : BaseSensorInteractorTestHelper<PlaqlessSensorInteractor>() {

    private val sensorListener = mock<GameSensorListener>()

    override fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ): PlaqlessSensorInteractor =
        PlaqlessSensorInteractor(
            sensorListener = sensorListener,
            gameLifecycleProvider = stageProvider,
            gameToothbrushEventProvider = brushingEventProvider
        )

    /*
   registerListeners
    */
    @Test
    fun `registerListeners does not subscribe to plaqlessData if there's a previous subscription`() {
        val connection = mock<KLTBConnection>()
        initSensorInteractor(connection = connection)

        sensorInteractor.sensorDisposable = mock()
        whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(false)

        sensorInteractor.registerListeners()
    }

    @Test
    fun `registerListeners subscribes to plaqlessdata if sensorDisposable is null`() {
        val plaqlessSubject = PublishProcessor.create<PlaqlessSensorState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withPlaqlessSupport(plaqlessSubject)
                .build()
        initSensorInteractor(connection = connection)

        TestCase.assertNull(sensorInteractor.sensorDisposable)

        TestCase.assertFalse(plaqlessSubject.hasSubscribers())

        sensorInteractor.registerListeners()

        TestCase.assertTrue(plaqlessSubject.hasSubscribers())
    }

    @Test
    fun `registerListeners subscribes to plaqlessdata if there isn't a previous subscription`() {
        val plaqlessSubject = PublishProcessor.create<PlaqlessSensorState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withPlaqlessSupport(plaqlessSubject)
                .build()
        initSensorInteractor(connection = connection)

        sensorInteractor.sensorDisposable = mock()
        whenever(sensorInteractor.sensorDisposable!!.isDisposed).thenReturn(true)

        TestCase.assertFalse(plaqlessSubject.hasSubscribers())

        sensorInteractor.registerListeners()

        TestCase.assertTrue(plaqlessSubject.hasSubscribers())
    }

    @Test
    fun `registerListeners new plaqlessdata invokes onPlaqlessData`() {
        val plaqlessSubject = PublishProcessor.create<PlaqlessSensorState>()
        val connection =
            KLTBConnectionBuilder
                .createAndroidLess()
                .withPlaqlessSupport(plaqlessSubject)
                .build()
        spySensorInteractor(connection = connection)

        doNothing().whenever(sensorInteractor).onPlaqlessData(any())

        sensorInteractor.registerListeners()

        verify(sensorInteractor, never()).onPlaqlessData(any())

        val sensorState = plaqlessSensorState()

        plaqlessSubject.onNext(sensorState)

        verify(sensorInteractor).onPlaqlessData(sensorState)
    }

    /*
    unregisterListeners
     */

    @Test
    fun `unregisterListeners does nothing if there's no plaqlessDataSubscription`() {
        initSensorInteractor()

        sensorInteractor.unregisterListeners()
    }

    @Test
    fun `unregisterListeners disposes plaqlessDataSubscription`() {
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
    fun `onPlaqlessRawData invokes sensorListener onPlaqlessData with isPlaying state`() {
        initSensorInteractor()

        val sensorState = plaqlessSensorState()

        setIsPlaying(true)

        sensorInteractor.onPlaqlessData(sensorState)

        verify(sensorListener).onPlaqlessData(true, sensorState)
    }

    /*
    Utils
     */
    private fun plaqlessSensorState(): PlaqlessSensorState =
        PlaqlessSensorState(
            453L,
            randomUnsignedSigned16(),
            randomUnsignedSigned16(),
            randomUnsignedSigned16(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            randomUnsigned8(),
            PlaqlessError.NONE
        )
}
