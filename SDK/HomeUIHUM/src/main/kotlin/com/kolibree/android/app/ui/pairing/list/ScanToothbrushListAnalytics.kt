/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.tracker.AnalyticsEvent

internal object ScanToothbrushListAnalytics {
    fun main() = AnalyticsEvent(name = "ConnectTB_ChooseTB")
    fun goBack() = main() + "GoBack"
    fun blink() = main() + "Blink"
    private fun notfinding() = AnalyticsEvent(name = "ConnectTB_Notfinding_PopUp")
    fun noBrushFoundGetIt() = notfinding() + "OK"
    fun noBrushFoundClose() = notfinding() + "Close"
}
