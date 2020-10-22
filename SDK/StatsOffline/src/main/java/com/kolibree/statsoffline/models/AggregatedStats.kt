/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.kolibree.kml.MouthZone16

/**
 * Common interface for all aggregated data classes
 */
@Keep
interface AggregatedStats : BrushingMotionStats {
    val profileId: Long
    val averageDuration: Double
    val averageSurface: Double
    val averageCheckup: AverageCheckup
    val totalSessions: Int
}

/**
 * Common interface for all aggregated data classes that include more than one day
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
interface MultiDayAggregatedStats : AggregatedStats {
    val sessionsPerDay: Double
}

@Keep
interface BrushingMotionStats {
    /**
     * Average of correct movement on all [MouthZone16]
     *
     * 0 if the stats only contain offline brushings
     */
    val correctMovementAverage: Double

    /**
     * Average of correct orientation on all [MouthZone16]
     *
     * 0 if the stats only contain offline brushings
     */
    val correctOrientationAverage: Double

    /**
     * Average of under speed on all [MouthZone16]
     *
     * 0 if the stats only contain offline brushings
     */
    val underSpeedAverage: Double

    /**
     * Average of correct speed on all [MouthZone16]
     *
     * 0 if the stats only contain offline brushings
     */
    val correctSpeedAverage: Double

    /**
     * Average of over speed on all [MouthZone16]
     *
     * 0 if the stats only contain offline brushings
     */
    val overSpeedAverage: Double
    /**
     * Average of over pressure on all [MouthZone16]
     *
     * 0 if the stats only contain offline brushings
     */
    val overPressureAverage: Double
}
