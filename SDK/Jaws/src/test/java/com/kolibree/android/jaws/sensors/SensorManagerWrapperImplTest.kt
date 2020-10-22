/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.sensors

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.tilt.gyroscopic.SensorManagerWrapperImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class SensorManagerWrapperImplTest : BaseUnitTest() {

    private val sensorManager: SensorManager = mock()

    private val sensorManagerWrapper =
        SensorManagerWrapperImpl(
            sensorManager
        )

    @Test
    fun `registerListener passes all parameters to sensor manager and calls registerListener`() {
        val listener: SensorEventListener = mock()
        val sensor: Sensor = mock()
        val samplingPeriodUs = 1000

        sensorManagerWrapper.registerListener(listener, sensor, samplingPeriodUs)

        verify(sensorManager).registerListener(listener, sensor, samplingPeriodUs)
    }

    @Test
    fun `unregisterListener removes listener from sensor manager`() {
        val listener: SensorEventListener = mock()

        sensorManagerWrapper.unregisterListener(listener)

        verify(sensorManager).unregisterListener(listener)
    }

    @Test
    fun `getDefaultSensor returns sensor if it is available`() {
        val sensorId = 11
        val expectedSensor: Sensor = mock()
        whenever(sensorManager.getSensorList(sensorId)).thenReturn(listOf(expectedSensor))
        whenever(sensorManager.getDefaultSensor(sensorId)).thenReturn(expectedSensor)

        val sensor = sensorManagerWrapper.getDefaultSensor(sensorId)

        assertEquals(expectedSensor, sensor)
    }

    @Test
    fun `getDefaultSensor returns null if it is not available`() {
        val sensorId = 11
        val someOtherSensor: Sensor = mock()
        whenever(sensorManager.getSensorList(sensorId)).thenReturn(listOf(someOtherSensor))

        whenever(sensorManager.getDefaultSensor(sensorId)).thenReturn(null)

        val sensor = sensorManagerWrapper.getDefaultSensor(sensorId)

        assertNull(sensor)
    }

    @Test
    fun `getDefaultSensor returns null if the sensor is available, but not present on sensor list`() {
        val sensorId = 11
        val expectedSensor: Sensor = mock()
        whenever(sensorManager.getSensorList(sensorId)).thenReturn(emptyList())

        whenever(sensorManager.getDefaultSensor(sensorId)).thenReturn(expectedSensor)

        val sensor = sensorManagerWrapper.getDefaultSensor(sensorId)

        assertNull(sensor)
        verify(sensorManager, never()).getDefaultSensor(sensorId)
    }
}
