/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.curve

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

/** [BrushingModeCurve] settings */
@VisibleForApp
@Parcelize
data class BrushingModeCurveSettings internal constructor(
    val curveId: Int = BrushingModeCurve.FLAT_CURVE_INDEX,
    val modifiable: Boolean = false,
    val referenceVoltageMv: Int = DEFAULT_REFERENCE_VOLTAGE,
    val divider: Int = DEFAULT_DIVIDER,
    val slope10PercentsDutyCycle: Int = 0,
    val slope20PercentsDutyCycle: Int = 0,
    val slope30PercentsDutyCycle: Int = 0,
    val slope40PercentsDutyCycle: Int = 0,
    val slope50PercentsDutyCycle: Int = 0,
    val slope60PercentsDutyCycle: Int = 0,
    val slope70PercentsDutyCycle: Int = 0,
    val slope80PercentsDutyCycle: Int = 0,
    val slope90PercentsDutyCycle: Int = 0
) : Parcelable {

    fun curve(): BrushingModeCurve = BrushingModeCurve.fromBleIndex(curveId)

    @VisibleForApp
    companion object {

        const val DEFAULT_REFERENCE_VOLTAGE = 3600

        const val DEFAULT_DIVIDER = 600

        const val MAX_REFERENCE_VOLTAGE = 65535

        const val MAX_DIVIDER = 65535

        const val MAX_SLOPE = 255

        fun default() = BrushingModeCurveSettings()

        @Suppress("LongMethod")
        fun custom(
            referenceVoltageMv: Int = DEFAULT_REFERENCE_VOLTAGE,
            divider: Int = DEFAULT_DIVIDER,
            slope10PercentsDutyCycle: Int = 0,
            slope20PercentsDutyCycle: Int = 0,
            slope30PercentsDutyCycle: Int = 0,
            slope40PercentsDutyCycle: Int = 0,
            slope50PercentsDutyCycle: Int = 0,
            slope60PercentsDutyCycle: Int = 0,
            slope70PercentsDutyCycle: Int = 0,
            slope80PercentsDutyCycle: Int = 0,
            slope90PercentsDutyCycle: Int = 0
        ) = BrushingModeCurveSettings(
            curveId = BrushingModeCurve.CUSTOM_CURVE_INDEX,
            modifiable = true,
            referenceVoltageMv = referenceVoltageMv,
            divider = divider,
            slope10PercentsDutyCycle = slope10PercentsDutyCycle,
            slope20PercentsDutyCycle = slope20PercentsDutyCycle,
            slope30PercentsDutyCycle = slope30PercentsDutyCycle,
            slope40PercentsDutyCycle = slope40PercentsDutyCycle,
            slope50PercentsDutyCycle = slope50PercentsDutyCycle,
            slope60PercentsDutyCycle = slope60PercentsDutyCycle,
            slope70PercentsDutyCycle = slope70PercentsDutyCycle,
            slope80PercentsDutyCycle = slope80PercentsDutyCycle,
            slope90PercentsDutyCycle = slope90PercentsDutyCycle
        )
    }
}
