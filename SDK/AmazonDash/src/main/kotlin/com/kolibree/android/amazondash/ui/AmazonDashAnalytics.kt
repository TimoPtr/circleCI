/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object AmazonDashAnalytics {
    private fun main() = AnalyticsEvent(name = "AmazonDash")
    private fun congratsScreen() = main() + "Congrats"

    fun connect() = main() + "Connect"
    fun congrats() = send(congratsScreen())
    fun allow() = send(main() + "Allow")
    fun notNow() = send(main() + "NotNow")
    fun congratsNotNow() = send(congratsScreen() + "NotNow")
}
