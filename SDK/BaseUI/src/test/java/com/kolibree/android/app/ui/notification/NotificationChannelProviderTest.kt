/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.notification

import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.baseui.R
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotificationChannelProviderTest : BaseUnitTest() {

    lateinit var notificationChannelProvider: NotificationChannelProvider

    val applicationContext: ApplicationContext = mock()

    @Before
    fun setUp() {
        notificationChannelProvider = NotificationChannelProvider(applicationContext)
    }

    @Test
    fun `getRemindersChannel returns the right NotificationChannel`() {
        val expectedChannelName = "Reminders"

        whenever(applicationContext.getString(R.string.push_notification_channel_reminders))
            .thenReturn(expectedChannelName)

        val expectedChannel = NotificationChannel(
            "com.kolibree.android.ReminderNotificationChannel",
            expectedChannelName
        )

        assertEquals(expectedChannel, notificationChannelProvider.getRemindersChannel())
    }
}
