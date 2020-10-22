/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_3_MONTHS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_6_MONTHS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_CHANGE
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_CUT_OFF
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_FEW_DAYS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_FEW_WEEKS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_UNKNOWN
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.error.CommandNotSupportedException
import io.reactivex.Flowable
import io.reactivex.Single
import timber.log.Timber

/** Embedded [Battery] implementation (CM1) */
/* https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0 */
internal class ReplaceableBatteryImpl(
    bleDriver: BleDriver,
    usesCompatPayload: Boolean
) : BatteryBaseImpl(bleDriver, usesCompatPayload) {

    override val isCharging = Single.just(false)

    override val batteryLevel: Single<Int> =
        Single.error(CommandNotSupportedException("Percentage battery level not available"))

    override val discreteBatteryLevel =
        batteryPayloadReader()
            .map { it.skip(DISCRETE_BATTERY_LEVEL_OFFSET).readInt8() }
            .map { mapDiscreteBatteryLevel(it) }
            .onErrorResumeNext {
                if (it is CommandNotSupportedException) {
                    Single.just(BATTERY_UNKNOWN)
                } else {
                    Single.error(it)
                }
            }

    override val usesDiscreteLevels = true

    override fun isChargingFlowable(): Flowable<Boolean> =
        Flowable.error(CommandNotSupportedException("Replaceable batteries can't be charged"))

    @SuppressWarnings("MagicNumber")
    @VisibleForTesting
    fun mapDiscreteBatteryLevel(m1BatteryLevel: Byte): DiscreteBatteryLevel =
        when (m1BatteryLevel.toInt()) {
            0 -> BATTERY_6_MONTHS
            1 -> BATTERY_3_MONTHS
            2 -> BATTERY_FEW_WEEKS
            3 -> BATTERY_FEW_DAYS
            4 -> BATTERY_CHANGE
            5 -> BATTERY_CUT_OFF
            6 -> BATTERY_UNKNOWN
            else -> {
                Timber.e("Unknown battery level %d", m1BatteryLevel)
                BATTERY_UNKNOWN
            }
        }

    companion object {

        @VisibleForTesting
        const val DISCRETE_BATTERY_LEVEL_OFFSET = 9
    }
}
