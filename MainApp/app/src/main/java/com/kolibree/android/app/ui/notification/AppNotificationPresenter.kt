/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import com.kolibree.R
import com.kolibree.android.app.notification.DefaultNotificationChannel
import com.kolibree.android.app.notification.showNotification
import dagger.Binds
import dagger.Module
import javax.inject.Inject

internal class AppNotificationPresenter @Inject constructor(
    context: Context
) : NotificationPresenter {

    private val appContext: Context = context.applicationContext

    override fun show(notification: NotificationData) {
        showNotification(
            context = appContext,
            title = notification.title.orEmpty(),
            body = notification.body.orEmpty(),
            priority = notification.priority ?: NotificationCompat.PRIORITY_DEFAULT,
            autoCancel = notification.autoCancel ?: false,
            notificationChannel = channel(notification),
            imageUrl = notification.imageUrl,
            data = emptyMap(),
            icon = notification.icon ?: R.drawable.push_notification_icon
        )
    }

    private fun channel(notification: NotificationData): NotificationChannel {
        return notification.channel ?: DefaultNotificationChannel.get(appContext)
    }
}

@Module
abstract class NotificationPresenterModule {

    @Binds
    internal abstract fun bindsPresenter(
        implementation: AppNotificationPresenter
    ): NotificationPresenter
}
