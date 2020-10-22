/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.inprogress

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object InProgressOtaAnalytics {
    fun main() = AnalyticsEvent(name = "TBSettings_PopUpdate")

    fun done() = send(main() + "Done")

    fun fail() = send(AnalyticsEvent(name = "TBSettings_UpdateFailed_done"))
}
