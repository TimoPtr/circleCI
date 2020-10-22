/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.brushreminder.data.BrushReminderDao
import com.kolibree.android.brushreminder.data.BrushReminderEntity
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminders
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@VisibleForApp
interface BrushReminderRepository {
    fun brushingReminders(profileId: Long): Single<BrushingReminders>
    fun updateBrushingReminders(profileId: Long, reminders: BrushingReminders): Completable
    fun allBrushingReminders(): Single<List<BrushingReminders>>
}

internal class BrushReminderRepositoryImpl @Inject constructor(
    private val brushReminderDao: BrushReminderDao
) : BrushReminderRepository {

    override fun brushingReminders(profileId: Long): Single<BrushingReminders> {
        return brushReminderDao.findBy(profileId)
            .toSingle(BrushReminderEntity.new(profileId))
            .map(::toBrushingReminders)
    }

    override fun updateBrushingReminders(
        profileId: Long,
        reminders: BrushingReminders
    ): Completable {
        val entity = toBrushReminderEntity(profileId, reminders)
        return brushReminderDao.insertOrReplace(entity)
    }

    override fun allBrushingReminders(): Single<List<BrushingReminders>> {
        return brushReminderDao.readAll()
            .map { it.map(::toBrushingReminders) }
    }

    private fun toBrushReminderEntity(
        profileId: Long,
        reminders: BrushingReminders
    ): BrushReminderEntity = BrushReminderEntity(
        profileId = profileId,
        isMorningReminderOn = reminders.morningReminder.isOn,
        morningReminderTime = reminders.morningReminder.time,
        isAfternoonReminderOn = reminders.afternoonReminder.isOn,
        afternoonReminderTime = reminders.afternoonReminder.time,
        isEveningReminderOn = reminders.eveningReminder.isOn,
        eveningReminderTime = reminders.eveningReminder.time
    )

    private fun toBrushingReminders(entity: BrushReminderEntity): BrushingReminders =
        BrushingReminders(
            morningReminder = BrushingReminder(
                time = entity.morningReminderTime,
                isOn = entity.isMorningReminderOn
            ),
            afternoonReminder = BrushingReminder(
                time = entity.afternoonReminderTime,
                isOn = entity.isAfternoonReminderOn
            ),
            eveningReminder = BrushingReminder(
                time = entity.eveningReminderTime,
                isOn = entity.isEveningReminderOn
            )
        )
}
