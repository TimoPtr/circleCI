/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.scheduler

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class CancelReminderLogoutTest : BaseUnitTest() {

    private lateinit var cancelReminderLogout: CancelReminderLogout

    private val brushingReminderScheduler: BrushingReminderScheduler = mock()

    @Before
    fun setUp() {
        cancelReminderLogout = CancelReminderLogout(brushingReminderScheduler)
    }

    @Test
    fun `getLogoutHookCompletable cancel the brushing reminder`() {
        cancelReminderLogout.getLogoutHookCompletable()
            .test()
            .assertComplete()

        verify(brushingReminderScheduler).cancelReminder()
    }
}
