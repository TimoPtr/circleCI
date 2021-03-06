/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.enteremail

import com.kolibree.android.tracker.AnalyticsEvent

internal object EnterEmailAnalytics {

    fun main() = AnalyticsEvent(name = "Onboarding_Email")

    fun finishButtonClicked() = main() + "Finish"
}
