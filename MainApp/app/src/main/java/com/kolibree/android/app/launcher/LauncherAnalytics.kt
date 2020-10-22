/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import com.kolibree.android.tracker.AnalyticsEvent

internal object LauncherAnalytics {

    fun main() = AnalyticsEvent(name = "Welcome")
}
