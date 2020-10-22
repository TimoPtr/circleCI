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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminder.Companion.defaultAfternoon
import com.kolibree.android.brushreminder.model.BrushingReminder.Companion.defaultEvening
import com.kolibree.android.brushreminder.model.BrushingReminder.Companion.defaultMorning
import com.kolibree.android.brushreminder.model.BrushingReminders
import com.kolibree.android.brushreminder.scheduler.BrushingReminderScheduler
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime

internal class BrushReminderUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: BrushReminderUseCaseImpl

    private val repository: BrushReminderRepository = mock()

    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val reminderScheduler: BrushingReminderScheduler = mock()

    override fun setup() {
        super.setup()

        useCase = BrushReminderUseCaseImpl(repository, currentProfileProvider, reminderScheduler)
    }

    @Test
    fun `fetchBrushingReminders returns current profile reminders`() {
        val profileId = 897L
        val profile = ProfileBuilder.create()
            .withId(profileId)
            .build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(profile))
        val result = BrushingReminders(
            morningReminder = defaultMorning(),
            afternoonReminder = BrushingReminder(LocalTime.now(), true),
            eveningReminder = defaultEvening()
        )
        whenever(repository.brushingReminders(profileId))
            .thenReturn(Single.just(result))

        useCase.fetchBrushingReminders()
            .test()
            .assertValue(result)
    }

    @Test
    fun `scheduleNextReminder should cancel and schedule the next reminder available`() {

        setFixedDate("2020-01-25T06:30:00+01:00")

        val morningReminder = LocalDateTime.parse("2020-01-25T07:30")
        val morningLocalTime = morningReminder.toLocalTime()
        val eveningLocalTime = LocalTime.of(20, 30)

        whenever(repository.allBrushingReminders()).thenReturn(
            Single.just(
                listOf(
                    BrushingReminders(
                        morningReminder = BrushingReminder(morningLocalTime, true),
                        afternoonReminder = defaultAfternoon().copy(isOn = false),
                        eveningReminder = BrushingReminder(eveningLocalTime, true)
                    )
                )
            )
        )

        whenever(reminderScheduler.scheduleReminder(any()))
            .thenReturn(Completable.complete())

        whenever(reminderScheduler.cancelReminder())
            .thenReturn(Completable.complete())

        useCase.scheduleNextReminder()
            .test()
            .assertComplete()

        inOrder(reminderScheduler) {
            this.verify(reminderScheduler).cancelReminder()
            this.verify(reminderScheduler).scheduleReminder(morningReminder)
        }
    }

    @Test
    fun `scheduleNextReminder should cancel and schedule the next reminder available tomorrow`() {
        val morningReminder = LocalDateTime.parse("2020-01-26T07:30")

        val morningLocalTime = morningReminder.toLocalTime()
        val afternoonLocalTime = LocalTime.of(13, 30)
        val eveningLocalTime = LocalTime.of(20, 30)

        setFixedDate("2020-01-25T20:31:00+01:00")

        whenever(repository.allBrushingReminders()).thenReturn(
            Single.just(
                listOf(
                    BrushingReminders(
                        morningReminder = BrushingReminder(morningLocalTime, true),
                        afternoonReminder = BrushingReminder(afternoonLocalTime, true),
                        eveningReminder = BrushingReminder(eveningLocalTime, true)
                    )
                )
            )
        )

        whenever(reminderScheduler.scheduleReminder(any()))
            .thenReturn(Completable.complete())

        whenever(reminderScheduler.cancelReminder())
            .thenReturn(Completable.complete())

        useCase.scheduleNextReminder()
            .test()
            .assertComplete()

        inOrder(reminderScheduler) {
            this.verify(reminderScheduler).cancelReminder()
            this.verify(reminderScheduler).scheduleReminder(morningReminder)
        }
    }

    @Test
    fun `scheduleNextReminder should cancel and not schedule any reminders if they are all turned off`() {
        whenever(repository.allBrushingReminders()).thenReturn(
            Single.just(
                listOf(
                    BrushingReminders(
                        morningReminder = defaultMorning(),
                        afternoonReminder = defaultAfternoon(),
                        eveningReminder = defaultEvening()
                    )
                )
            )
        )

        whenever(reminderScheduler.scheduleReminder(any()))
            .thenReturn(Completable.complete())

        whenever(reminderScheduler.cancelReminder())
            .thenReturn(Completable.complete())

        useCase.scheduleNextReminder()
            .test()
            .assertComplete()

        verify(reminderScheduler).cancelReminder()
        verifyNoMoreInteractions(reminderScheduler)
    }

    private fun setFixedDate(offsetDateTimeText: String) {
        TrustedClock.setFixedDate(
            OffsetDateTime.parse(offsetDateTimeText)
        )
    }

    @Test
    fun `updateBrushingReminders updates current profile reminders`() {
        val profileId = 997L
        val profile = ProfileBuilder.create()
            .withId(profileId)
            .build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(profile))

        val currentReminders = BrushingReminders(
            morningReminder = defaultMorning(),
            afternoonReminder = BrushingReminder(LocalTime.of(13, 30), true),
            eveningReminder = BrushingReminder(LocalTime.of(20, 40), true)
        )
        whenever(repository.updateBrushingReminders(profileId, currentReminders))
            .thenReturn(Completable.complete())

        whenever(repository.allBrushingReminders())
            .thenReturn(Single.just(emptyList()))

        whenever(reminderScheduler.cancelReminder())
            .thenReturn(Completable.complete())

        useCase.updateBrushingReminders(currentReminders).test()
            .assertComplete()
    }
}
