/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import androidx.annotation.Keep
import io.reactivex.Flowable
import io.reactivex.Single

/** Toothbrush battery interface, get level and charging information */
@Keep
interface Battery {

    /**
     * Check if the battery is currently being charged
     *
     * @return non null [Single]
     */
    val isCharging: Single<Boolean>

    /**
     * Get the battery charging state as a [Flowable]
     *
     * @return [Boolean] charging state [Flowable]
     */
    fun isChargingFlowable(): Flowable<Boolean>

    /**
     * Get the battery level in percents
     *
     * Not available on M1, please use discreteBatteryLevel instead
     *
     * @return non null [Single]
     */
    val batteryLevel: Single<Int>

    /**
     * Get the battery level in percents
     *
     * Only available on M1, use batteryLevel on other models
     *
     * @return [DiscreteBatteryLevel] [Single]
     */
    val discreteBatteryLevel: Single<DiscreteBatteryLevel>

    /**
     * Check if the toothbrush battery uses discrete levels
     *
     * @return true if the battery uses discrete levels, false if it uses percentages
     */
    val usesDiscreteLevels: Boolean
}
