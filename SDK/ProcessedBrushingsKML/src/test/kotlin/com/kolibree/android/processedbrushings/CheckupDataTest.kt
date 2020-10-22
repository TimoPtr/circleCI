package com.kolibree.android.processedbrushings

import com.kolibree.android.processedbrushings.kml.KMLCheckupData
import com.kolibree.android.test.mocks.plaqlessCheckup
import com.kolibree.android.test.mocks.plaqueAggregate
import com.kolibree.android.test.mocks.zoneCheckupData
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqlessCheckup
import com.kolibree.kml.PlaqueAggregate
import com.kolibree.kml.PlaqueStatus
import com.kolibree.kml.SpeedKPI
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlin.math.ceil
import org.junit.Test

class CheckupDataTest {

    @Test
    fun `surface emptyMap returns -1`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData())

        assertEquals(-1, checkupData.surfacePercentage)
    }

    @Test
    fun `coverage is null if no checkup data`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData())
        assertNull(checkupData.coverage)
    }

    @Test
    fun `coverage is equal to surfacePercentage divided by 100 if there is checkup data`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface = 0.67F
        val zoneCheckupData = zoneCheckupData(zoneSurface = zoneSurface)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        assertEquals(0.01f, checkupData.coverage)
    }

    @Test
    fun `isManual returns true when checkupData is empty`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData())

        assertTrue(checkupData.isManual)
    }

    @Test
    fun `isManual returns false checkup data is not empty`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface = 0.67F
        val zoneCheckupData = zoneCheckupData(zoneSurface = zoneSurface)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        assertFalse(checkupData.isManual)
    }

    @Test
    fun `surface singleZoneMap returns ZoneSurface divided by 16`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface = 0.67F
        val zoneCheckupData = zoneCheckupData(zoneSurface = zoneSurface)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        val expectedSurface = ceil(zoneSurface / 16).toInt()

        assertEquals(expectedSurface, checkupData.surfacePercentage)
    }

    @Test
    fun `surface multipleZoneMap returns Sum of zoneSurface divided by 16`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface1 = 67F
        val zoneCheckupData1 = zoneCheckupData(zoneSurface = zoneSurface1)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData1

        val zoneSurface2 = 89F
        val zoneCheckupData2 = zoneCheckupData(zoneSurface = zoneSurface2)
        processedData[MouthZone16.LoMolRiInt] = zoneCheckupData2

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        val expectedSurface = ceil((zoneSurface1 + zoneSurface2) / 16).toInt()

        assertEquals(expectedSurface, checkupData.surfacePercentage)
    }

    /*
    plaqless
     */
    @Test
    fun `Checkupdata returns null if plaqlessCheckup is null`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = null))

        assertNull(checkupData.plaqlessCheckupData)
    }

    @Test
    fun `Checkupdata plaqueStatus returns null if plaqlessCheckup plaqueAggregate is null`() {
        val plaqlessCheckup = plaqlessCheckup(cleanPercent = 0)
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = plaqlessCheckup))

        assertNull(checkupData.plaqlessCheckupData!!.plaqueAggregate)
        MouthZone16.values().forEach { zone ->
            assertNull(checkupData.plaqlessCheckupData.plaqueStatus(zone))
        }
    }

    @Test
    fun `Checkupdata returns cleanPercent from plaqlessCheckup`() {
        val expectedCleanPercent = 6547
        val plaqlessCheckup = plaqlessCheckup(cleanPercent = expectedCleanPercent)
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = plaqlessCheckup))

        assertNotNull(checkupData.plaqlessCheckupData)

        assertEquals(expectedCleanPercent, checkupData.plaqlessCheckupData!!.cleanPercent)
        assertNull(checkupData.plaqlessCheckupData.missedPercent)
        assertNull(checkupData.plaqlessCheckupData.plaqueLeftPercent)
        assertNull(checkupData.plaqlessCheckupData.plaqueAggregate)
    }

    @Test
    fun `Checkupdata returns plaqueLeftPercent from plaqlessCheckup`() {
        val expectedPercent = 6547
        val plaqlessCheckup = plaqlessCheckup(plaqueLeftPercent = expectedPercent)
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = plaqlessCheckup))

        assertNotNull(checkupData.plaqlessCheckupData)

        assertEquals(expectedPercent, checkupData.plaqlessCheckupData!!.plaqueLeftPercent)
        assertNull(checkupData.plaqlessCheckupData.missedPercent)
        assertNull(checkupData.plaqlessCheckupData.cleanPercent)
        assertNull(checkupData.plaqlessCheckupData.plaqueAggregate)
    }

    @Test
    fun `Checkupdata returns missedPercent from plaqlessCheckup`() {
        val expectedPercent = 6547
        val plaqlessCheckup = plaqlessCheckup(missedPercent = expectedPercent)
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = plaqlessCheckup))

        assertNotNull(checkupData.plaqlessCheckupData)

        assertEquals(expectedPercent, checkupData.plaqlessCheckupData!!.missedPercent)
        assertNull(checkupData.plaqlessCheckupData.plaqueLeftPercent)
        assertNull(checkupData.plaqlessCheckupData.cleanPercent)
        assertNull(checkupData.plaqlessCheckupData.plaqueAggregate)
    }

    @Test
    fun `Checkupdata maps plaqueAggregate vector to Map of MouthZone and PlaqueAggregate`() {
        val expectedMap: Map<MouthZone16, PlaqueAggregate> = mapOf(
            MouthZone16.UpIncExt to mock(),
            MouthZone16.LoMolLeInt to mock(),
            MouthZone16.UpIncInt to mock(),
            MouthZone16.LoIncExt to mock()
        )
        val plaqlessCheckup = plaqlessCheckup(plaqueAggregate = expectedMap)
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = plaqlessCheckup))

        assertNotNull(checkupData.plaqlessCheckupData)

        assertEquals(expectedMap, checkupData.plaqlessCheckupData!!.plaqueAggregate)
        assertNull(checkupData.plaqlessCheckupData.missedPercent)
        assertNull(checkupData.plaqlessCheckupData.plaqueLeftPercent)
        assertNull(checkupData.plaqlessCheckupData.cleanPercent)
    }

    @Test
    fun `Checkupdata plaqueStatus for zone return expected status`() {
        val expectedMap: Map<MouthZone16, PlaqueAggregate> = mapOf(
            MouthZone16.UpIncExt to plaqueAggregate(PlaqueStatus.PlaqueLeft),
            MouthZone16.LoMolLeInt to plaqueAggregate(PlaqueStatus.NoPlaqueLeft),
            MouthZone16.UpIncInt to plaqueAggregate(PlaqueStatus.Missed),
            MouthZone16.LoIncExt to plaqueAggregate(PlaqueStatus.NotApplicable)
        )
        val plaqlessCheckup = plaqlessCheckup(plaqueAggregate = expectedMap)
        val checkupData = CheckupDataImpl(createKmlCheckupData(plaqlessCheckup = plaqlessCheckup))

        assertNotNull(checkupData.plaqlessCheckupData)

        assertEquals(expectedMap, checkupData.plaqlessCheckupData!!.plaqueAggregate)
        expectedMap.forEach { (zone, aggregate) ->
            assertEquals(aggregate.plaqueStatus, checkupData.plaqlessCheckupData.plaqueStatus(zone))
        }
    }

    /*
    ZONE SURFACE MAP
     */
    @Test
    fun `zoneSurfaceMap returns ExpectedMap`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface1 = 67F
        val zoneCheckupData1 = zoneCheckupData(zoneSurface = zoneSurface1)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData1

        val zoneSurface2 = 89F
        val zoneCheckupData2 = zoneCheckupData(zoneSurface = zoneSurface2)
        processedData[MouthZone16.LoMolRiInt] = zoneCheckupData2

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        val expectedMap: MutableMap<MouthZone16, Float> = mutableMapOf()
        expectedMap[MouthZone16.LoIncExt] = zoneSurface1
        expectedMap[MouthZone16.LoMolRiInt] = zoneSurface2

        assertEquals(expectedMap.toMap(), checkupData.zoneSurfaceMap)
    }

    @Test
    fun `speedCorrectness returns null when KPI not available`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        MouthZone16.values().forEach { zone ->
            assertNull(checkupData.speedCorrectness(zone))
        }
    }

    @Test
    fun `speedCorrectness returns KPI associate in zoneCheckupData zoneKPI`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface1 = 67F
        val zoneCheckupData1 = zoneCheckupData(zoneSurface = zoneSurface1)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData1

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        assertEquals(SpeedKPI.Correct, checkupData.speedCorrectness(MouthZone16.LoIncExt))
    }

    /*
    PROCESSED DATA
     */
    @Test
    fun `processedData returns ExpectedMap`() {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface1 = 67F
        val zoneCheckupData1 = zoneCheckupData(zoneSurface = zoneSurface1)
        processedData[MouthZone16.LoIncExt] = zoneCheckupData1

        val zoneSurface2 = 89F
        val zoneCheckupData2 = zoneCheckupData(zoneSurface = zoneSurface2)
        processedData[MouthZone16.LoMolRiInt] = zoneCheckupData2

        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = processedData))

        val expectedMap: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        expectedMap[MouthZone16.LoIncExt] = zoneCheckupData1
        expectedMap[MouthZone16.LoMolRiInt] = zoneCheckupData2

        assertEquals(expectedMap.toMap(), checkupData.checkupDataMap)
    }

    @Test
    fun `checkupZoneKpis returns list of checkupZoneKpis`() {
        val checkupData = getCheckupData() as CheckupDataImpl
        val kpis = checkupData.checkupZoneKpis()
        assertEquals(2, kpis.size)
    }

    /*
    correctMovement
     */
    @Test
    fun `correctMovement returns 0 when kpi not available`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = mutableMapOf()))

        assertEquals(0.0, checkupData.correctMovementAverage(), 0.0)
    }

    /*
    correctOrientationAverage
     */
    @Test
    fun `correctOrientationAverage returns 0 when kpi not available`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = mutableMapOf()))

        assertEquals(0.0, checkupData.correctOrientationAverage(), 0.0)
    }

    /*
    underSpeedAverage
     */
    @Test
    fun `underSpeedAverage returns 0 when kpi not available`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = mutableMapOf()))

        assertEquals(0.0, checkupData.underSpeedAverage(), 0.0)
    }

    /*
    correctSpeedAverage
     */
    @Test
    fun `correctSpeedAverage returns 0 when kpi not available`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = mutableMapOf()))

        assertEquals(0.0, checkupData.correctSpeedAverage(), 0.0)
    }

    /*
    overSpeedAverage
     */
    @Test
    fun `overSpeedAverage returns 0 when kpi not available`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = mutableMapOf()))

        assertEquals(0.0, checkupData.overSpeedAverage(), 0.0)
    }

    /*
    overpressureAverage
     */

    @Test
    fun `overpressureAverage returns 0 when kpi is not available`() {
        val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = mutableMapOf()))

        assertEquals(0.0, checkupData.overpressureAverage(), 0.0)
    }

    /*
    UTILS
     */

    private fun getCheckupData(): CheckupData {
        val processedData: MutableMap<MouthZone16, ZoneCheckupData> = mutableMapOf()
        val zoneSurface1 = 67F
        val zoneCheckupData1 = zoneCheckupData(
            zoneSurface = zoneSurface1,
            checkupZoneKpis = CheckupZoneKpis(20, 20, 20, 20, 20, 20)
        )
        processedData[MouthZone16.LoIncExt] = zoneCheckupData1

        val zoneSurface2 = 89F
        val zoneCheckupData2 = zoneCheckupData(
            zoneSurface = zoneSurface2,
            checkupZoneKpis = CheckupZoneKpis(80, 80, 80, 80, 80, 80)
        )
        processedData[MouthZone16.LoMolRiInt] = zoneCheckupData2
        return CheckupDataImpl(createKmlCheckupData(checkupData = processedData))
    }
}

internal fun createKmlCheckupData(
    timestamp: Long = 0,
    duration: Long = 0,
    checkupData: Map<MouthZone16, ZoneCheckupData> = mapOf(),
    plaqlessCheckup: PlaqlessCheckup? = null
): KMLCheckupData {
    return object : KMLCheckupData {
        override fun plaqlessCheckup(): PlaqlessCheckup? = plaqlessCheckup

        override fun timestamp() = timestamp

        override fun durationMs() = duration

        override fun checkupData(): Map<MouthZone16, ZoneCheckupData> = checkupData
    }
}
