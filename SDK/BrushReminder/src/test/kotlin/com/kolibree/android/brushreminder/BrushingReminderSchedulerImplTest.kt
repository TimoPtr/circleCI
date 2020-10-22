/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder

import android.app.AlarmManager
import android.app.PendingIntent
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushreminder.scheduler.BrushingReminderScheduler
import com.kolibree.android.brushreminder.scheduler.BrushingReminderSchedulerImpl
import com.kolibree.android.brushreminder.scheduler.PendingIntentProvider
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toEpochMilli
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.threeten.bp.LocalDateTime

class BrushingReminderSchedulerImplTest : BaseUnitTest() {

    lateinit var brushingReminderScheduler: BrushingReminderScheduler

    private val pendingIntentProvider: PendingIntentProvider = mock()
    private val alarmManager: AlarmManager = mock()

    override fun setup() {
        super.setup()

        brushingReminderScheduler =
            BrushingReminderSchedulerImpl(alarmManager, pendingIntentProvider)
    }

    @Test
    fun `scheduleReminder should configure the alarmManager with the right PendingIntent`() {
        val pendingIntent: PendingIntent = mock()
        val localDateTime: LocalDateTime = LocalDateTime.now()
        val expectedMilli = localDateTime.atZone(TrustedClock.systemZone).toEpochMilli()

        whenever(pendingIntentProvider.getPendingIntent(localDateTime))
            .thenReturn(pendingIntent)

        whenever(pendingIntentProvider.getPendingIntent())
            .thenReturn(pendingIntent)

        brushingReminderScheduler.scheduleReminder(localDateTime)
            .test()
            .assertComplete()

        verify(alarmManager).setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, expectedMilli, pendingIntent
        )
    }

    @Test
    fun `cancelReminder should cancel the alarmManager with the right PendingIntent`() {
        val pendingIntent: PendingIntent = mock()

        whenever(pendingIntentProvider.getPendingIntent())
            .thenReturn(pendingIntent)

        brushingReminderScheduler.cancelReminder()
            .test()
            .assertComplete()

        verify(alarmManager).cancel(pendingIntent)
    }
}
