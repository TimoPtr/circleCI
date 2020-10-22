/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.getready

import com.kolibree.android.tracker.AnalyticsEvent

internal object GetReadyAnalytics {

    fun main() = AnalyticsEvent(name = "Home")

    fun connectMyBrushButtonClicked() = main() + "Connect_TB"

    fun noBrushButtonClicked() = main() + "NoTB"

    fun signInButtonClicked() = main() + "SignIn"
}
