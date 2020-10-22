/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.BaseDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * BLE toothbrushes common driver's interface
 */
@SuppressWarnings("TooManyFunctions")
internal interface BleDriver : BaseDriver {

    /**
     * Enable notifications on ota update status characteristic.
     *
     * Blocking call
     *
     * @throws Exception if the command could not be sent
     */
    @Throws(Exception::class)
    fun enableOtaUpdateStatusCharacteristicNotifications()

    /**
     * Enables a notification on ota update status characteristic and returns.
     *
     * @return a Flowable that will emit data received on that characteristic
     */
    fun otaUpdateStatusCharacteristicChangedFlowable(): Flowable<ByteArray>

    fun writeOtaUpdateStartCharacteristic(payload: ByteArray): Completable

    fun writeOtaChunkCharacteristic(payload: ByteArray): Completable

    fun writeOtaChunkCharacteristicWithResponse(payload: ByteArray): Completable

    fun writeOtaUpdateValidateCharacteristic(payload: ByteArray): Completable

    fun reloadVersions(): Completable

    fun refreshDeviceCacheCompletable(): Completable

    /**
     * @return a Flowable that will emit all data received on [DEVICE_PARAMETERS]
     */
    fun deviceParametersCharacteristicChangedStream(): Flowable<ByteArray>

    /**
     * Send a command to the toothbrush
     *
     * See the CommandSet class
     *
     * @param commandPayload non null command payload byte array
     * @throws Exception if the command could not be sent
     */
    @Throws(Exception::class)
    fun sendCommand(commandPayload: ByteArray)

    /**
     * Set a device parameter
     *
     * see ParameterSet
     *
     * @param payload non null payload
     * @return true if the parameter has been set, false otherwise
     * @throws Exception if an error occurred
     */
    @Throws(Exception::class)
    fun setDeviceParameter(payload: ByteArray): Boolean

    /**
     * Get a device parameter
     *
     * see ParameterSet
     *
     * @param payload non null payload
     * @return non null PayloadReader
     * @throws Exception if an error occurred
     */
    @Throws(Exception::class)
    fun getDeviceParameter(payload: ByteArray): PayloadReader

    /**
     * Set and get a device parameter
     *
     * @param payload [ByteArray]
     * @return [PayloadReader]
     * @throws Exception if an error occurred
     */
    @Throws(Exception::class)
    fun setAndGetDeviceParameter(payload: ByteArray): PayloadReader

    /**
     * Set and get a device parameter
     *
     * @param payload [ByteArray]
     * @return Single<[PayloadReader]>
     */
    fun setAndGetDeviceParameterOnce(payload: ByteArray): Single<PayloadReader>

    @Throws(Exception::class)
    fun connectionInterval(): Single<Int>

    @Throws(Exception::class)
    fun queryRealName(): String
}
