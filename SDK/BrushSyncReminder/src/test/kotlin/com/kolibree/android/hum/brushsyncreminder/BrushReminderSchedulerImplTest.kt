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
import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminder
import com.kolibree.android.clock.TrustedClock
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class BrushReminderSchedulerImplTest : BaseUnitTest() {

    private lateinit var reminderScheduler: BrushReminderSchedulerImpl

    private val alarmManager: AlarmManager = mock()

    private val context: Context = mock()

    private val titleProvider: BrushSyncReminderTitleProvider = mock()

    override fun setup() {
        super.setup()

        reminderScheduler = spy(
            BrushReminderSchedulerImpl(
                alarmManager,
                context,
                titleProvider
            )
        )
    }

    @Test
    fun `scheduleReminders sets alarms`() {
        doNothing().whenever(alarmManager).setExactAndAllowWhileIdle(any(), any(), any())
        doNothing().whenever(reminderScheduler).scheduleReminder(any(), any(), any())
        doNothing().whenever(reminderScheduler).cancelAllReminders()
        val title = "user : title"
        whenever(titleProvider.title(any(), any(), any())).thenReturn(title)

        val nextSunday = TrustedClock.getNowLocalDateTime()
        val reminder =
            BrushSyncReminder(
                profileId = 123L,
                isEnabled = true,
                reminderDate = nextSunday
            )
        reminderScheduler.scheduleReminders(reminder)

        verify(reminderScheduler).scheduleReminder(
            date = nextSunday.plusWeeks(1),
            title = title,
            requestCode = REMINDER_REQUEST_CODE
        )

        verify(reminderScheduler).scheduleReminder(
            date = nextSunday.plusWeeks(2),
            title = title,
            requestCode = REMINDER_REQUEST_CODE + 1
        )

        verify(reminderScheduler).scheduleReminder(
            date = nextSunday.plusWeeks(4),
            title = title,
            requestCode = REMINDER_REQUEST_CODE + 2
        )
    }
}
