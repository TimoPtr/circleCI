/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.emailcheck

import com.kolibree.android.tracker.AnalyticsEvent

internal object EmailCheckAnalytics {

    fun main() = AnalyticsEvent(name = "EmailCheck")
}
