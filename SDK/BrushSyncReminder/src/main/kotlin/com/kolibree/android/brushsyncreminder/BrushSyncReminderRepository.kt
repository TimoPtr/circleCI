/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import com.kolibree.android.brushsyncreminder.data.BrushSyncReminder
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderDao
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderEntity
import com.kolibree.android.brushsyncreminder.data.toSynchronizableItem
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronizator.Synchronizator
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.LocalDateTime

internal interface BrushSyncReminderRepository {
    fun setSyncReminder(profileId: Long, isEnabled: Boolean): Completable
    fun isSyncReminderEnabled(profileId: Long): Single<Boolean>
    fun hasEnabledReminder(): Single<Boolean>
    fun enabledReminders(): Single<List<BrushSyncReminder>>
    fun updateReminderDate(profileId: Long, reminderDate: LocalDateTime): Completable
}

internal class BrushSyncReminderRepositoryImpl @Inject constructor(
    private val dao: BrushSyncReminderDao,
    private val synchronizator: Synchronizator
) : BrushSyncReminderRepository {

    private fun update(entity: BrushSyncReminderEntity): Completable {
        return Single
            .defer {
                val updatedEntity = entity.updatedNow()
                val syncItem = updatedEntity.toSynchronizableItem()

                when {
                    syncItem.uuid != null -> synchronizator.update(syncItem)
                    else -> synchronizator.create(syncItem)
                }
            }
            .ignoreElement()
            .andThen(synchronizator.delaySynchronizeCompletable())
    }

    override fun setSyncReminder(profileId: Long, isEnabled: Boolean): Completable {
        return findReminder(profileId)
            .flatMapCompletable { reminder ->
                update(reminder.copy(isEnabled = isEnabled))
            }
    }

    override fun isSyncReminderEnabled(profileId: Long): Single<Boolean> {
        return findReminder(profileId).map { it.isEnabled }
    }

    override fun hasEnabledReminder(): Single<Boolean> {
        return dao.readAll()
            .map { reminders ->
                reminders.any { it.isEnabled }
            }
    }

    override fun enabledReminders(): Single<List<BrushSyncReminder>> {
        return dao.enabledReminders()
            .map { reminders ->
                reminders.map {
                    BrushSyncReminder(
                        it.profileId,
                        it.isEnabled,
                        it.reminderDate.toLocalDateTime()
                    )
                }
            }
    }

    override fun updateReminderDate(profileId: Long, reminderDate: LocalDateTime): Completable {
        return findReminder(profileId)
            .flatMapCompletable { entity ->
                val reminder = entity.copy(
                    reminderDateTimestamp = reminderDate.toEpochSecond(TrustedClock.systemZoneOffset),
                    reminderDateZoneOffset = TrustedClock.systemZoneOffset
                )
                update(reminder)
            }
    }

    private fun findReminder(profileId: Long): Single<BrushSyncReminderEntity> {
        return dao
            .findBy(profileId)
            .toSingle(BrushSyncReminderEntity.new(profileId))
    }
}
