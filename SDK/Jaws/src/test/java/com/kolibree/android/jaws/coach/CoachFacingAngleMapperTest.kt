package com.kolibree.android.jaws.coach

import com.kolibree.kml.MouthZone16
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [CoachFacingAngleMapper] tests
 */
class CoachFacingAngleMapperTest {

    private val mapper = CoachFacingAngleMapper()

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_ANGLESForUpMolLeOcc() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpMolLeOcc)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_ANGLESForLoMolLeOcc() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoMolLeOcc)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_ANGLESForUpMolRiOcc() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpMolRiOcc)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_ANGLESForLoMolRiOcc() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoMolRiOcc)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_BOTTOM_ANGLESForUpIncInt() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_BOTTOM_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpIncInt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_UP_ANGLESForLoIncInt() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_UP_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoIncInt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_ANGLESForLoIncExt() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoIncExt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsFRONT_ANGLESForUpIncExt() {
        assertEquals(
            CoachFacingAngleMapper.FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpIncExt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsLEFT_FRONT_ANGLESForUpMolLeInt() {
        assertEquals(
            CoachFacingAngleMapper.LEFT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpMolLeInt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsLEFT_FRONT_ANGLESForLoMolLeInt() {
        assertEquals(
            CoachFacingAngleMapper.LEFT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoMolLeInt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsLEFT_FRONT_ANGLESForUpMolRiExt() {
        assertEquals(
            CoachFacingAngleMapper.LEFT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpMolRiExt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsLEFT_FRONT_ANGLESForLoMolRiExt() {
        assertEquals(
            CoachFacingAngleMapper.LEFT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoMolRiExt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsRIGHT_FRONT_ANGLESForLoMolLeExt() {
        assertEquals(
            CoachFacingAngleMapper.RIGHT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoMolLeExt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsRIGHT_FRONT_ANGLESForUpMolLeExt() {
        assertEquals(
            CoachFacingAngleMapper.RIGHT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpMolLeExt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsRIGHT_FRONT_ANGLESForUpMolRiInt() {
        assertEquals(
            CoachFacingAngleMapper.RIGHT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.UpMolRiInt)
        )
    }

    @Test
    fun mapZoneToFacingAngle_returnsRIGHT_FRONT_ANGLESForLoMolRiInt() {
        assertEquals(
            CoachFacingAngleMapper.RIGHT_FRONT_ANGLES,
            mapper.mapZoneToFacingAngle(MouthZone16.LoMolRiInt)
        )
    }
}
