package com.kolibree.android.sdk.connection.detectors.data

import androidx.annotation.Keep
import com.kolibree.kml.MouthZone16

/**
 * Created by aurelien on 25/11/16.
 *
 * RNN detector output
 * @constructor Creates a WeightedMouthZone with a specific zone and a weigh
 * @property zone Brushed zone
 * @property weight Trust value
 */

@Keep
data class WeightedMouthZone(private val zone: MouthZone16, private val weight: Int) {

    /**
     * Get the [MouthZone16]
     *
     * @return non null [MouthZone16]
     */
    fun zone(): MouthZone16 {
        return zone
    }

    /**
     * Get the weight associated with the zone
     *
     * @return integer weight
     */
    fun weight(): Int {
        return weight
    }
}
