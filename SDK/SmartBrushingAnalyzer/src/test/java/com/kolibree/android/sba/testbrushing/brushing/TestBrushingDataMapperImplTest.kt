/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.brushing

import android.graphics.Color
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.mouthmap.logic.BrushingResultsMapperImpl
import com.kolibree.android.mouthmap.logic.ResultColorsProvider
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.CheckupZoneKpis
import com.kolibree.android.processedbrushings.PlaqlessCheckupData
import com.kolibree.android.processedbrushings.ZoneCheckupData
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.Duration

class TestBrushingDataMapperImplTest : BaseUnitTest() {

    private val resultColorsProvider = ResultColorsProvider(
        overspeedColor,
        underspeedColor,
        missedColor,
        remainsColor
    )

    private lateinit var mapper: BrushingResultsMapperImpl

    override fun setup() {
        super.setup()

        mapper = spy(
            BrushingResultsMapperImpl(
                resultColorsProvider
            )
        )
    }

    @Test
    fun `map method returns BrushingResults`() {
        val checkupData = mock<CheckupData>()
        val seconds = 123L
        val coverage = 78

        whenever(checkupData.duration).thenReturn(Duration.ofSeconds(seconds))
        whenever(checkupData.surfacePercentage).thenReturn(coverage)

        val coverageColorZones = emptyMap<MouthZone16, Float>()
        val coveragePlaqlessColorZones = ColorMouthZones.oneColor(Color.RED)
        doReturn(coveragePlaqlessColorZones).whenever(mapper).plaqlessColorMouthZones(any(), any(), any())
        val speedColors = ColorMouthZones.oneColor(Color.BLUE)
        doReturn(speedColors).whenever(mapper).speedColorMouthZones(
            checkupData,
            overspeedColor,
            underspeedColor
        )
        val speedZones = emptyMap<MouthZone16, Int>()
        val checkupDataMap = emptyMap<MouthZone16, ZoneCheckupData>()
        whenever(checkupData.checkupDataMap).thenReturn(checkupDataMap)
        doReturn(speedZones).whenever(mapper).overspeedPercentageMouthZones(checkupData)
        doReturn(speedZones).whenever(mapper).underspeedPercentageMouthZones(checkupData)
        val missed = 12
        doReturn(missed).whenever(mapper).missedArea(checkupData)
        val remains = 25
        doReturn(remains).whenever(mapper).buildUpRemains(checkupData)

        val expectedResults = BrushingResults(
            duration = seconds.toInt(),
            coverage = coverage,
            coverageColorMouthZones = coverageColorZones,
            overSpeedPercentageMouthZones = speedZones,
            underSpeedPercentageMouthZones = speedZones,
            speedColorMouthZones = speedColors,
            plaqlessColorMouthZones = coveragePlaqlessColorZones,
            missedAreas = missed,
            buildUpRemains = remains
        )

        assertEquals(expectedResults, mapper.map(checkupData))
    }

    @Test
    fun `buildUpRemains returns 0 when no plaqlessCheckupData`() {
        val checkupData = mock<CheckupData>()
        whenever(checkupData.plaqlessCheckupData).thenReturn(null)
        assertEquals(0, mapper.buildUpRemains(checkupData))
    }

    @Test
    fun `buildUpRemains returns plaqueLeftPercent from plaqlessCheckupData `() {
        val checkupData = mock<CheckupData>()
        val plaqlessCheckupData = mock<PlaqlessCheckupData>()
        val expectedPercentage = 76
        whenever(checkupData.plaqlessCheckupData).thenReturn(plaqlessCheckupData)
        whenever(plaqlessCheckupData.plaqueLeftPercent).thenReturn(expectedPercentage)
        assertEquals(expectedPercentage, mapper.buildUpRemains(checkupData))
    }

    @Test
    fun `missedArea returns 0 when no plaqlessCheckupData`() {
        val checkupData = mock<CheckupData>()
        whenever(checkupData.plaqlessCheckupData).thenReturn(null)
        assertEquals(0, mapper.missedArea(checkupData))
    }

    @Test
    fun `missedArea returns missedPercent from plaqlessCheckupData `() {
        val checkupData = mock<CheckupData>()
        val plaqlessCheckupData = mock<PlaqlessCheckupData>()
        val expectedPercentage = 76
        whenever(checkupData.plaqlessCheckupData).thenReturn(plaqlessCheckupData)
        whenever(plaqlessCheckupData.missedPercent).thenReturn(expectedPercentage)
        assertEquals(expectedPercentage, mapper.missedArea(checkupData))
    }

    @Test
    fun `overspeedPercentageMouthZones returns expectedOverspeed from CheckupZoneKPIs`() {
        val checkupData = mock<CheckupData>()
        val zoneCheckupData = mock<ZoneCheckupData>()
        val zoneKPIs = mock<CheckupZoneKpis>()
        val expectedOverspeed = 99

        val map = mapOf(
            MouthZone16.LoIncExt to zoneCheckupData
        )
        whenever(zoneKPIs.overSpeedPercentage).thenReturn(expectedOverspeed)
        whenever(zoneCheckupData.checkupZoneKpis()).thenReturn(zoneKPIs)
        whenever(checkupData.checkupDataMap).thenReturn(map)

        assertEquals(expectedOverspeed, mapper.overspeedPercentageMouthZones(checkupData)[MouthZone16.LoIncExt])
    }

    @Test
    fun `underspeedPercentageMouthZones returns expectedUnderspeed from CheckupZoneKPIs`() {
        val checkupData = mock<CheckupData>()
        val zoneCheckupData = mock<ZoneCheckupData>()
        val zoneKPIs = mock<CheckupZoneKpis>()
        val expectedUnderspeed = 10

        val map = mapOf(
            MouthZone16.LoIncExt to zoneCheckupData
        )
        whenever(zoneKPIs.underSpeedPercentage).thenReturn(expectedUnderspeed)
        whenever(zoneCheckupData.checkupZoneKpis()).thenReturn(zoneKPIs)
        whenever(checkupData.checkupDataMap).thenReturn(map)

        assertEquals(expectedUnderspeed, mapper.underspeedPercentageMouthZones(checkupData)[MouthZone16.LoIncExt])
    }

    companion object {
        const val overspeedColor = Color.BLUE
        const val underspeedColor = Color.RED
        const val missedColor = Color.YELLOW
        const val remainsColor = Color.GREEN
    }
}
