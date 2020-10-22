package com.kolibree.android.processedbrushings

import com.kolibree.android.processedbrushings.models.ZonePass
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyOfflineBrushingTest {
    private val goalBrushingTime = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS

    /*
    extractMouthZonePasses
     */

    @Test
    fun extractMouthZonePasses_emptyEvents_returnsMapWith16ZonesAndNoEvents() {
        val emptyZones = mutableMapOf<MouthZone16, List<ZonePass>>().fillEmptyZones()
        val mouthZonePasses = LegacyOfflineBrushingImpl.extractMouthZonePasses(
            emptyZones,
            goalBrushingTime
        )

        assertEquals(MouthZone16.values().size, mouthZonePasses.size)

        MouthZone16.values().forEach {
            assertTrue(mouthZonePasses.getValue(it).isEmpty())
        }
    }

    @Test
    fun extractMouthZonePasses_1Event_returnsMapWith1Entry() {
        val mouthZone = MouthZone16.LoIncExt
        val mapZonePasses = LegacyOfflineBrushingImpl.extractMouthZonePasses(
            mutableMapOf(mouthZone to listOf(ZonePass(0, 10))).fillEmptyZones(),
            goalBrushingTime
        )
        assertEquals(16, mapZonePasses.size)

        assertEquals(1, mapZonePasses.getValue(mouthZone).size)
    }

    @Test
    fun extractMouthZonePasses_multipleZones_multipleEvents_returnsMapWithMultipleEntries() {
        val mouthZone1 = MouthZone16.LoIncExt
        val mouthZone2 = MouthZone16.UpIncInt
        val mapZonePasses = LegacyOfflineBrushingImpl.extractMouthZonePasses(
            mutableMapOf(
                mouthZone1 to listOf(ZonePass(0, 10)),
                mouthZone2 to listOf(ZonePass(0, 10), ZonePass(10, 30))
            )
                .fillEmptyZones(),
            goalBrushingTime
        )
        assertEquals(16, mapZonePasses.size)

        assertEquals(1, mapZonePasses.getValue(mouthZone1).size)
        assertEquals(2, mapZonePasses.getValue(mouthZone2).size)
    }

    /*
    UTILS
     */
    private fun MutableMap<MouthZone16, List<ZonePass>>.fillEmptyZones(): MutableMap<MouthZone16, List<ZonePass>> {
        MouthZone16.values().filterNot { containsKey(it) }
            .forEach {
                put(it, listOf())
            }

        return this
    }
}
