/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object HomeAnalytics {
    fun mandatoryUpdateStart() = send(AnalyticsEvent("MandatoryUpdate_PopUp_Start"))
    fun mandatoryUpdateCancel() = send(AnalyticsEvent("MandatoryUpdate_PopUp_Cancel"))
    fun noToothbrushConnected() = send(AnalyticsEvent("NoToothbrushConnected_Banner"))
    fun noToothbrushConnectedClose() = send(AnalyticsEvent("NoToothbrushConnected_Banner_Close"))
}
