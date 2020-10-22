/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.is_brush_ready

import com.kolibree.android.tracker.AnalyticsEvent

internal object IsBrushReadyAnalytics {
    fun main() = AnalyticsEvent(name = "NothingHappening")
    fun connect() = main() + "Yes"
    fun moreHelp() = main() + "No"
}
