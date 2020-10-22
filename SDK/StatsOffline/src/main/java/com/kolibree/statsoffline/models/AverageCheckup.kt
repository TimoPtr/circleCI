/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import android.annotation.SuppressLint
import com.kolibree.android.commons.extensions.zeroIfNan
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.roundOneDecimalToFloat
import java.math.RoundingMode

typealias AverageCheckup = Map<MouthZone16, Float>

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun emptyAverageCheckup() = MouthZone16.values().associate { it to 0f }

/**
 * @throws IllegalArgumentException if non-empty [AverageCheckup] doesn't contain all [MouthZone16]
 * @throws IllegalArgumentException if any [Float] in the values is [Float.NaN]
 */
internal fun AverageCheckup.validate() {
    if (values.isEmpty()) return

    if (values.contains(Float.NaN)) {
        throw IllegalArgumentException("averageCheckup Map can't contain NaN")
    }

    if (size != MouthZone16.values().size) {
        throw IllegalArgumentException("averageCheckup Map must be of size 16 (actual size: $size)")
    }

    if (!keys.containsAll(MouthZone16.values().asList())) {
        throw IllegalArgumentException("averageCheckup Map must contain all MouthZone16 values")
    }
}

/**
 * @return true if any [MouthZone16] has an average value higher than zero
 */
internal fun AverageCheckup.hasCheckupData() = values.sum() > 0f

/**
 * Given a Sequence<[AverageCheckup]>, return an [AverageCheckup] from calculating the average of each
 * [MouthZone16] in the map
 *
 * Values are rounded to a single decimal using [RoundingMode.HALF_UP] rounding mode
 */
internal fun Sequence<AverageCheckup>.calculateAverageCheckup(): AverageCheckup {
    @Suppress("RemoveExplicitTypeArguments")
    val zoneAverageMap: Map<MouthZone16, MutableList<Float>> =
        MouthZone16.values().associate { it to mutableListOf<Float>() }

    filter { it.hasCheckupData() }
        .map { it.entries }
        .flatten()
        .forEach {
            zoneAverageMap[it.key]?.add(it.value)
        }

    return zoneAverageMap.valuesAverage()
}

/**
 * Given a Map<[MouthZone16], List<[Float]>>, return an [AverageCheckup] from calculating the average of each
 * [MouthZone16] in the map
 *
 * Values are rounded to a single decimal using [RoundingMode.HALF_UP] rounding mode
 */
private fun Map<MouthZone16, List<Float>>.valuesAverage(): AverageCheckup {
    return mapValues { it.value.average().zeroIfNan().roundOneDecimalToFloat() }
}
