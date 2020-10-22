/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object NotificationsAnalytics {
    fun main() = AnalyticsEvent(name = "Notification")
    private fun syncReminder() = main() + "SyncReminder"
    private fun joinMailing() = main() + "JoinMailing"

    fun syncReminder(isOn: Boolean) = send(syncReminder() + isEnabled(isOn))
    fun joinMailing(isOn: Boolean) = send(joinMailing() + isEnabled(isOn))
    fun goBack() = send(main() + "GoBack")

    private fun isEnabled(enabled: Boolean) = if (enabled) "On" else "Off"
}
