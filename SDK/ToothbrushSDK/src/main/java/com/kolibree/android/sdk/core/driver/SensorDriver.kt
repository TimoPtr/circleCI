package com.kolibree.android.sdk.core.driver

import androidx.annotation.MainThread
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by aurelien on 10/08/17.
 *
 *
 * BaseDetector driver interface
 */
@Suppress("TooManyFunctions")
internal interface SensorDriver {

    /**
     * Get the toothbrush's sensors calibration data
     *
     * @return non null float array: magnetometer rotation matrix (9 floats) and magnetometer offset
     * vector (3 floats)
     */
    val sensorCalibration: FloatArray

    /**
     * Control the toothbrush sensors states
     *
     *
     * Notified on the main thread
     *
     * @param svmOn enable SVM detector
     * @param rnnOn enable rnnDetector
     * @param rawDataOn enable raw data and gravity streaming
     * @param rightHanded true if right handed, false if left handed
     * @throws FailureReason if the command could not be sent
     */
    @MainThread
    @Throws(FailureReason::class)
    fun onSensorControl(
        svmOn: Boolean,
        rnnOn: Boolean,
        rawDataOn: Boolean,
        rightHanded: Boolean
    )

    fun enableRawDataNotifications()

    fun disableRawDataNotifications()

    fun enableDetectionNotifications()

    fun disableDetectionNotifications()

    /**
     * Returns a [Flowable] that will emit notification data from [GattCharacteristic.PLAQLESS_DETECTOR_CHAR]
     *
     *
     * [GattCharacteristic.PLAQLESS_DETECTOR_CHAR] notifications will be enabled on the first
     * subscription to this Flowable, and disable when the last subscriber cancels the subscription
     *
     *
     * Invoking this method on a non-Plaqless toothbrush will emit a Flowable error
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     *
     *
     * If this method is invoked while there's a subscription to a previous invocation, the same
     * instance will be returned
     *
     * @return Flowable<PlaqlessRawSensorState> that will emit data from [GattCharacteristic.PLAQLESS_DETECTOR_CHAR]
     */
    fun plaqlessRawDataNotifications(): Flowable<PlaqlessRawSensorState>

    /**
     * Returns a [Flowable] that will emit [PlaqlessSensorState] from [GattCharacteristic.PLAQLESS_CONTROL_CHAR]
     *
     *
     * [GattCharacteristic.PLAQLESS_CONTROL_CHAR] notifications will be enabled on the first
     * subscription to this Flowable, and disable when the last subscriber cancels the subscription
     *
     *
     * Invoking this method on a non-Plaqless toothbrush will emit a Flowable error
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     *
     *
     * If this method is invoked while there's a subscription to a previous invocation, the same
     * instance will be returned
     *
     * @return Flowable<PlaqlessSensorState> that will emit data from [GattCharacteristic.PLAQLESS_CONTROL_CHAR]
     */
    fun plaqlessNotifications(): Flowable<PlaqlessSensorState>

    /**
     * Returns a [Flowable] that will emit [PlaqlessRingLedState] from[GattCharacteristic.DEVICE_PARAMETERS] and
     * [GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE] param
     *
     * When subscribe to this Flowable it will first ask the Paqless TB the current status and next will listen
     * for new events
     *
     * Invoking this method on a non-Plaqless toothbrush will emit a Flowable error
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     *
     * @return Flowable<PlaqlessSensorState> that will emit data from [GattCharacteristic.DEVICE_PARAMETERS]
     */
    fun plaqlessRingLedState(): Flowable<PlaqlessRingLedState>

    /**
     * Get the Overpressure Sensor's state [Flowable]
     *
     * Only compatible with Glint. Other devices will emit a
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     * since they don't embed such sensor.
     *
     * @return [OverpressureState] [Flowable]
     */
    fun overpressureStateFlowable(): Flowable<OverpressureState>

    /**
     * Enable / disable overpressure detector
     *
     * Only compatible with Glint. Other devices will emit a
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     * since they don't embed such sensor.
     *
     * @param enable [Boolean]
     * @return [Completable]
     */
    fun enableOverpressureDetector(enable: Boolean): Completable

    /**
     * Check whether the Overpressure Sensor is enabled
     *
     * Only compatible with Glint. Other devices it will emit false
     * since they don't embed such sensor.
     *
     * @return [Boolean] [Single]
     */
    fun isOverpressureDetectorEnabled(): Single<Boolean>

    /**
     * Enable / disable pickup detector
     *
     * Only compatible with Glint. Other devices will emit a
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     * since they don't embed such sensor.
     *
     * @param enable [Boolean]
     * @return [Completable]
     */
    fun enablePickupDetector(enable: Boolean): Completable
}
