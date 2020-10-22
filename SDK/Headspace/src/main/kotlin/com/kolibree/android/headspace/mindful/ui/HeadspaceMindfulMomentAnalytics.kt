/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object HeadspaceMindfulMomentAnalytics {
    fun main() = AnalyticsEvent(name = "HeadSpace_MM")

    fun open() = send(main() + "Open")

    fun close() = send(main() + "Close")
    fun collectPoints() = send(main() + "CollectPoints")
    fun visitHeadspace() = send(main() + "VisitHeadSpace")
}
