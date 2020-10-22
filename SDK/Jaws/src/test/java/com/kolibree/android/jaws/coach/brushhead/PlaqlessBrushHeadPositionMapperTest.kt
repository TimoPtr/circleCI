/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.brushhead

import com.kolibree.kml.MouthZone16
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * [PlaqlessBrushHeadPositionMapperTest] tests
 */
class PlaqlessBrushHeadPositionMapperTest {

    private val mapper = PlaqlessBrushHeadPositionMapper()

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoMolLeOcc() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoMolLeOcc,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoMolLeOcc)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpMolRiOcc() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpMolRiOcc,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpMolRiOcc)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpMolLeOcc() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpMolLeOcc,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpMolLeOcc)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoMolRiOcc() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoMolRiOcc,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoMolRiOcc)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpIncInt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpIncInt,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpIncInt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoIncInt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoIncInt,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoIncInt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoIncExt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoIncExt,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoIncExt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpIncExt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpIncExt,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpIncExt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpMolLeInt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpMolLeInt,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpMolLeInt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoMolLeInt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoMolLeInt,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoMolLeInt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpMolRiExt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpMolRiExt,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpMolRiExt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoMolRiExt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoMolRiExt,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoMolRiExt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoMolLeExt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoMolLeExt,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoMolLeExt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpMolLeExt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpMolLeExt,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpMolLeExt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionUpMolRiInt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_UpMolRiInt,
            mapper.mapZoneToPositionMatrix(MouthZone16.UpMolRiInt)
        )
    }

    @Test
    fun mapZoneToPositionMatrix_returnsPositionLoMolRiInt() {
        assertArrayEquals(
            PlaqlessBrushHeadPositionMapper.POSITION_MATRIX_LoMolRiInt,
            mapper.mapZoneToPositionMatrix(MouthZone16.LoMolRiInt)
        )
    }
}
