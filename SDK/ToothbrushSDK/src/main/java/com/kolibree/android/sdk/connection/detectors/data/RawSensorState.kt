package com.kolibree.android.sdk.connection.detectors.data

import androidx.annotation.Keep
import com.kolibree.android.sdk.math.Axis
import com.kolibree.android.sdk.math.Vector
import com.kolibree.kml.RawData

/**
 * @author aurelien
 *
 * Raw data model
 * @property timestamp packet timestamp
 * @property acceleration non null acceleration [Vector]
 * @property gyroscope non null gyroscope [Vector]
 * @property magnetometer non null magnetometer [Vector]
 */

@Keep
data class RawSensorState(
    val timestamp: Float,
    val acceleration: Vector,
    val gyroscope: Vector,
    val magnetometer: Vector
) {

    fun convertToKmlRawData(): RawData =
        RawData(
            convertRawTimestamp(timestamp),
            gyroscope[Axis.X],
            gyroscope[Axis.Y],
            gyroscope[Axis.Z],
            acceleration[Axis.X],
            acceleration[Axis.Y],
            acceleration[Axis.Z]
        )

    @Keep
    companion object {

        /**
         * Converts a decimal timestamp in seconds to a long one in milliseconds
         *
         * @param timestamp [Float] timestamp in seconds
         * @return [Long] timestamp in milliseconds
         */
        // We can't use TimeUnit since the timestamp parameter has 2 significant decimals
        @Suppress("MagicNumber")
        fun convertRawTimestamp(timestamp: Float): Long = (timestamp * 1000L).toLong()
    }
}
