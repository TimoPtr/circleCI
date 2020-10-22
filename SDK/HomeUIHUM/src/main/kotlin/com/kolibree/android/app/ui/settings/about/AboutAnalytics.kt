/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about

import com.kolibree.android.tracker.AnalyticsEvent

internal object AboutAnalytics {
    fun main() = AnalyticsEvent(name = "About")
    fun goBack() = main() + "GoBack"
    fun twitter() = main() + "Twitter"
    fun instagram() = main() + "Insta"
    fun facebook() = main() + "FB"
    fun website() = main() + "Website"
    fun licenses() = main() + "Licenses"
    fun accountId() = main() + "AccountID"
}
