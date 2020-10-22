/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.start

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object StartOtaAnalytics {
    fun main() = AnalyticsEvent(name = "TBSettings_StartUpdate")

    fun startUpgrade() = send(main() + "Update")
    fun cancelUpgrade() = send(main() + "Cancel")
}
