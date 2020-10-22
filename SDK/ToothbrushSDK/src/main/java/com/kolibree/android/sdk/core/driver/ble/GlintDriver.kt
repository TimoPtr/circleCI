/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_STATE
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PICKUP_DETECTION_CONTROL
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import com.kolibree.android.sdk.core.overpressure.OverpressureStreamMapper
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

/** Glint model [KolibreeBleDriver] implementation. Same hardware as CE2 */
internal open class GlintDriver : CE2Driver {

    constructor(context: Context, mac: String, listener: KLTBDriverListener) : super(
        context,
        mac,
        listener
    )

    @VisibleForTesting
    constructor(
        bleManager: KLNordicBleManager,
        listener: KLTBDriverListener,
        bluetoothScheduler: Scheduler,
        mac: String,
        streamer: CharacteristicNotificationStreamer,
        notifyListenerScheduler: Scheduler
    ) : super(
        bleManager,
        listener,
        bluetoothScheduler,
        mac,
        streamer,
        notifyListenerScheduler
    )

    private val overpressureStreamMapper = OverpressureStreamMapper()

    public override fun fileType() = FileType.GLINT

    override fun toothbrushModel() = ToothbrushModel.GLINT

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onDeviceParameterNotification(
        commandId: Byte,
        payloadReader: PayloadReader
    ) {
        super.onDeviceParameterNotification(commandId, payloadReader)

        if (commandId == DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_STATE) {
            overpressureStreamMapper.onOverpressureSensorData(payloadReader)
        }
    }

    override fun overpressureStateFlowable() =
        overpressureStreamMapper
            .overpressureStateFlowable()

    override fun enableOverpressureDetector(enable: Boolean): Completable =
        setAndGetDeviceParameterOnce(byteArrayOf(
            DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL,
            if (enable) 1 else 0
        )).ignoreElement()

    override fun isOverpressureDetectorEnabled(): Single<Boolean> =
        setAndGetDeviceParameterOnce(byteArrayOf(DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL))
            .map { it.skip(1).readBoolean() }

    override fun enablePickupDetector(enable: Boolean): Completable =
        setAndGetDeviceParameterOnce(byteArrayOf(
            DEVICE_PARAMETERS_PICKUP_DETECTION_CONTROL,
            if (enable) 1 else 0
        )).ignoreElement()
}
