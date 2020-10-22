/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import com.kolibree.android.tracker.AnalyticsEvent

internal object WakeYourBrushAnalytics {
    fun main() = AnalyticsEvent(name = "ConnectTB")
}
