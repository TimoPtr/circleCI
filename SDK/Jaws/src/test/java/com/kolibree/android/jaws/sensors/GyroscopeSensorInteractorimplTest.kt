/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.sensors

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl.Companion.ROTATION_MATRIX_LENGTH
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl.Companion.ROTATION_VECTOR_AZIMUTH_INDEX
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl.Companion.ROTATION_VECTOR_LENGTH
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl.Companion.ROTATION_VECTOR_PITCH_INDEX
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl.Companion.ROTATION_VECTOR_ROLL_INDEX
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopeSensorInteractorImpl.Companion.SENSOR_REFRESH_PERIOD_US
import com.kolibree.android.jaws.tilt.gyroscopic.SensorManagerWrapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** [GyroscopeSensorInteractorImpl] tests */
class GyroscopeSensorInteractorimplTest : BaseUnitTest() {

    private val sensorManagerWrapper = mock<SensorManagerWrapper>()

    private lateinit var gyroscopeSensorInteractor: GyroscopeSensorInteractorImpl

    @Before
    fun before() {
        gyroscopeSensorInteractor = spy(
            GyroscopeSensorInteractorImpl(
                sensorManagerWrapper
            )
        )
    }

    /*
    registerListener
     */

    @Test
    fun `registerListener uses TYPE_ROTATION_VECTOR sensor fusion`() {
        gyroscopeSensorInteractor.registerListener()

        verify(sensorManagerWrapper).getDefaultSensor(TYPE_ROTATION_VECTOR)
    }

    @Test
    fun `registerListener doesn't register listener if returned sensor was is null`() {
        whenever(sensorManagerWrapper.getDefaultSensor(TYPE_ROTATION_VECTOR))
            .thenReturn(null)

        gyroscopeSensorInteractor.registerListener()

        verify(sensorManagerWrapper).getDefaultSensor(TYPE_ROTATION_VECTOR)
        verify(sensorManagerWrapper, never())
            .registerListener(
                eq(gyroscopeSensorInteractor),
                any(),
                eq(SENSOR_REFRESH_PERIOD_US)
            )
    }

    @Test
    fun `registerListener registers instance to specified sensor stream with SENSOR_REFRESH_PERIOD_US`() {
        val expectedSensor = mock<Sensor>()
        whenever(sensorManagerWrapper.getDefaultSensor(TYPE_ROTATION_VECTOR))
            .thenReturn(expectedSensor)

        gyroscopeSensorInteractor.registerListener()

        verify(sensorManagerWrapper)
            .registerListener(
                gyroscopeSensorInteractor,
                expectedSensor,
                SENSOR_REFRESH_PERIOD_US
            )
    }

    /*
    unregisterListener
     */

    @Test
    fun `unregisterListener unregisters instance`() {
        gyroscopeSensorInteractor.unregisterListener()

        verify(sensorManagerWrapper).unregisterListener(gyroscopeSensorInteractor)
    }

    /*
    onSensorChanged
     */

    @Test
    fun `onSensorChanged with null event does nothing`() {
        gyroscopeSensorInteractor.onSensorChanged(null)
        verify(gyroscopeSensorInteractor, never()).onRotationFusionSensorChanged(any())
    }

    @Test
    fun `onSensorChanged with other sensor event does nothing`() {
        val sensor = mock<Sensor>()
        whenever(sensor.type).thenReturn(Sensor.TYPE_ACCELEROMETER)
        val event = mock<SensorEvent>()
        event.sensor = sensor

        gyroscopeSensorInteractor.onSensorChanged(event)

        verify(gyroscopeSensorInteractor, never()).onRotationFusionSensorChanged(any())
    }

    @Test
    fun `onSensorChanged invokes onRotationFusionSensorChanged with sensor values`() {
        val sensor = mock<Sensor>()
        whenever(sensor.type).thenReturn(TYPE_ROTATION_VECTOR)
        val event = mock<SensorEvent>()
        event.sensor = sensor

        gyroscopeSensorInteractor.onSensorChanged(event)

        verify(gyroscopeSensorInteractor).onRotationFusionSensorChanged(anyOrNull())
    }

    /*
    azimuth
     */

    @Test
    fun `azimuth returns rotationVector's value at ROTATION_VECTOR_AZIMUTH_INDEX`() {
        val azimuth = 1986f
        val pitch = 1983f
        val roll = 1982f

        gyroscopeSensorInteractor.rotationVector[0] = azimuth
        gyroscopeSensorInteractor.rotationVector[1] = pitch
        gyroscopeSensorInteractor.rotationVector[2] = roll

        assertEquals(
            azimuth,
            gyroscopeSensorInteractor.rotationVector[ROTATION_VECTOR_AZIMUTH_INDEX]
        )
    }

    /*
    pitch
     */

    @Test
    fun `pitch returns rotationVector's value at ROTATION_VECTOR_PITCH_INDEX`() {
        val azimuth = 1986f
        val pitch = 1983f
        val roll = 1982f

        gyroscopeSensorInteractor.rotationVector[0] = azimuth
        gyroscopeSensorInteractor.rotationVector[1] = pitch
        gyroscopeSensorInteractor.rotationVector[2] = roll

        assertEquals(
            pitch,
            gyroscopeSensorInteractor.rotationVector[ROTATION_VECTOR_PITCH_INDEX]
        )
    }

    /*
    roll
     */

    @Test
    fun `roll returns rotationVector's value at ROTATION_VECTOR_ROLL_INDEX`() {
        val azimuth = 1986f
        val pitch = 1983f
        val roll = 1982f

        gyroscopeSensorInteractor.rotationVector[0] = azimuth
        gyroscopeSensorInteractor.rotationVector[1] = pitch
        gyroscopeSensorInteractor.rotationVector[2] = roll

        assertEquals(
            roll,
            gyroscopeSensorInteractor.rotationVector[ROTATION_VECTOR_ROLL_INDEX]
        )
    }

    /*
    onRotationFusionSensorChanged
     */

    @Test
    fun `onRotationFusionSensorChanged updates rotation matrix from sensor payload`() {
        val expectedPayload = floatArrayOf()

        gyroscopeSensorInteractor.onRotationFusionSensorChanged(expectedPayload)

        verify(sensorManagerWrapper).getRotationMatrixFromVector(
            gyroscopeSensorInteractor.rotationMatrix,
            expectedPayload
        )
    }

    @Test
    fun `onRotationFusionSensorChanged updates rotation vector from rotation matrix`() {
        gyroscopeSensorInteractor.onRotationFusionSensorChanged(floatArrayOf())

        verify(sensorManagerWrapper).getOrientation(
            gyroscopeSensorInteractor.rotationMatrix,
            gyroscopeSensorInteractor.rotationVector
        )
    }

    /*
    Constants
     */

    @Test
    fun `value of ROTATION_MATRIX_LENGTH is 16`() {
        assertEquals(16, ROTATION_MATRIX_LENGTH)
    }

    @Test
    fun `value of ROTATION_VECTOR_LENGTH is 3`() {
        assertEquals(3, ROTATION_VECTOR_LENGTH)
    }

    @Test
    fun `value of SENSOR_REFRESH_PERIOD_US is 30000`() {
        assertEquals(30000, SENSOR_REFRESH_PERIOD_US)
    }

    @Test
    fun `value of ROTATION_VECTOR_AZIMUTH_INDEX is 0`() {
        assertEquals(0, ROTATION_VECTOR_AZIMUTH_INDEX)
    }

    @Test
    fun `value of ROTATION_VECTOR_PITCH_INDEX is 1`() {
        assertEquals(1, ROTATION_VECTOR_PITCH_INDEX)
    }

    @Test
    fun `value of ROTATION_VECTOR_ROLL_INDEX is 2`() {
        assertEquals(2, ROTATION_VECTOR_ROLL_INDEX)
    }
}
