/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import org.threeten.bp.Duration

@Suppress("MagicNumber")
internal object MindYouSpeedConstants {

    // KML sends us data every ~500ms
    val ESTIMATED_DATA_UPDATE_FREQUENCY: Duration = Duration.ofMillis(500)

    // We need to accumulate 10s of brushing with correct speed to complete the stage
    val STAGE_DURATION: Duration = Duration.ofSeconds(10)

    const val SPEEDOMETER_MIN_VALUE = 0

    const val SPEEDOMETER_MAX_VALUE = 100

    const val SPEEDOMETER_PERFECT_ZONE_MIN_VALUE = 33

    const val SPEEDOMETER_PERFECT_ZONE_MAX_VALUE = 67

    val SPEEDOMETER_DOT_TRANSITION_DURATION: Duration = Duration.ofSeconds(3)
}
