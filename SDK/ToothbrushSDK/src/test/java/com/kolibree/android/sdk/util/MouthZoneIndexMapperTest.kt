package com.kolibree.android.sdk.util

import com.kolibree.kml.MouthZone16
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * [MouthZoneIndexMapper] test unit
 *
 *
 * The following tests make sure that we map the hardware zone index to the good MouthZone16 The
 * map that the brushes are using can be found in the document below
 * https://docs.google.com/document/d/1n5b8xPcIhNvaraIVmipXMUWI_JBFVuFU1TM63zbGphU/edit#heading=h.m698t4fbqg84
 */
class MouthZoneIndexMapperTest {

    @Test
    fun `lookup by HardwareZoneIndex BottomMolarLeftOcclusal`() {
        assertEquals(MouthZone16.LoMolLeOcc, MouthZoneIndexMapper.mapZoneIdToMouthZone16(0))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomMolarLeftExterior`() {
        assertEquals(MouthZone16.LoMolLeExt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(1))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomMolarLeftInterior`() {
        assertEquals(MouthZone16.LoMolLeInt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(2))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomMolarRightOcclusal`() {
        assertEquals(MouthZone16.LoMolRiOcc, MouthZoneIndexMapper.mapZoneIdToMouthZone16(3))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomMolarRightExterior`() {
        assertEquals(MouthZone16.LoMolRiExt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(4))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomMolarRightInterior`() {
        assertEquals(MouthZone16.LoMolRiInt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(5))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomIncisorExterior`() {
        assertEquals(MouthZone16.LoIncExt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(6))
    }

    @Test
    fun `lookup by HardwareZoneIndex BottomIncisorInterior`() {
        assertEquals(MouthZone16.LoIncInt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(7))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopMolarLeftOcclusal`() {
        assertEquals(MouthZone16.UpMolLeOcc, MouthZoneIndexMapper.mapZoneIdToMouthZone16(8))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopMolarLeftExterior`() {
        assertEquals(MouthZone16.UpMolLeExt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(9))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopMolarLeftInterior`() {
        assertEquals(MouthZone16.UpMolLeInt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(10))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopMolarRightOcclusal`() {
        assertEquals(MouthZone16.UpMolRiOcc, MouthZoneIndexMapper.mapZoneIdToMouthZone16(11))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopMolarRightExterior`() {
        assertEquals(MouthZone16.UpMolRiExt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(12))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopMolarRightInterior`() {
        assertEquals(MouthZone16.UpMolRiInt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(13))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopIncisorExterior`() {
        assertEquals(MouthZone16.UpIncExt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(14))
    }

    @Test
    fun `lookup by HardwareZoneIndex TopIncisorInterior`() {
        assertEquals(MouthZone16.UpIncInt, MouthZoneIndexMapper.mapZoneIdToMouthZone16(15))
    }

    @Test
    fun `match hardware index and MouthZone16`() {
        MouthZone16.values().forEach { zone ->
            val zoneAfterMap =
                MouthZoneIndexMapper.mapZoneIdToMouthZone16(MouthZoneIndexMapper.mapMouthZone16ToId(zone).toInt())
            assertEquals(zone, zoneAfterMap)
        }
    }
}
