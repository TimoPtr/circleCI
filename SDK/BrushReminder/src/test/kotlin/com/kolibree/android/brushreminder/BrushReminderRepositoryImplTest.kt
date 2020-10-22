/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushreminder.data.BrushReminderDao
import com.kolibree.android.brushreminder.data.BrushReminderEntity
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminders
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.Test
import org.threeten.bp.LocalTime

internal class BrushReminderRepositoryImplTest : BaseUnitTest() {

    private lateinit var repository: BrushReminderRepositoryImpl

    private val brushReminderDao: BrushReminderDao = mock()

    override fun setup() {
        super.setup()

        repository = BrushReminderRepositoryImpl(brushReminderDao)
    }

    @Test
    fun `brushingReminders finds an appropriate entity in database`() {
        val profileId = 345L
        whenever(brushReminderDao.findBy(profileId))
            .thenReturn(Maybe.empty())

        repository.brushingReminders(profileId).test()

        verify(brushReminderDao).findBy(profileId)
    }

    @Test
    fun `brushingReminders returns default entity if not present in database`() {
        val profileId = 404L
        whenever(brushReminderDao.findBy(profileId))
            .thenReturn(Maybe.empty())

        val expectedResult = BrushingReminders(
            morningReminder = BrushingReminder.defaultMorning(),
            afternoonReminder = BrushingReminder.defaultAfternoon(),
            eveningReminder = BrushingReminder.defaultEvening()
        )
        repository.brushingReminders(profileId)
            .test()
            .assertValue(expectedResult)
    }

    @Test
    fun `brushingReminders maps entity to BrushingReminders model`() {
        val profileId = 200L
        val entity = BrushReminderEntity(
            profileId = profileId,
            isMorningReminderOn = false,
            morningReminderTime = LocalTime.of(8, 0),
            isAfternoonReminderOn = true,
            afternoonReminderTime = LocalTime.of(16, 46),
            isEveningReminderOn = true,
            eveningReminderTime = LocalTime.of(21, 30)
        )
        whenever(brushReminderDao.findBy(profileId))
            .thenReturn(Maybe.just(entity))

        val expectedResult = BrushingReminders(
            morningReminder = BrushingReminder(
                isOn = false,
                time = LocalTime.of(8, 0)
            ),
            afternoonReminder = BrushingReminder(
                isOn = true,
                time = LocalTime.of(16, 46)
            ),
            eveningReminder = BrushingReminder(
                isOn = true,
                time = LocalTime.of(21, 30)
            )
        )
        repository.brushingReminders(profileId)
            .test()
            .assertValue(expectedResult)
    }

    @Test
    fun `updateBrushingReminders insert entity to database`() {
        val profileId = 598L
        whenever(brushReminderDao.insertOrReplace(any()))
            .thenReturn(Completable.complete())

        val currentReminders = BrushingReminders(
            morningReminder = BrushingReminder(
                isOn = true,
                time = LocalTime.of(9, 0)
            ),
            afternoonReminder = BrushingReminder(
                isOn = true,
                time = LocalTime.of(17, 0)
            ),
            eveningReminder = BrushingReminder(
                isOn = true,
                time = LocalTime.of(22, 0)
            )
        )
        repository.updateBrushingReminders(profileId, currentReminders)
            .test()
            .assertComplete()

        val expectedEntity = BrushReminderEntity(
            profileId = profileId,
            isMorningReminderOn = true,
            morningReminderTime = LocalTime.of(9, 0),
            isAfternoonReminderOn = true,
            afternoonReminderTime = LocalTime.of(17, 0),
            isEveningReminderOn = true,
            eveningReminderTime = LocalTime.of(22, 0)
        )
        verify(brushReminderDao).insertOrReplace(expectedEntity)
    }
}
