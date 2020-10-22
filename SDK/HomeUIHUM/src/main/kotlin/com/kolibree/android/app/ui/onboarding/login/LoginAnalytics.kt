/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.login

import com.kolibree.android.app.ui.onboarding.signup.SignUpAnalytics
import com.kolibree.android.tracker.AnalyticsEvent

internal object LoginAnalytics {

    fun main() = AnalyticsEvent(name = "SignIn")

    fun googleButtonClicked() = SignUpAnalytics.main() + "Google"

    fun emailButtonClicked() = SignUpAnalytics.main() + "Email"
}
