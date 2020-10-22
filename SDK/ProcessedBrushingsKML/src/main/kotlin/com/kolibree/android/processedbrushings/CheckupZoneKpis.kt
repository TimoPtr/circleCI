package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import com.kolibree.kml.KPIPercentage

/**
 * Holds the percentage of the KPIs
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 */
@Keep
data class CheckupZoneKpis(
    val correctOrientationPercentage: Int,
    val correctMovementPercentage: Int,
    val underSpeedPercentage: Int,
    val correctSpeedPercentage: Int,
    val overSpeedPercentage: Int,
    val overpressurePercentage: Int
) {
    constructor(kpiPercentage: KPIPercentage) : this(
        kpiPercentage.correctOrientationRatio.value(),
        kpiPercentage.correctMovementRatio.value(),
        kpiPercentage.underSpeedRatio.value(),
        kpiPercentage.correctSpeedRatio.value(),
        kpiPercentage.overSpeedRatio.value(),
        kpiPercentage.overpressureRatio.value()
    )
}
