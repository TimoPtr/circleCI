/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object SupportCardAnalytics {
    private fun main() = AnalyticsEvent("Questions")
    fun productSupport() = send(main() + "product")

    fun oralCareSupport() = send(main() + "OralCare")
}
