/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import androidx.core.app.NotificationManagerCompat
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class SystemNotificationsEnabledUseCaseTest : BaseUnitTest() {

    private lateinit var useCase: SystemNotificationsEnabledUseCase

    private val notificationManagerCompat: NotificationManagerCompat = mock()

    override fun setup() {
        super.setup()

        useCase = SystemNotificationsEnabledUseCaseImpl(notificationManagerCompat)
    }

    @Test
    fun `when system notifications are enabled then useCase returns true`() {
        whenever(notificationManagerCompat.areNotificationsEnabled()).thenReturn(true)

        assertTrue(useCase.areNotificationsEnabled())
    }

    @Test
    fun `when system notifications are disabled then useCase returns false`() {
        whenever(notificationManagerCompat.areNotificationsEnabled()).thenReturn(false)

        assertFalse(useCase.areNotificationsEnabled())
    }
}
