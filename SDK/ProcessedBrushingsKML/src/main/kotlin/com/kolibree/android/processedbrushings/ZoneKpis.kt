package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import com.kolibree.kml.KPIAggregate
import com.kolibree.kml.SpeedKPI

/**
 * Holds boolean values determining how the user performed on a given MouthZone
 *
 * See https://confluence.kolibree.com/display/SOF/KML+library
 */
@Keep
data class ZoneKpis(
    val isOrientationCorrect: Boolean,
    val isMovementCorrect: Boolean,
    val speedCorrectness: SpeedKPI
) {
    constructor(kpiAggregate: KPIAggregate) : this(
        kpiAggregate.isOrientationCorrect,
        kpiAggregate.isMovementCorrect,
        kpiAggregate.speedCorrectness
    )
}
