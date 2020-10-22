/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.receiver

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.BaseDaggerBroadcastReceiver
import com.kolibree.android.app.ui.notification.NotificationChannelProvider
import com.kolibree.android.app.ui.notification.NotificationPresenter
import com.kolibree.android.app.ui.notification.notification
import com.kolibree.android.brushreminder.BrushReminderUseCase
import com.kolibree.android.brushreminder.formatter.BrushReminderTimeFormatter
import com.kolibree.android.brushreminder.scheduler.EXTRA_TIME_INFORMATION_SECONDS
import com.kolibree.android.brushsreminder.R
import com.kolibree.android.extensions.appName
import com.kolibree.android.failearly.FailEarly
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.LocalTime
import timber.log.Timber

@VisibleForApp
class BrushingReminderBroadcastReceiver : BaseDaggerBroadcastReceiver() {

    private var nextAlarmDisposable: Disposable? = null

    @VisibleForTesting
    @Inject
    lateinit var presenter: NotificationPresenter

    @VisibleForTesting
    @Inject
    lateinit var brushReminderUseCase: BrushReminderUseCase

    @VisibleForTesting
    @Inject
    lateinit var timeFormatter: BrushReminderTimeFormatter

    @VisibleForTesting
    @Inject
    lateinit var channelProvider: NotificationChannelProvider

    override fun internalOnReceive(context: Context, intent: Intent?) {
        if (intent == null || !intent.hasExtra(EXTRA_TIME_INFORMATION_SECONDS)) {
            return FailEarly.fail("Intent or EXTRA_TIME_INFORMATION_SECONDS should be set.")
        }

        sendNotification(context, intent)
        scheduleNextAlarm()
    }

    private fun sendNotification(context: Context, intent: Intent) {

        val localTimeSeconds = intent.getIntExtra(EXTRA_TIME_INFORMATION_SECONDS, -1)

        notification(presenter) {
            title(context.appName())
            body(
                context.getString(
                    R.string.brushing_reminder_notification_body,
                    timeFormatter.format(LocalTime.ofSecondOfDay(localTimeSeconds.toLong()))
                )
            )
            autoCancel(true)
            channel(channelProvider.getRemindersChannel())
            priorityMax()
        }
    }

    private fun scheduleNextAlarm() {
        nextAlarmDisposable = brushReminderUseCase.scheduleNextReminder()
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    override fun isDaggerReady(): Boolean = ::presenter.isInitialized

    override fun injectSelf(context: Context) {
        AndroidInjection.inject(this, context)
    }
}
