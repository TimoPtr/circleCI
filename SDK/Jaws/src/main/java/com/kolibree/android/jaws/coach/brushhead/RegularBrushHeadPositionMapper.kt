/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.brushhead

import androidx.annotation.VisibleForTesting
import com.kolibree.kml.MouthZone16

/** [BrushHeadPositionMapper] implementation for regular brush head 3D model */
internal class RegularBrushHeadPositionMapper : BrushHeadPositionMapper {

    @SuppressWarnings("ComplexMethod")
    override fun mapZoneToPositionMatrix(mouthZone16: MouthZone16): Array<FloatArray> {
        return when (mouthZone16) {
            MouthZone16.LoMolLeOcc -> POSITION_MATRIX_LoMolLeOcc
            MouthZone16.LoMolRiOcc -> POSITION_MATRIX_LoMolRiOcc
            MouthZone16.UpMolLeOcc -> POSITION_MATRIX_UpMolLeOcc
            MouthZone16.UpMolRiOcc -> POSITION_MATRIX_UpMolRiOcc
            MouthZone16.LoMolRiExt -> POSITION_MATRIX_LoMolRiExt
            MouthZone16.LoMolLeExt -> POSITION_MATRIX_LoMolLeExt
            MouthZone16.UpMolLeExt -> POSITION_MATRIX_UpMolLeExt
            MouthZone16.UpMolRiExt -> POSITION_MATRIX_UpMolRiExt
            MouthZone16.UpIncExt -> POSITION_MATRIX_UpIncExt
            MouthZone16.LoIncExt -> POSITION_MATRIX_LoIncExt
            MouthZone16.LoMolRiInt -> POSITION_MATRIX_LoMolRiInt
            MouthZone16.LoMolLeInt -> POSITION_MATRIX_LoMolLeInt
            MouthZone16.UpMolLeInt -> POSITION_MATRIX_UpMolLeInt
            MouthZone16.UpMolRiInt -> POSITION_MATRIX_UpMolRiInt
            MouthZone16.UpIncInt -> POSITION_MATRIX_UpIncInt
            MouthZone16.LoIncInt -> POSITION_MATRIX_LoIncInt
        }
    }

    companion object {

        @VisibleForTesting
        val POSITION_MATRIX_LoMolLeOcc = arrayOf(
            floatArrayOf(-2.4f, -0.4f, 0f),
            floatArrayOf(35f, 15f, 0f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_LoMolRiOcc = arrayOf(
            floatArrayOf(2.4f, -0.4f, 0f),
            floatArrayOf(35f, -15f, 0f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpMolLeOcc = arrayOf(
            floatArrayOf(-2.6f, 0.4f, 0f),
            floatArrayOf(-35f, 15f, 0f), floatArrayOf(0f, 0f, 180f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpMolRiOcc = arrayOf(
            floatArrayOf(2.6f, 0.4f, 0f),
            floatArrayOf(-35f, -15f, 0f), floatArrayOf(0f, 0f, 180f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_LoMolRiExt = arrayOf(
            floatArrayOf(3.3f, -1.2f, 0f),
            floatArrayOf(35f, -15f, 0f), floatArrayOf(0f, 0f, -60f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_LoMolLeExt = arrayOf(
            floatArrayOf(-3.3f, -1.2f, 0f),
            floatArrayOf(35f, 15f, 0f), floatArrayOf(0f, 0f, 60f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpMolLeExt = arrayOf(
            floatArrayOf(-3.4f, 1.2f, 0f),
            floatArrayOf(-30f, 10f, 0f), floatArrayOf(0f, 0f, 120f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpMolRiExt = arrayOf(
            floatArrayOf(3.4f, 1.2f, 0f),
            floatArrayOf(-30f, -10f, 0f), floatArrayOf(0f, 0f, -120f)
        )

        // This position uses world rotation to make the top of the brush face the left of the world
        // so X and Z axis are inverted in the position vector (-Z, Y, X)
        @VisibleForTesting
        val POSITION_MATRIX_UpIncExt = arrayOf(
            floatArrayOf(-4f, 3.6f, -1f),
            floatArrayOf(0f, 90f, 0f), floatArrayOf(0f, 0f, 60f)
        )

        // This position uses world rotation to make the top of the brush face the left of the world
        // so X and Z axis are inverted in the position vector (-Z, Y, X)
        @VisibleForTesting
        val POSITION_MATRIX_LoIncExt = arrayOf(
            floatArrayOf(-4f, -3.6f, -1f),
            floatArrayOf(0f, 90f, 0f), floatArrayOf(0f, 0f, 130f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_LoMolRiInt = arrayOf(
            floatArrayOf(1.3f, -0.7f, 0f),
            floatArrayOf(35f, -15f, 0f), floatArrayOf(0f, 0f, 60f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_LoMolLeInt = arrayOf(
            floatArrayOf(-1.2f, -0.7f, 0f),
            floatArrayOf(30f, 15f, 0f), floatArrayOf(0f, 0f, -60f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpMolLeInt = arrayOf(
            floatArrayOf(-1.4f, 1.0f, 0f),
            floatArrayOf(-30f, 15f, 0f), floatArrayOf(0f, 0f, -120f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpMolRiInt = arrayOf(
            floatArrayOf(1.4f, 1.0f, 0f),
            floatArrayOf(-30f, -15f, 0f), floatArrayOf(0f, 0f, 120f)
        )

        @VisibleForTesting
        val POSITION_MATRIX_UpIncInt = arrayOf(
            floatArrayOf(0f, 3.5f, 0f),
            floatArrayOf(50f, 0f, 0f), floatArrayOf(0f, 0f, 180f)
        )

        // This position uses world rotation to make the top of the brush face the bottom of the world
        // so all axis are inverted in the position vector (Y, Z, X)
        @VisibleForTesting
        val POSITION_MATRIX_LoIncInt = arrayOf(
            floatArrayOf(0f, 3.5f, 0f),
            floatArrayOf(130f, 0f, 0f), floatArrayOf(0f, 180f, 180f)
        )
    }
}
