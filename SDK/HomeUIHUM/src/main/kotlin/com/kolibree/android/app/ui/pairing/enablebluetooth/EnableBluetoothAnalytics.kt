/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.enablebluetooth

import com.kolibree.android.tracker.AnalyticsEvent

internal object EnableBluetoothAnalytics {
    fun main() = AnalyticsEvent(name = "EnableBluetooth")
    fun activate() = main() + "Activate"
}
