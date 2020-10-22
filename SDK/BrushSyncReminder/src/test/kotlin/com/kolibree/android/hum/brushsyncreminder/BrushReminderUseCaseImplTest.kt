/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminder
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

internal class BrushReminderUseCaseImplTest : BaseUnitTest() {

    lateinit var useCase: BrushSyncReminderUseCaseImpl

    private val brushingsRepository: BrushingsRepository = mock()

    private val reminderScheduler: BrushReminderScheduler = mock()

    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val componentToggle: BrushSyncReminderComponentsToggle = mock()

    private val reminderRepository: BrushSyncReminderRepository = mock()

    private val defaultProfile = ProfileBuilder.create().withId(123L).build()

    override fun setup() {
        super.setup()

        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.just(defaultProfile))

        useCase = BrushSyncReminderUseCaseImpl(
            brushingsRepository = brushingsRepository,
            reminderScheduler = reminderScheduler,
            componentToggle = componentToggle,
            currentProfileProvider = currentProfileProvider,
            reminderRepository = reminderRepository
        )
    }

    @Test
    fun `isCurrentProfileReminderOn returns true if reminder is on`() {
        val profileId = 123L
        val profile = ProfileBuilder.create()
            .withId(profileId)
            .build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(profile))
        whenever(reminderRepository.isSyncReminderEnabled(profileId))
            .thenReturn(Single.just(true))

        useCase.isCurrentProfileReminderOn().test().assertValue(true)
    }

    @Test
    fun `isCurrentProfileReminderOn returns false if reminder is off`() {
        val profileId = 123L
        val profile = ProfileBuilder.create()
            .withId(profileId)
            .build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(profile))
        whenever(reminderRepository.isSyncReminderEnabled(profileId))
            .thenReturn(Single.just(false))

        useCase.isCurrentProfileReminderOn().test().assertValue(false)
    }

    @Test
    fun `onlyAfternoonBrushings returns only afternoon brushings`() {
        val brushings = listOf(
            brushing(createDate(1, 0)), // afternoon
            brushing(createDate(2, 0)), // afternoon
            brushing(createDate(3, 0)), // afternoon
            brushing(createDate(4, 0)), // afternoon
            brushing(createDate(5, 0)),
            brushing(createDate(6, 0)),
            brushing(createDate(7, 0)),
            brushing(createDate(8, 0)),
            brushing(createDate(9, 0)),
            brushing(createDate(10, 0)),
            brushing(createDate(11, 0)),
            brushing(createDate(12, 0)),
            brushing(createDate(13, 0)),
            brushing(createDate(14, 0)),
            brushing(createDate(15, 0)),
            brushing(createDate(16, 0)), // afternoon
            brushing(createDate(17, 0)), // afternoon
            brushing(createDate(18, 0)), // afternoon
            brushing(createDate(19, 0)), // afternoon
            brushing(createDate(20, 0)), // afternoon
            brushing(createDate(21, 0)), // afternoon
            brushing(createDate(22, 0)), // afternoon
            brushing(createDate(23, 0)) // afternoon
        )

        val onlyAfternoon = useCase.onlyAfternoonBrushings(brushings)
        assertTrue(onlyAfternoon.size == 12)
        onlyAfternoon.contains(brushing(createDate(1, 0)))
        onlyAfternoon.contains(brushing(createDate(2, 0)))
        onlyAfternoon.contains(brushing(createDate(3, 0)))
        onlyAfternoon.contains(brushing(createDate(4, 0)))
        onlyAfternoon.contains(brushing(createDate(16, 0)))
        onlyAfternoon.contains(brushing(createDate(17, 0)))
        onlyAfternoon.contains(brushing(createDate(18, 0)))
        onlyAfternoon.contains(brushing(createDate(19, 0)))
        onlyAfternoon.contains(brushing(createDate(20, 0)))
        onlyAfternoon.contains(brushing(createDate(21, 0)))
        onlyAfternoon.contains(brushing(createDate(22, 0)))
        onlyAfternoon.contains(brushing(createDate(23, 0)))
    }

    private fun brushing(createdAt: OffsetDateTime) = Brushing(
        100,
        100,
        createdAt,
        0,
        0,
        null,
        123L,
        null,
        null,
        "mac"
    )

    @Test
    fun `isAfternoon returns true if time between 4PM and 4AM`() {
        assertTrue(useCase.isAfternoon(createDate(16, 0)))
        assertTrue(useCase.isAfternoon(createDate(16, 1)))
        assertTrue(useCase.isAfternoon(createDate(23, 59)))
        assertTrue(useCase.isAfternoon(createDate(3, 59)))
        assertTrue(useCase.isAfternoon(createDate(4, 0)))

        assertFalse(useCase.isAfternoon(createDate(4, 1)))
        assertFalse(useCase.isAfternoon(createDate(15, 59)))
        assertFalse(useCase.isAfternoon(createDate(12, 0)))
    }

    @Test
    fun `nextSunday returns next Sunday`() {
        val day1 = LocalDate.of(2020, 7, 14)
        assertEquals(LocalDate.of(2020, 7, 19), useCase.nextSunday(day1))

        val day2 = LocalDate.of(2020, 7, 19)
        assertEquals(LocalDate.of(2020, 7, 19), useCase.nextSunday(day2))

        val day3 = LocalDate.of(2020, 7, 27)
        assertEquals(LocalDate.of(2020, 8, 2), useCase.nextSunday(day3))

        val day4 = LocalDate.of(2020, 7, 4)
        assertEquals(LocalDate.of(2020, 7, 5), useCase.nextSunday(day4))
    }

    private fun createDate(hour: Int, minutes: Int): OffsetDateTime {
        return OffsetDateTime.of(LocalDate.now(), LocalTime.of(hour, minutes), ZoneOffset.UTC)
    }

    private fun createDate(
        localDateTime: LocalDateTime
    ): OffsetDateTime {
        return OffsetDateTime.of(localDateTime, ZoneOffset.UTC)
    }

    @Test
    fun `averageBrushingTimeOfDay returns average brushing time of day`() {
        val brushings = listOf(
            brushing(createDate(10, 0)),
            brushing(createDate(11, 0)),
            brushing(createDate(12, 0)),
            brushing(createDate(13, 0)),
            brushing(createDate(14, 0)),
            brushing(createDate(15, 0))
        )
        val expectedTimeOfDay = LocalTime.of(12, 30)
        assertEquals(expectedTimeOfDay, useCase.averageBrushingTimeOfDay(brushings))
    }

    @Test
    fun `setCurrentProfileReminder makes changes in database`() {
        whenever(reminderRepository.enabledReminders())
            .thenReturn(Single.just(emptyList()))
        whenever(reminderRepository.hasEnabledReminder())
            .thenReturn(Single.just(true))
        whenever(reminderRepository.setSyncReminder(defaultProfile.id, true))
            .thenReturn(Completable.complete())
        whenever(brushingsRepository.getBrushings(defaultProfile.id))
            .thenReturn(Single.just(emptyList()))
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(defaultProfile))

        useCase.setCurrentProfileReminder(true)
            .test()
            .assertComplete()

        verify(reminderRepository).setSyncReminder(defaultProfile.id, true)
    }

    @Test
    fun `setCurrentProfileReminder changes component`() {
        whenever(reminderRepository.enabledReminders())
            .thenReturn(Single.just(emptyList()))
        whenever(reminderRepository.hasEnabledReminder())
            .thenReturn(Single.just(false))
        whenever(reminderRepository.setSyncReminder(defaultProfile.id, false))
            .thenReturn(Completable.complete())
        whenever(brushingsRepository.getBrushings(defaultProfile.id))
            .thenReturn(Single.just(emptyList()))
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(defaultProfile))

        useCase.setCurrentProfileReminder(false)
            .test()
            .assertComplete()

        verify(componentToggle).setComponents(false)
    }

    @Test
    fun `scheduleReminders cancels all pending reminders when reminder is off`() {
        whenever(reminderRepository.enabledReminders())
            .thenReturn(Single.just(emptyList()))

        useCase.scheduleReminders()
            .test()
            .assertComplete()

        verify(reminderScheduler).cancelAllReminders()
    }

    @Test
    fun `scheduleReminders does nothing reminder is off`() {
        whenever(reminderRepository.enabledReminders())
            .thenReturn(Single.just(emptyList()))

        useCase.scheduleReminders()
            .test()
            .assertComplete()

        verify(reminderScheduler).cancelAllReminders()
    }

    @Test
    fun `scheduleReminders does nothing if less then 5 afternoons brushings`() {
        val reminder =
            BrushSyncReminder(
                profileId = 123L,
                reminderDate = TrustedClock.getNowLocalDateTime(),
                isEnabled = true
            )
        whenever(reminderRepository.enabledReminders())
            .thenReturn(Single.just(listOf(reminder)))
        whenever(reminderRepository.updateReminderDate(123L, LocalDateTime.MIN))
            .thenReturn(Completable.complete())

        val brushings = listOf(
            brushing(createDate(12, 12)),
            brushing(createDate(13, 13)),
            brushing(createDate(14, 14))
        )
        whenever(brushingsRepository.getBrushings(defaultProfile.id))
            .thenReturn(Single.just(brushings))

        useCase.scheduleReminders()
            .test()
            .assertComplete()

        verify(reminderRepository).updateReminderDate(123L, LocalDateTime.MIN)
    }

    @Test
    fun `scheduleReminders schedules reminders if more than 5 afternoons brushings`() {
        val reminder =
            BrushSyncReminder(
                profileId = 123L,
                reminderDate = TrustedClock.getNowLocalDateTime(),
                isEnabled = true
            )

        whenever(reminderRepository.enabledReminders())
            .thenReturn(Single.just(listOf(reminder)))

        val brushings = listOf(
            brushing(createDate(LocalDateTime.of(2020, Month.JULY, 13, 17, 0))),
            brushing(createDate(LocalDateTime.of(2020, Month.JULY, 13, 17, 30))),
            brushing(createDate(LocalDateTime.of(2020, Month.JULY, 14, 18, 0))),
            brushing(createDate(LocalDateTime.of(2020, Month.JULY, 14, 18, 30))),
            brushing(createDate(LocalDateTime.of(2020, Month.JULY, 15, 19, 0)))
        )
        val today = createDate(LocalDateTime.of(2020, Month.JULY, 16, 8, 15))
        TrustedClock.setFixedDate(today)

        whenever(brushingsRepository.getBrushings(defaultProfile.id))
            .thenReturn(Single.just(brushings))

        val expectedReminderDate = createDate(LocalDateTime.of(2020, Month.JULY, 19, 18, 0))

        whenever(reminderRepository.updateReminderDate(123L, expectedReminderDate.toLocalDateTime()))
            .thenReturn(Completable.complete())

        useCase.scheduleReminders()
            .test()
            .assertComplete()

        verify(reminderRepository).updateReminderDate(123L, expectedReminderDate.toLocalDateTime())
    }
}
