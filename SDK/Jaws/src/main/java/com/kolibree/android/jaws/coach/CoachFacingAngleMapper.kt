package com.kolibree.android.jaws.coach

import androidx.annotation.VisibleForTesting
import com.kolibree.kml.MouthZone16

/**
 * Helper for mapping zones to angles and positions in the 3D space
 */
internal class CoachFacingAngleMapper {

    /**
     * Map a zone to a jaws facing 3D angle
     *
     * @param zone16 non null [MouthZone16]
     * @return float[] X and Y angles in radians
     */
    fun mapZoneToFacingAngle(zone16: MouthZone16): FloatArray {
        return when (zone16) {
            MouthZone16.UpMolLeOcc,
            MouthZone16.LoMolLeOcc,
            MouthZone16.UpMolRiOcc,
            MouthZone16.LoMolRiOcc,
            MouthZone16.LoIncExt,
            MouthZone16.UpIncExt -> FRONT_ANGLES

            MouthZone16.UpIncInt -> FRONT_BOTTOM_ANGLES

            MouthZone16.LoIncInt -> FRONT_UP_ANGLES

            MouthZone16.UpMolLeInt,
            MouthZone16.LoMolLeInt,
            MouthZone16.UpMolRiExt,
            MouthZone16.LoMolRiExt -> LEFT_FRONT_ANGLES

            MouthZone16.LoMolLeExt,
            MouthZone16.UpMolLeExt,
            MouthZone16.UpMolRiInt,
            MouthZone16.LoMolRiInt -> RIGHT_FRONT_ANGLES
        }
    }

    companion object {

        const val FACING_NEGATIVE_ANGLE = -0.01f

        const val FACING_FRONT_ANGLE = 0f

        const val FACING_POSITIVE_ANGLE = 0.01f

        @VisibleForTesting
        val FRONT_ANGLES = floatArrayOf(FACING_FRONT_ANGLE, FACING_FRONT_ANGLE)

        @VisibleForTesting
        val FRONT_BOTTOM_ANGLES = floatArrayOf(FACING_FRONT_ANGLE, FACING_NEGATIVE_ANGLE)

        @VisibleForTesting
        val FRONT_UP_ANGLES = floatArrayOf(FACING_FRONT_ANGLE, FACING_POSITIVE_ANGLE)

        @VisibleForTesting
        val LEFT_FRONT_ANGLES = floatArrayOf(FACING_NEGATIVE_ANGLE, FACING_FRONT_ANGLE)

        @VisibleForTesting
        val RIGHT_FRONT_ANGLES = floatArrayOf(FACING_POSITIVE_ANGLE, FACING_FRONT_ANGLE)
    }
}
