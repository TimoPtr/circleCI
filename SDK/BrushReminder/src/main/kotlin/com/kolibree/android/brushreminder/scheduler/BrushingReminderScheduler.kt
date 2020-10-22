/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.brushreminder.receiver.BrushingReminderBroadcastReceiver
import com.kolibree.android.extensions.toEpochMilli
import io.reactivex.Completable
import javax.inject.Inject
import org.threeten.bp.LocalDateTime
import timber.log.Timber

@VisibleForApp
interface BrushingReminderScheduler {
    fun scheduleReminder(reminderDate: LocalDateTime): Completable
    fun cancelReminder(): Completable
}

internal class BrushingReminderSchedulerImpl @Inject constructor(
    private val alarmManager: AlarmManager,
    private val pendingIntentProvider: PendingIntentProvider
) : BrushingReminderScheduler {

    override fun scheduleReminder(reminderDate: LocalDateTime): Completable {
        return Completable.fromAction {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderDate.toEpochMilli(),
                pendingIntentProvider.getPendingIntent(reminderDate)
            )
        }.doOnComplete { Timber.i("Next alarm will be triggered at : $reminderDate") }
    }

    override fun cancelReminder(): Completable {
        return Completable.fromAction {
            alarmManager.cancel(pendingIntentProvider.getPendingIntent())
        }.doOnComplete {
            Timber.i("Alarm has been canceled")
        }
    }
}

internal class PendingIntentProvider @Inject constructor(
    private val applicationContext: ApplicationContext
) {
    fun getPendingIntent(reminderDate: LocalDateTime? = null): PendingIntent {
        return PendingIntent.getBroadcast(
            applicationContext,
            REQUEST_CODE,
            getIntent(reminderDate),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun getIntent(reminderDate: LocalDateTime? = null): Intent {
        val localTimeInSeconds = reminderDate?.toLocalTime()?.toSecondOfDay()

        return Intent(applicationContext, BrushingReminderBroadcastReceiver::class.java)
            .apply { localTimeInSeconds?.let { putExtra(EXTRA_TIME_INFORMATION_SECONDS, it) } }
    }
}

const val EXTRA_TIME_INFORMATION_SECONDS = "EXTRA_TIME_INFORMATION"
const val REQUEST_CODE = 25200
