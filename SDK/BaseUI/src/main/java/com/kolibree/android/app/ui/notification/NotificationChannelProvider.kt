/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.notification

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.baseui.R
import javax.inject.Inject

@VisibleForApp
class NotificationChannelProvider @Inject constructor(private val applicationContext: ApplicationContext) {

    fun getRemindersChannel(): NotificationChannel {
        return NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL,
            applicationContext.getString(R.string.push_notification_channel_reminders)
        )
    }
}

private const val REMINDER_NOTIFICATION_CHANNEL = "com.kolibree.android.ReminderNotificationChannel"
