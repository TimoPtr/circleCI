/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.error.CommandNotSupportedException
import io.reactivex.Single

/** Base BLE [Battery] implementation */
/* https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0 */
internal abstract class BatteryBaseImpl(
    private val bleDriver: BleDriver,
    private val usesCompatPayload: Boolean
) : Battery {

    /**
     * Returns the PayloadReader for the battery command
     *
     * If the toothbrush is in bootloader, it emits a CommandNotSupportedException
     */
    @SuppressWarnings("TooGenericExceptionCaught")
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun batteryPayloadReader(): Single<PayloadReader> =
        Single.create {
            if (isRunningBootloader()) {
                it.tryOnError(CommandNotSupportedException("Toothbrush is in bootloader"))
            } else {
                try {
                    it.onSuccess(bleDriver.getDeviceParameter(batteryPayload()))
                } catch (exception: Exception) {
                    it.tryOnError(exception)
                }
            }
        }

    @VisibleForTesting
    fun batteryPayload(): ByteArray =
        if (usesCompatPayload)
            BATTERY_PAYLOAD_COMPAT
        else
            BATTERY_PAYLOAD

    protected fun isRunningBootloader() = bleDriver.isRunningBootloader

    companion object {

        /*
        Ara and E1 have to provide a parameter to enable battery events
         */
        @VisibleForTesting
        val BATTERY_PAYLOAD_COMPAT = byteArrayOf(0x22, 0x01)

        @VisibleForTesting
        val BATTERY_PAYLOAD = byteArrayOf(0x22)
    }
}
