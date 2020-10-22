/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParser
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS
import io.reactivex.Single
import javax.inject.Inject

internal class DspStateUseCase @Inject constructor(private val bleDriver: BleDriver) {
    fun dspStateSingle(): Single<DspState> {
        return bleDriver
            .setAndGetDeviceParameterOnce(byteArrayOf(DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS))
            .map {
                DspStatePayloadParser
                    .create(bleDriver.getFirmwareVersion())
                    .parseDspStatePayload(it)
            }
    }
}
