/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.notification

import android.content.Context
import com.kolibree.R
import com.kolibree.android.app.ui.notification.NotificationChannel

object DefaultNotificationChannel {

    fun get(context: Context) = NotificationChannel(
        id = context.getString(R.string.default_notification_channel),
        name = context.getString(R.string.default_notification_channel_name)
    )
}
