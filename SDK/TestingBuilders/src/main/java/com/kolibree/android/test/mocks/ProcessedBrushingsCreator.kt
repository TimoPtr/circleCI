/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.processedbrushings.CheckupZoneKpis
import com.kolibree.android.processedbrushings.ZoneCheckupData
import com.kolibree.android.processedbrushings.ZoneKpis
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.Percentage
import com.kolibree.kml.PlaqlessCheckup
import com.kolibree.kml.PlaqueAggregate
import com.kolibree.kml.PlaqueAggregateByMouthZone16Pair
import com.kolibree.kml.PlaqueAggregateByMouthZone16ZoneVector
import com.kolibree.kml.PlaquePercentage
import com.kolibree.kml.PlaqueStatus
import com.kolibree.kml.SpeedKPI
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

@Keep
fun zoneCheckupData(
    zoneSurface: Float = 0f,
    zoneKpis: ZoneKpis = ZoneKpis(false, false, SpeedKPI.Correct),
    checkupZoneKpis: CheckupZoneKpis = CheckupZoneKpis(0, 0, 0, 0, 0, 0)
): ZoneCheckupData {
    return object : ZoneCheckupData {
        override fun surfacePercentage() = zoneSurface

        override fun zoneKpis() = zoneKpis

        override fun checkupZoneKpis() = checkupZoneKpis

        override fun toString(): String {
            return "[zoneSurface: $zoneSurface, zoneKpis: $zoneKpis, checkupZoneKpis: $checkupZoneKpis]"
        }
    }
}

@Keep
fun plaqlessCheckup(
    cleanPercent: Int? = null,
    missedPercent: Int? = null,
    plaqueLeftPercent: Int? = null,
    plaqueAggregate: Map<MouthZone16, PlaqueAggregate>? = null
): PlaqlessCheckup? {
    if (cleanPercent == null &&
        missedPercent == null &&
        plaqueLeftPercent == null &&
        plaqueAggregate == null
    ) {
        return null
    }

    val plaqlessCheckup = mock<PlaqlessCheckup>()

    val plaquePercentage = mock<PlaquePercentage>()
    whenever(plaqlessCheckup.plaquePercentage).thenReturn(plaquePercentage)

    val cleanPercentKML = percentage(cleanPercent)
    whenever(plaquePercentage.cleanPercent).thenReturn(cleanPercentKML)

    val missedPercentKML = percentage(missedPercent)
    whenever(plaquePercentage.missedPercent).thenReturn(missedPercentKML)

    val plaqueLeftPercentKML = percentage(plaqueLeftPercent)
    whenever(plaquePercentage.plaqueLeftPercent).thenReturn(plaqueLeftPercentKML)

    plaqueAggregate?.apply {
        val vector = mock<PlaqueAggregateByMouthZone16ZoneVector>()

        whenever(vector.capacity()).thenReturn(size.toLong())
        whenever(vector.size).thenReturn(size)

        val pairs = mutableListOf<PlaqueAggregateByMouthZone16Pair>()
        entries.forEachIndexed { index, entry ->
            val pair = mock<PlaqueAggregateByMouthZone16Pair>()
            whenever(pair.first).thenReturn(entry.key)
            whenever(pair.second).thenReturn(entry.value)

            pairs.add(pair)

            whenever(vector[index]).thenReturn(pair)
        }

        whenever(vector.iterator()).thenReturn(pairs.iterator())

        whenever(plaqlessCheckup.plaqueAggregateByZoneVector).thenReturn(vector)
    }

    return plaqlessCheckup
}

@Keep
fun percentage(value: Int?): Percentage? {
    return value?.let {
        val percentage = mock<Percentage>()

        whenever(percentage.value()).thenReturn(it)

        percentage
    }
}

@Keep
fun plaqueAggregate(
    status: PlaqueStatus,
    percentage: Percentage = percentage(0)!!
): PlaqueAggregate {
    val plaqueAggregate = mock<PlaqueAggregate>()
    whenever(plaqueAggregate.plaqueStatus).thenReturn(status)
    whenever(plaqueAggregate.cleannessPercentage).thenReturn(percentage)
    return plaqueAggregate
}
