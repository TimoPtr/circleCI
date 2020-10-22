/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.Keep

/** Kolibree toothbrush vibration speed modes */
// https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0

private const val LOW_INTENSITY = 0
private const val MEDIUM_INTENSITY = 1
private const val HIGH_INTENSITY = 2
private const val MIXED_INTENSITY = 3
private const val UNKNOWN_INTENSITY = 4

private const val REGULAR_MODE_BLE_INDEX = 0
private const val SLOW_MODE_BLE_INDEX = 1
private const val STRONG_MODE_BLE_INDEX = 2
private const val POLISHING_MODE_BLE_INDEX = 3
private const val USER_DEFINED_BLE_INDEX = 4

@Keep
enum class BrushingMode(
    internal val bleIndex: Int,
    internal val intensity: Int
) : Comparable<BrushingMode> {

    /** Medium intensity, for sensitive gums */
    Regular(bleIndex = REGULAR_MODE_BLE_INDEX, intensity = MEDIUM_INTENSITY),

    /** Low intensity, for very sensitive gums */
    Slow(bleIndex = SLOW_MODE_BLE_INDEX, intensity = LOW_INTENSITY),

    /** High intensity, most efficient cleaning */
    Strong(bleIndex = STRONG_MODE_BLE_INDEX, intensity = HIGH_INTENSITY),

    /** Mixed intensity from Regular, Slow and Strong */
    Polishing(bleIndex = POLISHING_MODE_BLE_INDEX, intensity = MIXED_INTENSITY),

    /** User-defined intensity */
    UserDefined(bleIndex = USER_DEFINED_BLE_INDEX, intensity = UNKNOWN_INTENSITY);

    companion object {

        @JvmStatic
        fun defaultMode(): BrushingMode =
            Regular

        @JvmStatic
        fun lookupFromBleIndex(bleIndex: Int) =
            when (bleIndex) {
                Regular.bleIndex -> Regular
                Slow.bleIndex -> Slow
                Strong.bleIndex -> Strong
                Polishing.bleIndex -> Polishing
                UserDefined.bleIndex -> UserDefined
                else -> throw IllegalArgumentException("Unknown BLE index $bleIndex")
            }

        @JvmStatic
        fun sortByIntensity(modes: List<BrushingMode>): List<BrushingMode> =
            modes.sortedBy { it.intensity }
    }
}
