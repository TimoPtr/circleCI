/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.brushsyncreminder

import android.content.Context
import android.content.Intent
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.BaseDaggerBroadcastReceiver
import com.kolibree.android.app.ui.notification.NotificationChannelProvider
import com.kolibree.android.app.ui.notification.NotificationPresenter
import com.kolibree.android.app.ui.notification.notification
import com.kolibree.android.brushsyncreminder.EXTRA_BODY
import com.kolibree.android.extensions.appName
import dagger.android.AndroidInjection
import javax.inject.Inject

@VisibleForApp
class ReminderNotificationBroadcastReceiver : BaseDaggerBroadcastReceiver() {

    @Inject
    lateinit var presenter: NotificationPresenter

    @Inject
    lateinit var channelProvider: NotificationChannelProvider

    override fun internalOnReceive(context: Context, intent: Intent?) {
        val body = checkNotNull(intent?.getStringExtra(EXTRA_BODY)) { "body should not be null" }

        val title = context.appName()

        notification(presenter) {
            title(title)
            body(body)
            autoCancel(true)
            channel(channelProvider.getRemindersChannel())
            priorityMax()
        }
    }

    override fun isDaggerReady(): Boolean = ::presenter.isInitialized

    override fun injectSelf(context: Context) {
        AndroidInjection.inject(this, context)
    }
}
