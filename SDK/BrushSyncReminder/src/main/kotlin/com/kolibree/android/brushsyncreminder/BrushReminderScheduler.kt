/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminder
import com.kolibree.android.extensions.toEpochMilli
import com.kolibree.android.hum.brushsyncreminder.ReminderNotificationBroadcastReceiver
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import org.threeten.bp.LocalDateTime

internal interface BrushReminderScheduler {
    fun scheduleReminders(reminder: BrushSyncReminder)
    fun cancelAllReminders()
}

internal class BrushReminderSchedulerImpl @Inject constructor(
    private val alarmManager: AlarmManager,
    private val context: Context,
    private val titleProvider: BrushSyncReminderTitleProvider
) : BrushReminderScheduler {

    private val requestCode = AtomicInteger(REMINDER_REQUEST_CODE)

    /**
     * https://kolibree.atlassian.net/wiki/spaces/PROD/pages/2736001/Notifications
     * According to documentation:
     * Every time a toothbrush is connected to the App,
     * 3 local notifications are scheduled (previously scheduled ones are reset):
     * - 1st one the next Sunday + 1 week
     * - 2nd one the next Sunday + 2 week
     * - 3rd one the next Sunday + 4 week
     */
    override fun scheduleReminders(reminder: BrushSyncReminder) {
        val nextSundayDate = reminder.reminderDate
        val profileId = reminder.profileId
        scheduleReminder(
            date = nextSundayDate.plusWeeks(WEEK_LATER),
            title = titleProvider.title(context, profileId),
            requestCode = nextRequestCode()
        )
        scheduleReminder(
            date = nextSundayDate.plusWeeks(TWO_WEEKS_LATER),
            title = titleProvider.title(context, profileId),
            requestCode = nextRequestCode()
        )
        scheduleReminder(
            date = nextSundayDate.plusWeeks(MONTH_LATER),
            title = titleProvider.title(context, profileId),
            requestCode = nextRequestCode()
        )
    }

    private fun nextRequestCode() = requestCode.getAndIncrement()

    override fun cancelAllReminders() {
        val pendingIntentToCancel = pendingIntent()
        alarmManager.cancel(pendingIntentToCancel)
        pendingIntentToCancel.cancel()
    }

    @VisibleForTesting
    fun scheduleReminder(date: LocalDateTime, requestCode: Int, title: String) {
        val pendingIntent = pendingIntent(requestCode, title)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            date.toEpochMilli(),
            pendingIntent
        )
    }

    @VisibleForTesting
    fun pendingIntent(requestCode: Int = 0, title: String = ""): PendingIntent {
        val intent = Intent(context, ReminderNotificationBroadcastReceiver::class.java)
        intent.putExtra(EXTRA_BODY, title)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

const val EXTRA_BODY = "EXTRA_BODY"
internal const val REMINDER_REQUEST_CODE = 222000
private const val WEEK_LATER = 1L
private const val TWO_WEEKS_LATER = 2L
private const val MONTH_LATER = 4L
