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
import com.kolibree.android.test.TestForcedException
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class SvmSensorInteractorTest : BaseSensorInteractorTestHelper<SvmSensorInteractor>() {
    private val holder = mock<GameSensorListener>()

    /*
    registerListeners
     */
    @Test
    fun `registerListeners invokes enableDetectionNotifications`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).enableDetectionNotifications()

        sensorInteractor.registerListeners()

        verify(sensorInteractor).enableDetectionNotifications()
    }

    /*
    registerDelayableListeners
     */
    @Test
    fun `registerDelayableListeners invokes maybeRegisterProbableMouthZonesListener`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).maybeRegisterProbableMouthZonesListener()

        sensorInteractor.registerDelayableListeners()

        verify(sensorInteractor).maybeRegisterProbableMouthZonesListener()
    }

    /*
    unregisterListeners
     */
    @Test
    fun `unregisterListeners invokes unregisterProbableMouthZonesListener`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).unregisterProbableMouthZonesListener()
        doNothing().whenever(sensorInteractor).disableDetectionNotifications()

        sensorInteractor.unregisterListeners()

        verify(sensorInteractor).unregisterProbableMouthZonesListener()
    }

    @Test
    fun `unregisterListeners invokes disableDetectionNotifications`() {
        spySensorInteractor()

        doNothing().whenever(sensorInteractor).unregisterProbableMouthZonesListener()
        doNothing().whenever(sensorInteractor).disableDetectionNotifications()

        sensorInteractor.unregisterListeners()

        verify(sensorInteractor).disableDetectionNotifications()
    }

    /*
    disableDetectionNotifications
     */
    @Test
    fun `disableDetectionNotifications invokes disableDetectionNotifications`() {
        val connection = createSensorTestConnection()

        spySensorInteractor(connection = connection)

        sensorInteractor.disableDetectionNotifications()

        verify(connection.detectors()).disableDetectionNotifications()
    }

    @Test
    fun `disableDetectionNotifications captures disableDetectionNotifications exception`() {
        val connection = createSensorTestConnection()

        spySensorInteractor(connection = connection)

        whenever(connection.detectors().disableDetectionNotifications())
            .thenAnswer { throw TestForcedException() }

        sensorInteractor.disableDetectionNotifications()
    }

    /*
    enableDetectionNotifications
     */
    @Test
    fun `enableDetectionNotifications invokes enableDetectionNotifications`() {
        val connection = createSensorTestConnection()

        spySensorInteractor(connection = connection)

        sensorInteractor.enableDetectionNotifications()

        verify(connection.detectors()).enableDetectionNotifications()
    }

    /*
    MAYBE REGISTER PROBABLE MOUTH ZONES LISTENER
     */
    @Test
    fun `maybeRegisterProbableMouthZonesListener registers as mouth zone listeners if registeredProbableMouthZoneListener=false and sets value true`() {
        val connection = createSensorTestConnection()

        initSensorInteractor(connection = connection)

        assertFalse(sensorInteractor.registeredProbableMouthZoneListener)

        sensorInteractor.maybeRegisterProbableMouthZonesListener()

        verify(connection.detectors().probableMouthZones()).register(sensorInteractor)

        assertTrue(sensorInteractor.registeredProbableMouthZoneListener)
    }

    @Test
    fun `maybeRegisterProbableMouthZonesListener does nothing if registeredProbableMouthZoneListener=true`() {
        val connection = createSensorTestConnection()

        initSensorInteractor(connection = connection)

        sensorInteractor.registeredProbableMouthZoneListener = true

        sensorInteractor.maybeRegisterProbableMouthZonesListener()

        verify(connection.detectors().probableMouthZones(), never()).register(sensorInteractor)
    }

    /*
    unregisterProbableMouthZonesListener
     */
    @Test
    fun `unregisterProbableMouthZonesListener invokes unregister on probableMouthZones`() {
        val connection = createSensorTestConnection()

        initSensorInteractor(connection = connection)

        sensorInteractor.unregisterProbableMouthZonesListener()

        verify(connection.detectors().probableMouthZones()).unregister(sensorInteractor)
    }

    @Test
    fun `unregisterProbableMouthZonesListener sets registeredProbableMouthZoneListener to false`() {
        val connection = createSensorTestConnection()

        initSensorInteractor(connection = connection)

        sensorInteractor.registeredProbableMouthZoneListener = true

        sensorInteractor.unregisterProbableMouthZonesListener()

        assertFalse(sensorInteractor.registeredProbableMouthZoneListener)
    }

    @Test
    fun `unregisterProbableMouthZonesListener captures unregister exception`() {
        val connection = createSensorTestConnection()

        initSensorInteractor(connection = connection)

        whenever(connection.detectors().probableMouthZones().unregister(any()))
            .thenAnswer { throw TestForcedException() }

        sensorInteractor.unregisterProbableMouthZonesListener()
    }

    @Test
    fun `unregisterProbableMouthZonesListener sets registeredProbableMouthZoneListener to false even if unregister throws exception`() {
        val connection = createSensorTestConnection()

        initSensorInteractor(connection = connection)

        sensorInteractor.registeredProbableMouthZoneListener = true

        whenever(connection.detectors().probableMouthZones().unregister(any()))
            .thenAnswer { throw TestForcedException() }

        sensorInteractor.unregisterProbableMouthZonesListener()

        assertFalse(sensorInteractor.registeredProbableMouthZoneListener)
    }

    /*
    ON SVM DATA
     */
    @Test
    fun `onSVMData does not invoke holder onSvmData if isPlaying returns false`() {
        initSensorInteractor()

        setIsPlaying(isPlaying = false)

        sensorInteractor.onSVMData(mock(), mock())

        verifyNoMoreInteractions(holder)
    }

    @Test
    fun `onSVMData invokes holder onSvmData if coachState is isPlaying returns true`() {
        initSensorInteractor()

        setIsPlaying(isPlaying = true)

        val expectedConnection = mock<KLTBConnection>()
        val data = mutableListOf<MouthZone16>()
        sensorInteractor.onSVMData(expectedConnection, data)

        verify(holder).onSVMData(eq(expectedConnection), any<List<MouthZone16>>())
    }

    /*
    utils
     */

    override fun createInstance(
        brushingEventProvider: GameToothbrushEventProvider,
        stageProvider: GameLifecycleProvider
    ): SvmSensorInteractor {
        return SvmSensorInteractor(
            holder = holder,
            gameLifecycleProvider = stageProvider,
            gameToothbrushEventProvider = brushingEventProvider
        )
    }
}
