/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.model_mismatch

import com.kolibree.android.tracker.AnalyticsEvent

internal object ModelMismatchAnalytics {
    fun main() = AnalyticsEvent(name = "WrongApp")
    fun changeApp() = main() + "ChangeApp"
    fun continueAnyway() = main() + "Continue"
}
