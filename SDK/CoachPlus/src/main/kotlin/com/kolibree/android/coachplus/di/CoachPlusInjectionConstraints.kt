/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.di

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import org.threeten.bp.Duration

@Keep
object CoachPlusInjectionConstraints {

    @VisibleForTesting
    const val DEFAULT_BRUSHING_DURATION_SECONDS = 120

    /*
    Backend processed data time unit is tenth of seconds
    Human retinal persistence period is around 44ms
    */
    @VisibleForTesting
    @Suppress("MagicNumber")
    val TICK_PERIOD: Duration = Duration.ofMillis(25)

    const val DI_MAX_FAIL_TIME = "di_max_fail_time"

    const val DI_GOAL_BRUSHING_TIME = "di_goal_brushing_time"
}
