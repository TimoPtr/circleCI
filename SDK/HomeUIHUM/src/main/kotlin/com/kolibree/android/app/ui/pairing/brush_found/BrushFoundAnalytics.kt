/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.brush_found

import com.kolibree.android.tracker.AnalyticsEvent

internal object BrushFoundAnalytics {
    fun main() = AnalyticsEvent(name = "ConnectTB_Success")
    fun connect() = main() + "Yes"
    fun notRightToothbrush() = main() + "No"
}
