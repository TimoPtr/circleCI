/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.help

import com.kolibree.android.tracker.AnalyticsEvent

internal object HelpAnalytics {
    fun main() = AnalyticsEvent(name = "Help")
    fun helpCenter() = main() + "HelpCenter"
    fun contactUs() = main() + "ContactUs"
    fun goBack() = main() + "GoBack"
}
