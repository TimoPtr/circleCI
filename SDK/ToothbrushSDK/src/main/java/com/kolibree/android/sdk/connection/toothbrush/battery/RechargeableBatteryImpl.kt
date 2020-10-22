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
import io.reactivex.Flowable
import io.reactivex.Single

/** Rechargeable [Battery] implementation (CE1, CE2, Ara) */
/* https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0 */
internal class RechargeableBatteryImpl(
    private val bleDriver: BleDriver,
    usesCompatPayload: Boolean
) : BatteryBaseImpl(bleDriver, usesCompatPayload) {

    override val isCharging =
        batteryPayloadReader()
            .map { parseBatteryPayload(it) }
            .onErrorResumeNext {
                if (it is CommandNotSupportedException) {
                    Single.just(false)
                } else {
                    Single.error(it)
                }
            }

    override val batteryLevel =
        batteryPayloadReader()
            .map { it.skip(BATTERY_LEVEL_OFFSET).readInt8() }
            .map { it.toInt() }
            .onErrorResumeNext {
                if (it is CommandNotSupportedException) {
                    Single.just(0)
                } else {
                    Single.error(it)
                }
            }

    override val discreteBatteryLevel: Single<DiscreteBatteryLevel> =
        Single.error(CommandNotSupportedException("Discrete battery level not available"))

    override val usesDiscreteLevels = false

    override fun isChargingFlowable(): Flowable<Boolean> =
        Flowable.merge(
            isCharging.toFlowable(),
            deviceParametersChargingStateFlowable()
        )

    private fun deviceParametersChargingStateFlowable() =
        bleDriver
            .deviceParametersCharacteristicChangedStream()
            .map { PayloadReader(it) }
            .map { parseBatteryPayload(it) }

    private fun parseBatteryPayload(payloadReader: PayloadReader) =
        payloadReader
            .skip(CHARGING_FLAG_OFFSET)
            .readBoolean()

    companion object {

        @VisibleForTesting
        const val CHARGING_FLAG_OFFSET = 4

        @VisibleForTesting
        const val BATTERY_LEVEL_OFFSET = 1
    }
}
