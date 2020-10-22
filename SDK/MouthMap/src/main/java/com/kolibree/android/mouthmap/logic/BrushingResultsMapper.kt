/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.logic

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqueStatus
import com.kolibree.kml.SpeedKPI
import javax.inject.Inject

@Keep
interface BrushingResultsMapper {
    fun map(checkupData: CheckupData): BrushingResults
}

@Keep
class BrushingResultsMapperImpl @Inject constructor(
    private val resultColorsProvider: ResultColorsProvider
) : BrushingResultsMapper {

    override fun map(checkupData: CheckupData): BrushingResults {
        val duration = checkupData.duration.seconds.toInt()
        val coverage = checkupData.surfacePercentage

        val speedColors =
            speedColorMouthZones(
                checkupData,
                resultColorsProvider.overspeed,
                resultColorsProvider.underspeed
            )
        val plaqlessColors =
            plaqlessColorMouthZones(
                checkupData,
                resultColorsProvider.missed,
                resultColorsProvider.buildUpRemains
            )
        val missed = missedArea(checkupData)
        val remains = buildUpRemains(checkupData)

        return BrushingResults(
            coverageColorMouthZones = checkupData.zoneSurfaceMap,
            speedColorMouthZones = speedColors,
            overSpeedPercentageMouthZones = overspeedPercentageMouthZones(checkupData),
            underSpeedPercentageMouthZones = underspeedPercentageMouthZones(checkupData),
            duration = duration,
            coverage = coverage,
            hasPlaqlessData = checkupData.plaqlessCheckupData != null,
            plaqlessColorMouthZones = plaqlessColors,
            missedAreas = missed,
            buildUpRemains = remains
        )
    }

    @VisibleForTesting
    fun buildUpRemains(checkupData: CheckupData): Int =
        checkupData.plaqlessCheckupData?.plaqueLeftPercent ?: 0

    @VisibleForTesting
    fun missedArea(checkupData: CheckupData): Int =
        checkupData.plaqlessCheckupData?.missedPercent ?: 0

    @VisibleForTesting
    fun plaqlessColorMouthZones(
        checkupData: CheckupData,
        @ColorInt colorMissed: Int,
        @ColorInt colorRemains: Int
    ): ColorMouthZones =
        ColorMouthZones(MouthZone16.values().map { zone ->
            val color = when (checkupData.plaqlessCheckupData?.plaqueStatus(zone)) {
                PlaqueStatus.Missed -> colorMissed
                PlaqueStatus.PlaqueLeft -> colorRemains
                else -> Color.WHITE
            }
            zone to color
        }.toMap(), Color.WHITE)

    @VisibleForTesting
    fun speedColorMouthZones(
        checkupData: CheckupData,
        overspeedColor: Int,
        underspeedColor: Int
    ): ColorMouthZones =
        ColorMouthZones(MouthZone16.values().map { zone ->
            val zoneColor = when (checkupData.speedCorrectness(zone)) {
                SpeedKPI.Overspeed -> overspeedColor
                SpeedKPI.Underspeed -> underspeedColor
                else -> Color.WHITE
            }
            zone to zoneColor
        }.toMap(), Color.WHITE)

    @VisibleForTesting
    fun overspeedPercentageMouthZones(checkupData: CheckupData): Map<MouthZone16, Int> =
        checkupData.checkupDataMap
            .mapValues { entry ->
                entry.value.checkupZoneKpis()?.overSpeedPercentage ?: 0
            }

    @VisibleForTesting
    fun underspeedPercentageMouthZones(checkupData: CheckupData): Map<MouthZone16, Int> =
        checkupData.checkupDataMap
            .mapValues { entry ->
                entry.value.checkupZoneKpis()?.underSpeedPercentage ?: 0
            }
}
