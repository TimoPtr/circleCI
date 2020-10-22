/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminders
import com.kolibree.android.brushreminder.scheduler.BrushingReminderScheduler
import com.kolibree.android.clock.TrustedClock
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

@VisibleForApp
interface BrushReminderUseCase {
    fun fetchBrushingReminders(): Single<BrushingReminders>
    fun updateBrushingReminders(reminders: BrushingReminders): Completable
    fun scheduleNextReminder(): Completable
}

internal class BrushReminderUseCaseImpl @Inject constructor(
    private val repository: BrushReminderRepository,
    private val currentProfileProvider: CurrentProfileProvider,
    private val reminderScheduler: BrushingReminderScheduler
) : BrushReminderUseCase {

    override fun fetchBrushingReminders(): Single<BrushingReminders> {
        return currentProfileProvider.currentProfileSingle()
            .flatMap { profile ->
                repository.brushingReminders(profile.id)
            }
    }

    override fun updateBrushingReminders(reminders: BrushingReminders): Completable {
        return currentProfileProvider.currentProfileSingle()
            .flatMapCompletable { profile ->
                repository.updateBrushingReminders(profile.id, reminders)
            }
            .andThen(scheduleNextReminder())
    }

    override fun scheduleNextReminder(): Completable {
        return repository.allBrushingReminders()
            .mapToAllBrushingReminder()
            .filterDisabledReminders()
            .mapToReminderLocalTime()
            .map { it.sortedBy(LocalTime::toSecondOfDay) }
            .flatMapCompletable { reminders ->
                reminderScheduler.cancelReminder()
                    .andThen(scheduleNextReminder(reminders))
            }
    }

    private fun Single<List<BrushingReminders>>.mapToAllBrushingReminder(): Single<List<BrushingReminder>> {
        return map { reminders ->
            val allReminders = mutableListOf<BrushingReminder>()
            for (reminder in reminders) {
                allReminders += reminder.morningReminder
                allReminders += reminder.afternoonReminder
                allReminders += reminder.eveningReminder
            }
            allReminders
        }
    }

    private fun Single<List<BrushingReminder>>.filterDisabledReminders(): Single<List<BrushingReminder>> {
        return map { reminders ->
            reminders.filter(BrushingReminder::isOn)
        }
    }

    private fun Single<List<BrushingReminder>>.mapToReminderLocalTime(): Single<List<LocalTime>> {
        return map { reminders ->
            reminders.map(BrushingReminder::time)
        }
    }

    private fun scheduleNextReminder(reminderTimes: List<LocalTime>): Completable {
        if (reminderTimes.isEmpty()) {
            Timber.i("No need for to schedule alarms. The list is empty.")
            return Completable.complete()
        }

        return findNextReminder(reminderTimes)
            .flatMapCompletable(reminderScheduler::scheduleReminder)
    }

    private fun findNextReminder(reminderTimes: List<LocalTime>): Single<LocalDateTime> {
        return Single.fromCallable {
            val tomorrowReminder = getTomorrowReminder(reminderTimes)
            val todayReminder = getTodayReminder(reminderTimes)

            todayReminder ?: tomorrowReminder
        }
    }

    private fun getTomorrowReminder(reminderTimes: List<LocalTime>): LocalDateTime {
        return LocalDateTime.of(
            TrustedClock.getNowLocalDate().plusDays(1),
            reminderTimes.first()
        )
    }

    private fun getTodayReminder(reminderTimes: List<LocalTime>): LocalDateTime? {
        val now = TrustedClock.getNowLocalDateTime()
        val today = now.toLocalDate()

        for (reminderTime in reminderTimes) {
            LocalDateTime.of(today, reminderTime)
                .takeIf { it.isAfter(now) }
                ?.let { return it }
        }

        return null
    }
}
