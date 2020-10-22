/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.account.AccountFacade
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushreminder.BrushReminderUseCase
import com.kolibree.android.brushreminder.formatter.BrushReminderTimeFormatter
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminderType
import com.kolibree.android.brushreminder.model.BrushingReminderType.AFTERNOON
import com.kolibree.android.brushreminder.model.BrushingReminderType.EVENING
import com.kolibree.android.brushreminder.model.BrushingReminderType.MORNING
import com.kolibree.android.brushreminder.model.BrushingReminders
import com.kolibree.android.brushsyncreminder.BrushSyncReminderUseCase
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalTime

internal class NotificationsViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: NotificationsViewModel

    private val accountFacade: AccountFacade = mock()

    private val brushSyncReminderUseCase: BrushSyncReminderUseCase = mock()
    private val systemNotificationsEnabledUseCase: SystemNotificationsEnabledUseCase = mock()

    private val notificationsNavigator: NotificationsNavigator = mock()

    private val brushReminderUseCase: BrushReminderUseCase = mock()

    private val brushReminderTimeFormatter: BrushReminderTimeFormatter = mock()

    override fun setup() {
        super.setup()

        viewModel = NotificationsViewModel(
            initialViewState = NotificationsViewState.initial(),
            accountFacade = accountFacade,
            brushSyncReminderUseCase = brushSyncReminderUseCase,
            systemNotificationsEnabledUseCase = systemNotificationsEnabledUseCase,
            notificationsNavigator = notificationsNavigator,
            brushReminderUseCase = brushReminderUseCase,
            timeFormatter = brushReminderTimeFormatter
        )
    }

    @Test
    fun `fetch newsletter subscription on start`() {
        whenever(accountFacade.isEmailNewsletterSubscriptionEnabled())
            .thenReturn(Single.just(true))
        whenever(brushSyncReminderUseCase.isCurrentProfileReminderOn())
            .thenReturn(Single.never())
        whenever(brushReminderUseCase.fetchBrushingReminders())
            .thenReturn(Single.never())

        assertTrue(viewModel.getViewState()?.isNewsletterSubscriptionOn == false)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertTrue(viewModel.getViewState()?.isNewsletterSubscriptionOn == true)
        verify(accountFacade).isEmailNewsletterSubscriptionEnabled()
    }

    @Test
    fun `when subscribe newsletter is on then send ON event`() {
        val accountId = 1230L
        whenever(accountFacade.accountId).thenReturn(accountId)
        whenever(accountFacade.emailNewsletterSubscription(accountId, true))
            .thenReturn(Completable.complete())

        viewModel.updateViewState { copy(isNewsletterSubscriptionOn = false) }

        viewModel.onSubscribeNewsletterClick(isOn = true)

        verify(eventTracker).sendEvent(AnalyticsEvent("Notification_JoinMailing_On"))
    }

    @Test
    fun `when subscribe newsletter is off then send OFF event`() {
        val accountId = 1230L
        whenever(accountFacade.accountId).thenReturn(accountId)
        whenever(accountFacade.emailNewsletterSubscription(accountId, false))
            .thenReturn(Completable.complete())

        viewModel.updateViewState { copy(isNewsletterSubscriptionOn = true) }

        viewModel.onSubscribeNewsletterClick(isOn = false)

        verify(eventTracker).sendEvent(AnalyticsEvent("Notification_JoinMailing_Off"))
    }

    @Test
    fun `when subscribe newsletter is clicked then update value`() {
        val accountId = 1230L
        whenever(accountFacade.accountId).thenReturn(accountId)
        whenever(accountFacade.emailNewsletterSubscription(accountId, true))
            .thenReturn(Completable.complete())

        viewModel.onSubscribeNewsletterClick(isOn = true)

        assertTrue(viewModel.getViewState()?.isNewsletterSubscriptionOn == true)
        verify(accountFacade).emailNewsletterSubscription(accountId, true)
    }

    @Test
    fun `when brush sync reminder is on then send ON event`() {
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(true)
        whenever(brushSyncReminderUseCase.setCurrentProfileReminder(true))
            .thenReturn(Completable.never())
        viewModel.updateViewState { copy(isBrushingSyncReminderOn = false) }

        viewModel.onBrushingReminderClick(isOn = true)

        verify(eventTracker).sendEvent(AnalyticsEvent("Notification_SyncReminder_On"))
    }

    @Test
    fun `when brush sync reminder is off then send OFF event`() {
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(true)
        whenever(brushSyncReminderUseCase.setCurrentProfileReminder(false))
            .thenReturn(Completable.never())
        viewModel.updateViewState { copy(isBrushingSyncReminderOn = true) }

        viewModel.onBrushingReminderClick(isOn = false)

        verify(eventTracker).sendEvent(AnalyticsEvent("Notification_SyncReminder_Off"))
    }

    @Test
    fun `when sync reminder is clicked then update value`() {
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(true)
        whenever(brushSyncReminderUseCase.setCurrentProfileReminder(true))
            .thenReturn(Completable.complete())
        viewModel.updateViewState { copy(isBrushingSyncReminderOn = false) }

        viewModel.onBrushingReminderClick(isOn = true)

        assertTrue(viewModel.getViewState()?.isBrushingSyncReminderOn == true)
        verify(brushSyncReminderUseCase).setCurrentProfileReminder(true)
    }

    @Test
    fun `when system notifications are enabled and user set reminder then brushingReminder is on`() {
        whenever(brushSyncReminderUseCase.isCurrentProfileReminderOn())
            .thenReturn(Single.just(true))
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(true)

        viewModel.updateViewState { copy(isBrushingSyncReminderOn = false) }

        viewModel.fetchBrushSyncReminder()

        assertTrue(viewModel.getViewState()?.isBrushingSyncReminderOn == true)
    }

    @Test
    fun `when system notifications are disabled and user set reminder then brushingReminder is off`() {
        whenever(brushSyncReminderUseCase.isCurrentProfileReminderOn())
            .thenReturn(Single.just(true))
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(false)

        viewModel.updateViewState { copy(isBrushingSyncReminderOn = false) }

        viewModel.fetchBrushSyncReminder()

        assertTrue(viewModel.getViewState()?.isBrushingSyncReminderOn == false)
    }

    @Test
    fun `when system notifications are enabled but user does not set reminder then brushingReminder is off`() {
        whenever(brushSyncReminderUseCase.isCurrentProfileReminderOn())
            .thenReturn(Single.just(false))
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(true)

        viewModel.updateViewState { copy(isBrushingSyncReminderOn = false) }

        viewModel.fetchBrushSyncReminder()

        assertTrue(viewModel.getViewState()?.isBrushingSyncReminderOn == false)
    }

    @Test
    fun `when notifications are disabled and user click on brush reminder then app shows dialog`() {
        whenever(systemNotificationsEnabledUseCase.areNotificationsEnabled())
            .thenReturn(false)

        viewModel.updateViewState { copy(isBrushingSyncReminderOn = false) }

        viewModel.onBrushingReminderClick(isOn = true)

        verify(notificationsNavigator).showNotificationsDisabledDialog()
    }

    @Test
    fun `onGoToSettingsClick navigates to notifications app settings `() {
        viewModel.onGoToSettingsClick()

        verify(notificationsNavigator).openAppNotificationSettings()
    }

    @Test
    fun `onCloseScreen sends Analytics event`() {
        viewModel.onCloseScreen()

        verify(eventTracker).sendEvent(AnalyticsEvent("Notification_GoBack"))
    }

    @Test
    fun `when reminders are fetched  then update view state`() {
        val reminders = BrushingReminders(
            morningReminder = BrushingReminder(LocalTime.of(9, 0), true),
            afternoonReminder = BrushingReminder(LocalTime.of(19, 10), true),
            eveningReminder = BrushingReminder(LocalTime.of(21, 20), true)
        )
        whenever(brushReminderUseCase.fetchBrushingReminders())
            .thenReturn(Single.just(reminders))

        viewModel.fetchBrushReminder()

        assertEquals(reminders.morningReminder, viewModel.getViewState()?.morningReminder)
        assertEquals(reminders.afternoonReminder, viewModel.getViewState()?.afternoonReminder)
        assertEquals(reminders.eveningReminder, viewModel.getViewState()?.eveningReminder)
    }

    @Test
    fun `when brushing reminder time click then push ShowTimePicker action`() {
        viewModel.updateViewState {
            NotificationsViewState.initial().copy(
                morningReminder = BrushingReminder(LocalTime.of(10, 0), true)
            )
        }

        val actionTest = viewModel.actionsObservable.test()

        viewModel.onBrushingReminderTimeClick(MORNING)

        actionTest.assertValue(
            NotificationsActions.ShowTimePicker(
                MORNING,
                LocalTime.of(10, 0)
            )
        )
    }

    @Test
    fun `after updating reminder time app saves current reminders`() {
        whenever(brushReminderUseCase.updateBrushingReminders(any()))
            .thenReturn(Completable.complete())

        viewModel.updateViewState {
            NotificationsViewState.initial()
        }

        val newTime = LocalTime.of(13, 44)
        viewModel.userSelectedReminderTime(newTime, BrushingReminderType.AFTERNOON)

        assertEquals(BrushingReminder(newTime, false), viewModel.getViewState()?.afternoonReminder)

        val expectedReminders = BrushingReminders(
            morningReminder = BrushingReminder.defaultMorning(),
            afternoonReminder = BrushingReminder(newTime, false),
            eveningReminder = BrushingReminder.defaultEvening()
        )
        verify(brushReminderUseCase).updateBrushingReminders(expectedReminders)
    }

    @Test
    fun `when morningReminder changes, the livedata should dispatch the formatter results`() {

        val localTimeOfThisMorning = LocalTime.now()
        val expectedTime = "8:00 AM"

        whenever(brushReminderTimeFormatter.format(localTimeOfThisMorning))
            .thenReturn(expectedTime)

        viewModel.updateViewState {
            this.withReminderTime(localTimeOfThisMorning, MORNING)
        }

        viewModel.morningReminderTime.test().assertValue(expectedTime)
    }

    @Test
    fun `when afternoonReminderTime changes, the livedata should dispatch the formatter results`() {

        val localTimeOfThisAfternoon = LocalTime.now()
        val expectedTime = "6:00 PM"

        whenever(brushReminderTimeFormatter.format(localTimeOfThisAfternoon))
            .thenReturn(expectedTime)

        viewModel.updateViewState {
            this.withReminderTime(localTimeOfThisAfternoon, AFTERNOON)
        }

        viewModel.afternoonReminderTime.test().assertValue(expectedTime)
    }

    @Test
    fun `when eveningReminder changes, the livedata should dispatch the formatter results`() {

        val localTimeOfThisEvening = LocalTime.now()
        val expectedTime = "10:00 PM"

        whenever(brushReminderTimeFormatter.format(localTimeOfThisEvening))
            .thenReturn(expectedTime)

        viewModel.updateViewState {
            this.withReminderTime(localTimeOfThisEvening, EVENING)
        }

        viewModel.eveningReminderTime.test().assertValue(expectedTime)
    }
}
