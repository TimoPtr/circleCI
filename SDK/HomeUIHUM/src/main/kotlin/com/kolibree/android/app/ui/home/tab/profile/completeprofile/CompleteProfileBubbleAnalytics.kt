/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import com.kolibree.android.tracker.AnalyticsEvent

internal object CompleteProfileBubbleAnalytics {

    private fun main() = AnalyticsEvent("WaysToEarnPoints_CompleteProfile_Banner")

    fun show() = main()

    fun close() = AnalyticsEvent("WaysToEarnPoints_CP_Banner_Close")
}
