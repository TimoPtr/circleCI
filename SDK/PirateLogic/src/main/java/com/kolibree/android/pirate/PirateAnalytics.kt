/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import androidx.annotation.Keep
import com.kolibree.android.tracker.AnalyticsEvent

@Keep
object PirateAnalytics {

    fun main() = AnalyticsEvent(name = "Go Pirate")
}
