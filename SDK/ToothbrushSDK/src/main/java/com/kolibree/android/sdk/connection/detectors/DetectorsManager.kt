package com.kolibree.android.sdk.connection.detectors

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by aurelien on 10/08/17.
 *
 *
 * Toothbrush movement detectors interface
 */
@Keep
interface DetectorsManager {

    /**
     * Get the toothbrush's magnetometer calibration data
     *
     * @return toothbrush calibration data Magnetometer's rotation matrix (9 floats) Magnetometer
     * offset vector (3 floats)
     */
    val calibrationData: FloatArray

    /**
     * Set handedness to be used in detectors
     *
     * @param rightHanded true if right handed, false if left handed
     */
    fun setRightHanded(rightHanded: Boolean)

    /**
     * Get the Possible Mouth Zones detector
     *
     * @return non null SVM [SVMDetector]
     */
    fun probableMouthZones(): SVMDetector

    /**
     * Get the Most Probable Mouth Zones detector
     *
     *
     * Not compatible with Kolibree V1 toothbrushes
     *
     * @return null if the toothbrush model is Kolibree V1, RNN [RNNDetector] instead
     */
    fun mostProbableMouthZones(): RNNDetector?

    /**
     * Get the sensors rawData data detector
     *
     * @return non null rawData data [RawDetector]
     */
    fun rawData(): RawDetector

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
     * Returns a [Flowable] that will emit notification data from [ ][GattCharacteristic.PLAQLESS_CONTROL_CHAR]
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
     * Returns a [Flowable] that will emit [PlaqlessRingLedState] from [GattCharacteristic.DEVICE_PARAMETERS] and
     * [GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE] param
     *
     * When subscribe to this Flowable it will first ask the PLaqless TB the current status and next will listen
     * for new events
     *
     * Invoking this method on a non-Plaqless toothbrush will emit a Flowable error
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     *
     * @return Flowable<PlaqlessRingLedState> that will emit data from [GattCharacteristic.DEVICE_PARAMETERS]
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
     * Only compatible with Glint. Other devices will emit false
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
