/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.receiver

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.notification.NotificationChannel
import com.kolibree.android.app.ui.notification.NotificationChannelProvider
import com.kolibree.android.app.ui.notification.NotificationData
import com.kolibree.android.app.ui.notification.NotificationPresenter
import com.kolibree.android.brushreminder.BrushReminderUseCase
import com.kolibree.android.brushreminder.formatter.BrushReminderTimeFormatter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalTime

class BrushingReminderBroadcastReceiverTest : BaseUnitTest() {

    private lateinit var broadcastReceiver: BrushingReminderBroadcastReceiver

    private val brushingReminderUseCase: BrushReminderUseCase = mock()

    private val notificationPresenter: NotificationPresenter = mock()

    private val brushReminderTimeFormatter: BrushReminderTimeFormatter = mock()

    private val notificationChannelProvider: NotificationChannelProvider = mock()

    override fun setup() {
        super.setup()

        broadcastReceiver = BrushingReminderBroadcastReceiver().apply {
            brushReminderUseCase = brushingReminderUseCase
            presenter = notificationPresenter
            timeFormatter = brushReminderTimeFormatter
            channelProvider = notificationChannelProvider
        }
    }

    @Test
    fun `onReceive should configure the notification according to the time passed in the intent and then configure the next alarm`() {
        val context: Context = mock()
        val applicationInfo: ApplicationInfo = mock()
        val intent: Intent = mock()
        val localTime = LocalTime.of(12, 30)
        val expectedTimeAsText = "12:30"
        val expectedTime = localTime.toSecondOfDay()
        val expectedTitle = "ColgateUnitTest"
        val expectedBody = "Expected Body"
        val expectedChannel = NotificationChannel("abc", "defcon")

        val scheduleNextReminderCompletable = CompletableSubject.create()

        whenever(intent.hasExtra(EXTRA_TIME_INFORMATION_SECONDS))
            .thenReturn(true)

        whenever(intent.getIntExtra(EXTRA_TIME_INFORMATION_SECONDS, -1))
            .thenReturn(expectedTime)

        whenever(context.applicationInfo).thenReturn(applicationInfo)
        whenever(applicationInfo.loadLabel(anyOrNull()))
            .thenReturn(expectedTitle)

        whenever(brushReminderTimeFormatter.format(localTime))
            .thenReturn(expectedTimeAsText)

        whenever(notificationChannelProvider.getRemindersChannel())
            .thenReturn(expectedChannel)

        whenever(context.getString(any(), eq(expectedTimeAsText)))
            .thenReturn(expectedBody)

        whenever(brushingReminderUseCase.scheduleNextReminder())
            .thenReturn(scheduleNextReminderCompletable)

        broadcastReceiver.internalOnReceive(context, intent)

        verify(notificationPresenter).show(
            NotificationData(
                title = expectedTitle,
                body = expectedBody,
                channel = expectedChannel,
                autoCancel = true,
                priority = 2,
                imageUrl = null,
                icon = null
            )
        )

        assertTrue(scheduleNextReminderCompletable.hasObservers())
    }
}

const val EXTRA_TIME_INFORMATION_SECONDS = "EXTRA_TIME_INFORMATION"
