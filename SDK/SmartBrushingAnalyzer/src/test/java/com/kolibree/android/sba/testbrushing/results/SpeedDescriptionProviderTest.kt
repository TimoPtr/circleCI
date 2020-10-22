package com.kolibree.android.sba.testbrushing.results

import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupOverspeed
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.InsideIncisives
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.InsideMolars
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.Occlusal
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.OutsideIncisives
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.GroupZones.OutsideMolars
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantOverspeed
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.BottomLeft
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.BottomRight
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.TopLeft
import com.kolibree.android.sba.testbrushing.results.SpeedDescriptionProvider.QuadrantZones.TopRight
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test

class SpeedDescriptionProviderTest : BaseUnitTest() {

    private val context = mock<Context>()

    private lateinit var provider: SpeedDescriptionProvider

    override fun setup() {
        super.setup()

        provider = spy(SpeedDescriptionProvider(context))
    }

    @Test
    fun `if overspeed for group gt 50% then return description with max group name`() {
        val speedZones = emptyMap<MouthZone16, Int>()
        val speedResult =
            BrushingResults(overSpeedPercentageMouthZones = speedZones)
        val max = GroupOverspeed(Occlusal, 51)
        doReturn(max).whenever(provider).maxGroupOverspeed(speedZones)
        val groupName = "groupName"
        whenever(context.getString(R.string.speed_group_occlusal)).thenReturn(groupName)
        val description = "description for group name"
        whenever(context.getString(R.string.speed_description_by_groups, groupName)).thenReturn(description)

        Assert.assertEquals(description, provider.description(speedResult))
        verify(context).getString(R.string.speed_group_occlusal)
        verify(context).getString(R.string.speed_description_by_groups, groupName)
    }

    @Test
    fun `if overspeed for quadrant gt 25% then return description with max quadrant name`() {
        val speedZones = emptyMap<MouthZone16, Int>()
        val speedResult =
            BrushingResults(overSpeedPercentageMouthZones = speedZones)
        whenever(provider.maxGroupOverspeed(speedZones)).thenReturn(GroupOverspeed(Occlusal, 49))

        val max = QuadrantOverspeed(TopRight, 26)
        doReturn(max).whenever(provider).maxQuadrantOverspeed(speedZones)

        val quadrantName = "quadrantName"
        whenever(context.getString(R.string.speed_quadrant_top_right)).thenReturn(quadrantName)
        val description = "description for quadrant name"
        whenever(context.getString(R.string.speed_description_by_groups, quadrantName)).thenReturn(description)

        Assert.assertEquals(description, provider.description(speedResult))
        verify(context).getString(R.string.speed_quadrant_top_right)
        verify(context).getString(R.string.speed_description_by_groups, quadrantName)
    }

    @Test
    fun `if average overspeed gt 5% then return description with average overspeed`() {
        val speedZones = emptyMap<MouthZone16, Int>()
        val speedResult =
            BrushingResults(overSpeedPercentageMouthZones = speedZones)
        whenever(provider.maxGroupOverspeed(speedZones)).thenReturn(GroupOverspeed(Occlusal, 49))
        whenever(provider.maxQuadrantOverspeed(speedZones)).thenReturn(QuadrantOverspeed(TopRight, 24))

        val overspeed = 13
        val overspeedPercentTextual = "13%"
        val description = "overspeed description"
        whenever(provider.averageOverspeed(speedZones)).thenReturn(overspeed)
        whenever(provider.textualPercentage(overspeed)).thenReturn(overspeedPercentTextual)
        whenever(context.getString(R.string.speed_description_average, overspeedPercentTextual)).thenReturn(description)

        Assert.assertEquals(description, provider.description(speedResult))
        verify(context).getString(R.string.speed_description_average, overspeedPercentTextual)
        verify(provider).textualPercentage(overspeed)
    }

    @Test
    fun `description for no overspeed zones returns speed_description_all_good`() {
        val allGoodText = "All good"
        whenever(context.getString(R.string.speed_description_all_good)).thenReturn(allGoodText)

        val result =
            BrushingResults(overSpeedPercentageMouthZones = emptyMap())
        Assert.assertEquals(allGoodText, provider.description(result))
    }

    @Test
    fun `group Occlusal contains 4 zones`() {
        Assert.assertEquals(4, Occlusal.zones.size)
        Assert.assertTrue(Occlusal.zones.contains(MouthZone16.LoMolLeOcc))
        Assert.assertTrue(Occlusal.zones.contains(MouthZone16.LoMolRiOcc))
        Assert.assertTrue(Occlusal.zones.contains(MouthZone16.UpMolRiOcc))
        Assert.assertTrue(Occlusal.zones.contains(MouthZone16.UpMolLeOcc))
    }

    @Test
    fun `Occlusal name is speed_group_occlusal`() {
        Assert.assertEquals(R.string.speed_group_occlusal, Occlusal.nameRes)
    }

    @Test
    fun `group OutsideMolars contains 4 zones`() {
        Assert.assertEquals(4, Occlusal.zones.size)
        Assert.assertTrue(OutsideMolars.zones.contains(MouthZone16.LoMolRiExt))
        Assert.assertTrue(OutsideMolars.zones.contains(MouthZone16.LoMolLeExt))
        Assert.assertTrue(OutsideMolars.zones.contains(MouthZone16.UpMolRiExt))
        Assert.assertTrue(OutsideMolars.zones.contains(MouthZone16.UpMolLeExt))
    }

    @Test
    fun `OutsideMolars name is speed_group_outside_molars`() {
        Assert.assertEquals(R.string.speed_group_outside_molars, OutsideMolars.nameRes)
    }

    @Test
    fun `group InsideMolars contains 4 zones`() {
        Assert.assertEquals(4, InsideMolars.zones.size)
        Assert.assertTrue(InsideMolars.zones.contains(MouthZone16.LoMolRiInt))
        Assert.assertTrue(InsideMolars.zones.contains(MouthZone16.LoMolLeInt))
        Assert.assertTrue(InsideMolars.zones.contains(MouthZone16.UpMolRiInt))
        Assert.assertTrue(InsideMolars.zones.contains(MouthZone16.UpMolLeInt))
    }

    @Test
    fun `InsideMolars name is speed_group_outside_molars`() {
        Assert.assertEquals(R.string.speed_group_inside_molars, InsideMolars.nameRes)
    }

    @Test
    fun `group OutsideIncisives contains 2 zones`() {
        Assert.assertEquals(2, OutsideIncisives.zones.size)
        Assert.assertTrue(OutsideIncisives.zones.contains(MouthZone16.LoIncExt))
        Assert.assertTrue(OutsideIncisives.zones.contains(MouthZone16.UpIncExt))
    }

    @Test
    fun `OutsideIncisives name is speed_group_outside_molars`() {
        Assert.assertEquals(R.string.speed_group_outside_incisives, OutsideIncisives.nameRes)
    }

    @Test
    fun `group InsideIncisives contains 2 zones`() {
        Assert.assertEquals(2, InsideIncisives.zones.size)
        Assert.assertTrue(InsideIncisives.zones.contains(MouthZone16.LoIncInt))
        Assert.assertTrue(InsideIncisives.zones.contains(MouthZone16.UpIncInt))
    }

    @Test
    fun `InsideIncisives name is speed_group_outside_molars`() {
        Assert.assertEquals(R.string.speed_group_inside_incisives, InsideIncisives.nameRes)
    }

    @Test
    fun `quadrant TopLeft contains 3 zones`() {
        Assert.assertEquals(3, TopLeft.zones.size)
        Assert.assertTrue(TopLeft.zones.contains(MouthZone16.UpMolLeOcc))
        Assert.assertTrue(TopLeft.zones.contains(MouthZone16.UpMolLeExt))
        Assert.assertTrue(TopLeft.zones.contains(MouthZone16.UpMolLeInt))
    }

    @Test
    fun `TopLeft name is speed_quadrant_top_left`() {
        Assert.assertEquals(R.string.speed_quadrant_top_left, TopLeft.nameRes)
    }

    @Test
    fun `quadrant TopRight contains 3 zones`() {
        Assert.assertEquals(3, TopRight.zones.size)
        Assert.assertTrue(TopRight.zones.contains(MouthZone16.UpMolRiExt))
        Assert.assertTrue(TopRight.zones.contains(MouthZone16.UpMolRiOcc))
        Assert.assertTrue(TopRight.zones.contains(MouthZone16.UpMolRiInt))
    }

    @Test
    fun `TopRight name is speed_quadrant_top_right`() {
        Assert.assertEquals(R.string.speed_quadrant_top_right, TopRight.nameRes)
    }

    @Test
    fun `quadrant BottomLeft contains 3 zones`() {
        Assert.assertEquals(3, BottomLeft.zones.size)
        Assert.assertTrue(BottomLeft.zones.contains(MouthZone16.LoMolLeExt))
        Assert.assertTrue(BottomLeft.zones.contains(MouthZone16.LoMolLeOcc))
        Assert.assertTrue(BottomLeft.zones.contains(MouthZone16.LoMolLeInt))
    }

    @Test
    fun `BottomLeft name is speed_quadrant_bottom_left`() {
        Assert.assertEquals(R.string.speed_quadrant_bottom_left, BottomLeft.nameRes)
    }

    @Test
    fun `quadrant BottomRight contains 3 zones`() {
        Assert.assertEquals(3, BottomRight.zones.size)
        Assert.assertTrue(BottomRight.zones.contains(MouthZone16.LoMolRiExt))
        Assert.assertTrue(BottomRight.zones.contains(MouthZone16.LoMolRiOcc))
        Assert.assertTrue(BottomRight.zones.contains(MouthZone16.LoMolRiInt))
    }

    @Test
    fun `BottomRight name is speed_quadrant_bottom_right`() {
        Assert.assertEquals(R.string.speed_quadrant_bottom_right, BottomRight.nameRes)
    }

    @Test
    fun `averageOverspeed returns average value of all zones`() {
        val oneSpeed = 56
        val oneSpeedMap = MouthZone16.values().map {
            it to oneSpeed
        }.toMap()
        Assert.assertEquals(oneSpeed, provider.averageOverspeed(oneSpeedMap))

        var startValue = 1
        val incrementalSpeedMap = MouthZone16.values().map {
            it to startValue++
        }.toMap()
        val totalOverspeed = (1..16).sum()
        val expectedAverageOverspeed = totalOverspeed / 16
        Assert.assertEquals(expectedAverageOverspeed, provider.averageOverspeed(incrementalSpeedMap))
    }

    @Test
    fun `percentageForZones returns average value for given zones`() {
        val speedZones = mapOf((MouthZone16.UpMolLeExt to 10),
            (MouthZone16.UpMolRiExt to 20),
            (MouthZone16.UpIncInt to 30),
            (MouthZone16.UpMolLeOcc to 40),
            (MouthZone16.LoMolRiExt to 50))

        val percentageZones1 = (10 + 20) / 2
        Assert.assertEquals(percentageZones1, provider.percentageForZones(speedZones,
            listOf(MouthZone16.UpMolLeExt, MouthZone16.UpMolRiExt)))

        val percentageZones2 = (20 + 30 + 40) / 3
        Assert.assertEquals(percentageZones2, provider.percentageForZones(speedZones,
            listOf(MouthZone16.UpMolRiExt, MouthZone16.UpIncInt, MouthZone16.UpMolLeOcc)))

        val percentageZones3 = (10 + 20 + 30 + 40 + 50) / 5
        Assert.assertEquals(percentageZones3, provider.percentageForZones(speedZones,
            listOf(MouthZone16.UpMolLeExt, MouthZone16.UpMolRiExt, MouthZone16.UpIncInt, MouthZone16.UpMolLeOcc, MouthZone16.LoMolRiExt)))
    }

    @Test
    fun `maxQuadrantOverspeed returns quadrant with maximum overspeed`() {
        val overspeedMap = mutableMapOf<MouthZone16, Int>()

        // average value -> 20
        val quadrantTopLeft = listOf(10, 20, 30)
        overspeedMap.putAll(TopLeft.zones.zip(quadrantTopLeft).toMap())

        // average value -> 30
        val quadrantTopRight = listOf(20, 30, 40)
        overspeedMap.putAll(TopRight.zones.zip(quadrantTopRight).toMap())

        // average value -> 70 -> MAX
        val quadrantBottomLeft = listOf(80, 100, 30)
        overspeedMap.putAll(BottomLeft.zones.zip(quadrantBottomLeft).toMap())

        // average value -> 50
        val quadrantBottomRight = listOf(30, 40, 80)
        overspeedMap.putAll(BottomRight.zones.zip(quadrantBottomRight).toMap())

        val expectedGroup = QuadrantOverspeed(BottomLeft, 70)
        Assert.assertEquals(expectedGroup, provider.maxQuadrantOverspeed(overspeedMap))
    }

    @Test
    fun `maxGroupOverspeed returns group with maximum overspeed`() {
        val overspeedMap = mutableMapOf<MouthZone16, Int>()

        // average value -> 10
        val occlusalOverspeed = listOf(10, 10, 10, 10)
        overspeedMap.putAll(Occlusal.zones.zip(occlusalOverspeed).toMap())

        // average value -> 25
        val insideIncisivesOverspeed = listOf(20, 30)
        overspeedMap.putAll(InsideIncisives.zones.zip(insideIncisivesOverspeed).toMap())

        // average value -> 85 -> MAX
        val outsideMolarsOverspeed = listOf(80, 90, 80, 90)
        overspeedMap.putAll(OutsideMolars.zones.zip(outsideMolarsOverspeed).toMap())

        // average value -> 50
        val insideMolarsOverspeed = listOf(30, 40, 80, 50)
        overspeedMap.putAll(InsideMolars.zones.zip(insideMolarsOverspeed).toMap())

        // average value -> 75
        val outsideIncisivesOverspeed = listOf(100, 50)
        overspeedMap.putAll(OutsideIncisives.zones.zip(outsideIncisivesOverspeed).toMap())

        val expectedGroup = GroupOverspeed(OutsideMolars, 85)
        Assert.assertEquals(expectedGroup, provider.maxGroupOverspeed(overspeedMap))
    }
}
