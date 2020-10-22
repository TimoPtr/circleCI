/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself

import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent

internal object RewardYourselfAnalytics {
    private fun main() = AnalyticsEvent(name = "RewardYourself")
    fun click(itemId: String) = Analytics.send(main() + "click" + ("productID" to itemId))
}
