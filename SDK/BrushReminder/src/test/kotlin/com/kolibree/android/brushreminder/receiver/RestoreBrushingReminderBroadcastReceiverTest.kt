/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.receiver

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushreminder.BrushReminderUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreBrushingReminderBroadcastReceiverTest : BaseUnitTest() {

    private lateinit var broadcastReceiver: RestoreBrushingReminderBroadcastReceiver

    private val brushingReminderUseCase: BrushReminderUseCase = mock()

    override fun setup() {
        super.setup()

        broadcastReceiver = RestoreBrushingReminderBroadcastReceiver().apply {
            brushReminderUseCase = brushingReminderUseCase
        }
    }

    @Test
    fun `onReceive should configure the next alarm`() {
        val scheduleNextReminderCompletable = CompletableSubject.create()

        whenever(brushingReminderUseCase.scheduleNextReminder())
            .thenReturn(scheduleNextReminderCompletable)

        broadcastReceiver.internalOnReceive(mock(), mock())

        assertTrue(scheduleNextReminderCompletable.hasObservers())
    }
}
