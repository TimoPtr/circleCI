/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.gyroscopic

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/** Wrapper for Android's [SensorManager] so related classes can be unit tested */
internal interface SensorManagerWrapper {

    fun registerListener(
        listener: SensorEventListener,
        sensor: Sensor,
        samplingPeriodUs: Int
    ): Boolean

    fun unregisterListener(listener: SensorEventListener)

    fun getDefaultSensor(sensor: Int): Sensor?

    fun getRotationMatrixFromVector(rotationMatrix: FloatArray, vector: FloatArray)

    fun getOrientation(rotationMatrix: FloatArray, rotationVector: FloatArray)
}

/** [SensorManagerWrapper] implementation */
internal class SensorManagerWrapperImpl(
    private val sensorManager: SensorManager
) : SensorManagerWrapper {

    override fun registerListener(
        listener: SensorEventListener,
        sensor: Sensor,
        samplingPeriodUs: Int
    ) = sensorManager
        .registerListener(listener, sensor, samplingPeriodUs)

    override fun unregisterListener(listener: SensorEventListener) =
        sensorManager
            .unregisterListener(listener)

    override fun getDefaultSensor(sensor: Int): Sensor? {
        val sensorList = sensorManager.getSensorList(sensor)
        return if (sensorList.isNullOrEmpty()) null
        else sensorManager.getDefaultSensor(sensor)
    }

    override fun getRotationMatrixFromVector(rotationMatrix: FloatArray, vector: FloatArray) =
        SensorManager
            .getRotationMatrixFromVector(rotationMatrix, vector)

    override fun getOrientation(rotationMatrix: FloatArray, rotationVector: FloatArray) {
        SensorManager
            .getOrientation(rotationMatrix, rotationVector)
    }
}
