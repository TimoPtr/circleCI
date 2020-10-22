/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BrushSyncReminderMonitorTest : BaseUnitTest() {
    private val activeConnectionUseCase: ActiveConnectionUseCase = mock()
    private val brushReminderUseCase: BrushSyncReminderUseCase = mock()

    private val monitor = BrushSyncReminderMonitor(activeConnectionUseCase, brushReminderUseCase)

    @Test
    fun `nothing happens if onApplicationStopped and we never subscribed to useCase`() {
        monitor.onApplicationStopped()
    }

    @Test
    fun `onApplicationStarted subscribes to activeConnectionUseCase`() {
        val connectionActiveSubject = mockActiveConnection()

        monitor.onApplicationStarted()

        assertTrue(connectionActiveSubject.hasSubscribers())
    }

    @Test
    fun `each time activeConnectionUseCase emits an item, we invoke brushReminderUseCase`() {
        val connectionActiveSubject = mockActiveConnection()

        val scheduleRemindersSubject = CompletableSubject.create()
        whenever(brushReminderUseCase.scheduleReminders())
            .thenReturn(scheduleRemindersSubject)

        monitor.onApplicationStarted()

        assertFalse(scheduleRemindersSubject.hasObservers())

        connectionActiveSubject.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("da").build())

        scheduleRemindersSubject.assertHasObserversAndComplete()
    }

    @Test
    fun `each time activeConnectionUseCase emits an item, we dispose previous sync completable`() {
        val connectionActiveSubject = mockActiveConnection()

        val firstScheduleRemindersSubject = CompletableSubject.create()
        val secondScheduleRemindersSubject = CompletableSubject.create()
        whenever(brushReminderUseCase.scheduleReminders())
            .thenReturn(firstScheduleRemindersSubject, secondScheduleRemindersSubject)

        monitor.onApplicationStarted()

        assertFalse(firstScheduleRemindersSubject.hasObservers())

        connectionActiveSubject.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("da").build())

        assertTrue(firstScheduleRemindersSubject.hasObservers())

        connectionActiveSubject.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("dff").build())

        assertFalse(firstScheduleRemindersSubject.hasObservers())
        assertTrue(secondScheduleRemindersSubject.hasObservers())
    }

    @Test
    fun `each time activeConnectionUseCase emits an item, we invoke brushReminderUseCase, even if a previous invocation errored`() {
        val connectionActiveSubject = mockActiveConnection()

        val scheduleRemindersSubject = CompletableSubject.create()
        whenever(brushReminderUseCase.scheduleReminders())
            .thenReturn(Completable.error(TestForcedException()), scheduleRemindersSubject)

        monitor.onApplicationStarted()

        assertFalse(scheduleRemindersSubject.hasObservers())

        connectionActiveSubject.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("da").build())

        assertFalse(scheduleRemindersSubject.hasObservers())

        connectionActiveSubject.onNext(KLTBConnectionBuilder.createAndroidLess().withMac("dff").build())

        assertTrue(scheduleRemindersSubject.hasObservers())
    }

    /*
    Utils
     */

    private fun mockActiveConnection(): PublishProcessor<KLTBConnection> {
        val connectionActiveSubject = PublishProcessor.create<KLTBConnection>()
        whenever(activeConnectionUseCase.onConnectionsUpdatedStream())
            .thenReturn(connectionActiveSubject)
        return connectionActiveSubject
    }
}
