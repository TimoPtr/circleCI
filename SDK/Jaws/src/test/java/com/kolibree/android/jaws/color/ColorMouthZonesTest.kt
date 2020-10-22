package com.kolibree.android.jaws.color

import android.graphics.Color
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.MouthZone16.LoIncExt
import com.kolibree.kml.MouthZone16.LoIncInt
import com.kolibree.kml.MouthZone16.LoMolRiExt
import com.kolibree.kml.MouthZone16.LoMolRiInt
import com.kolibree.kml.MouthZone16.LoMolRiOcc
import org.junit.Assert
import org.junit.Test

class ColorMouthZonesTest {

    @Test
    fun `color() returns defaultColor if zone not present`() {
        val defaultColor = Color.WHITE
        val map = mutableMapOf<MouthZone16, Int>()
        map.put(LoMolRiExt, Color.RED)
        map.put(LoMolRiInt, Color.GREEN)
        map.put(LoMolRiOcc, Color.BLUE)

        val colorZones = ColorMouthZones(map, defaultColor)

        Assert.assertEquals(Color.RED, colorZones.color(LoMolRiExt))
        Assert.assertEquals(Color.GREEN, colorZones.color(LoMolRiInt))
        Assert.assertEquals(Color.BLUE, colorZones.color(LoMolRiOcc))
        Assert.assertEquals(Color.WHITE, colorZones.color(LoIncInt))
        Assert.assertEquals(Color.WHITE, colorZones.color(LoIncExt))
    }

    @Test
    fun `oneColor() returns the same color for all zones`() {
        val color = Color.RED
        val colorZones = ColorMouthZones.oneColor(color)
        val allZones = MouthZone16.values()
        for (zone in allZones) {
            Assert.assertEquals(color, colorZones.color(zone))
        }
    }

    @Test
    fun `white() returns white color for all zones`() {
        val color = Color.WHITE
        val colorZones = ColorMouthZones.white()
        val allZones = MouthZone16.values()
        for (zone in allZones) {
            Assert.assertEquals(color, colorZones.color(zone))
        }
    }
}
