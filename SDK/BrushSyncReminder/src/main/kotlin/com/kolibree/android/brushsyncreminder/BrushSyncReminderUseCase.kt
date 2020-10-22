/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.TemporalAdjusters

@VisibleForApp
interface BrushSyncReminderUseCase {
    fun scheduleReminders(): Completable
    fun isCurrentProfileReminderOn(): Single<Boolean>
    fun setCurrentProfileReminder(isOn: Boolean): Completable
    fun restoreReminders(): Completable
}

internal class BrushSyncReminderUseCaseImpl @Inject constructor(
    private val brushingsRepository: BrushingsRepository,
    private val reminderScheduler: BrushReminderScheduler,
    private val currentProfileProvider: CurrentProfileProvider,
    private val componentToggle: BrushSyncReminderComponentsToggle,
    private val reminderRepository: BrushSyncReminderRepository
) : BrushSyncReminderUseCase {

    override fun scheduleReminders(): Completable = calculateRemindersDate()
        .andThen(restoreReminders())

    @Suppress("SpreadOperator")
    private fun calculateRemindersDate(): Completable = reminderRepository.enabledReminders()
        .flatMapCompletable { enabledReminders ->
            val completableToMerge = enabledReminders.map {
                calculateAndUpdateReminderDate(it.profileId)
            }.toTypedArray()
            Completable.mergeArray(*completableToMerge)
        }

    override fun isCurrentProfileReminderOn(): Single<Boolean> =
        currentProfileProvider.currentProfileSingle()
            .flatMap {
                reminderRepository.isSyncReminderEnabled(it.id)
            }

    override fun setCurrentProfileReminder(isOn: Boolean): Completable {
        return currentProfileProvider.currentProfileSingle()
            .flatMapCompletable { profile ->
                reminderRepository.setSyncReminder(profile.id, isOn)
            }
            .andThen(toggleComponents())
            .andThen(scheduleReminders())
    }

    private fun toggleComponents() = reminderRepository.hasEnabledReminder()
        .map { isEnabled ->
            componentToggle.setComponents(isEnabled)
        }.ignoreElement()

    override fun restoreReminders(): Completable = reminderRepository.enabledReminders()
        .map { enabledReminders ->
            reminderScheduler.cancelAllReminders()
            enabledReminders.filter {
                it.isValid()
            }.forEach(reminderScheduler::scheduleReminders)
        }.ignoreElement()

    // https://kolibree.atlassian.net/browse/KLTB002-11694?focusedCommentId=23583
    // Preconditions:
    // - The notification time is the average time of the recent "afternoon"  brushing sessions
    // (last 5 brushings performed between 4:00 PM and 4:00 AM)
    //
    // - Then the notification should only be set after the user has at least 5 brushings.
    // Before that we save the switcher “on” and wait
    private fun calculateAndUpdateReminderDate(profileId: Long): Completable {
        return brushingsRepository.getBrushings(profileId)
            .map(::onlyAfternoonBrushings)
            .map(::calculateReminderDate)
            .flatMapCompletable { reminderDate ->
                reminderRepository.updateReminderDate(profileId, reminderDate)
            }
    }

    private fun calculateReminderDate(afternoonBrushings: List<Brushing>): LocalDateTime {
        if (afternoonBrushings.size >= MIN_AFTERNOON_BRUSHINGS) {
            val brushingTimeOfDay = averageBrushingTimeOfDay(afternoonBrushings)
            return nextSunday().atTime(brushingTimeOfDay)
        }
        return LocalDateTime.MIN
    }

    @VisibleForTesting
    fun onlyAfternoonBrushings(brushings: List<Brushing>): List<Brushing> = brushings.filter {
        isAfternoon(it.dateTime)
    }

    @VisibleForTesting
    fun averageBrushingTimeOfDay(brushings: List<Brushing>): LocalTime {
        val averageBrushingLocalTimeInSeconds = brushings.map {
            it.dateTime.toLocalTime().toSecondOfDay()
        }.average()
        return LocalTime.ofSecondOfDay(averageBrushingLocalTimeInSeconds.toLong())
    }

    @VisibleForTesting
    fun isAfternoon(dateTime: OffsetDateTime): Boolean {
        val localTime = dateTime.toLocalTime()
        val firstHour = LocalTime.of(AFTERNOON_FIRS_HOUR, 0)
        val lastHour = LocalTime.of(AFTERNOON_LAST_HOUR, 0)
        return !localTime.isBefore(firstHour) || !localTime.isAfter(lastHour)
    }

    @VisibleForTesting
    fun nextSunday(today: LocalDate = TrustedClock.getNowLocalDate()): LocalDate {
        return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }
}

private const val MIN_AFTERNOON_BRUSHINGS = 5
private const val AFTERNOON_FIRS_HOUR = 16
private const val AFTERNOON_LAST_HOUR = 4
