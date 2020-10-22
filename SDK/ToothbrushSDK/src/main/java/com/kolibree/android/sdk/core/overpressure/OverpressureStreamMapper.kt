/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.overpressure

import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_STATE
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

/** Helper class for Overpressure Sensor interaction. Make sure each driver has its own instance */
internal class OverpressureStreamMapper {

    private val overpressureStatePublishProcess = PublishProcessor.create<OverpressureState>()

    /**
     * To be called when the parameters char receives a
     * [DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_STATE] notification. The first byte of the payload
     * reader (command ID) MUST have been consumed before calling this method.
     *
     * @param payloadReader [PayloadReader]
     */
    // https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=H21
    fun onOverpressureSensorData(payloadReader: PayloadReader) =
        overpressureStatePublishProcess.onNext(
            OverpressureState(
                payloadReader.readBoolean(),
                payloadReader.readBoolean()
            )
        )

    /**
     * Get the Overpressure Sensor's state [Flowable]
     *
     * Only compatible with Glint. Other devices will emit a
     * [com.kolibree.android.sdk.error.CommandNotSupportedException]
     * since they don't embed such sensor.
     *
     * @return [OverpressureState] [Flowable]
     */
    fun overpressureStateFlowable(): Flowable<OverpressureState> =
        overpressureStatePublishProcess.hide()
}
