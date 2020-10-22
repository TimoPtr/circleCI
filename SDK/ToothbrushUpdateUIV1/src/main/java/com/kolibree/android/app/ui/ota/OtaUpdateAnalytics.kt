/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.tracker.AnalyticsEvent

internal object OtaUpdateAnalytics {
    @JvmStatic
    fun tbUpdate() = AnalyticsEvent(name = "TBUpdate")

    @JvmStatic
    fun blockedNotCharging() = tbUpdate() + "UpdateBlocked_NotCharging"

    @JvmStatic
    fun start() = tbUpdate() + "UpdateStart"

    @JvmStatic
    fun success() = tbUpdate() + "UpdateSuccess"

    @JvmStatic
    fun failure(errorMessage: String) = tbUpdate() + "UpdateError" + Pair("error", errorMessage)

    @JvmStatic
    fun tbSettings() = AnalyticsEvent("TBSettings")

    @JvmStatic
    fun popUpUpdateDone() = tbSettings() + AnalyticsEvent("PopUpUpdate_Done")

    const val KEY_ERROR = "error"
}
