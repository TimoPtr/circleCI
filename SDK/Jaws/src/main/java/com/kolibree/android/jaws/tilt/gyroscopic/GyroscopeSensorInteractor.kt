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
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.annotation.VisibleForTesting
import javax.inject.Inject

/** Device's rotation-fusion sensors interactor */
internal interface GyroscopeSensorInteractor {

    /**
     * Subscribe to gyroscope sensor output events
     */
    fun registerListener()

    /**
     * Release the gyroscope sensor. Must be called when the jaws view stops rendering
     */
    fun unregisterListener()

    /**
     * Get the Z Euler angle in radians
     */
    fun azimuth(): Float

    /**
     * Get the X Euler angle in radians
     */
    fun pitch(): Float

    /**
     * Get the Y Euler angle in radians
     */
    fun roll(): Float
}

/** [GyroscopeSensorInteractor] implementation */
internal class GyroscopeSensorInteractorImpl @Inject constructor(
    private val sensorManagerWrapper: SensorManagerWrapper
) : GyroscopeSensorInteractor, SensorEventListener {

    @VisibleForTesting
    val rotationMatrix = FloatArray(ROTATION_MATRIX_LENGTH)

    @VisibleForTesting
    val rotationVector = FloatArray(ROTATION_VECTOR_LENGTH)

    override fun registerListener() {
        val rotationFusionSensor = sensorManagerWrapper.getDefaultSensor(TYPE_ROTATION_VECTOR)
        rotationFusionSensor?.let {
            sensorManagerWrapper.registerListener(this, it,
                SENSOR_REFRESH_PERIOD_US
            )
        }
    }

    override fun unregisterListener() = sensorManagerWrapper.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == TYPE_ROTATION_VECTOR) {
                onRotationFusionSensorChanged(it.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }

    override fun azimuth() =
        synchronized(rotationVector) {
            rotationVector[ROTATION_VECTOR_AZIMUTH_INDEX]
        }

    override fun pitch() =
        synchronized(rotationVector) {
            rotationVector[ROTATION_VECTOR_PITCH_INDEX]
        }

    override fun roll() =
        synchronized(rotationVector) {
            rotationVector[ROTATION_VECTOR_ROLL_INDEX]
        }

    // The payload is nullable only to make us able to unit test this method (SensorEvent values
    // field is hidden)
    @VisibleForTesting
    fun onRotationFusionSensorChanged(payload: FloatArray?) =
        payload?.let {
            sensorManagerWrapper.getRotationMatrixFromVector(rotationMatrix, it)

            synchronized(rotationVector) {
                sensorManagerWrapper.getOrientation(rotationMatrix, rotationVector)
            }
        }

    companion object {

        @VisibleForTesting
        const val ROTATION_MATRIX_LENGTH = 16

        @VisibleForTesting
        const val ROTATION_VECTOR_LENGTH = 3

        @VisibleForTesting
        const val SENSOR_REFRESH_PERIOD_US = 30000 // A bit more than persistence of vision

        @VisibleForTesting
        const val ROTATION_VECTOR_AZIMUTH_INDEX = 0

        @VisibleForTesting
        const val ROTATION_VECTOR_PITCH_INDEX = 1

        @VisibleForTesting
        const val ROTATION_VECTOR_ROLL_INDEX = 2
    }
}
