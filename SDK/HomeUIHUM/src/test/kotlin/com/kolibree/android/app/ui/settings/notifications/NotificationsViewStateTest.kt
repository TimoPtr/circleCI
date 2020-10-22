/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminderType
import com.kolibree.android.brushreminder.model.BrushingReminders
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalTime

internal class NotificationsViewStateTest : BaseUnitTest() {

    @Test
    fun `brushingReminder returns appropriate object based on type`() {
        val viewState = defaultViewState()
        assertEquals(
            BrushingReminder.defaultMorning(),
            viewState.brushingReminder(BrushingReminderType.MORNING)
        )
        assertEquals(
            BrushingReminder.defaultAfternoon(),
            viewState.brushingReminder(BrushingReminderType.AFTERNOON)
        )
        assertEquals(
            BrushingReminder.defaultEvening(),
            viewState.brushingReminder(BrushingReminderType.EVENING)
        )
    }

    @Test
    fun `withReminderOn changes isOn of appropriate type`() {
        val viewState = defaultViewState().withReminderOn(true, BrushingReminderType.EVENING)
        assertTrue(viewState.eveningReminder.isOn)
        assertFalse(viewState.morningReminder.isOn)
        assertFalse(viewState.afternoonReminder.isOn)
    }

    @Test
    fun `withReminderTime changes time of appropriate type`() {
        val newTime = LocalTime.of(13, 55)
        val viewState = defaultViewState().withReminderTime(newTime, BrushingReminderType.AFTERNOON)
        assertEquals(newTime, viewState.afternoonReminder.time)
        assertEquals(
            BrushingReminderType.MORNING.defaultLocalTime(),
            viewState.morningReminder.time
        )
        assertEquals(
            BrushingReminderType.EVENING.defaultLocalTime(),
            viewState.eveningReminder.time
        )
    }

    @Test
    fun `withReminders returns viewState with values from BrushingReminders`() {
        val morningReminder = BrushingReminder(
            time = LocalTime.of(6, 55),
            isOn = true
        )
        val afternoonReminder = BrushingReminder.defaultAfternoon()
        val eveningReminder = BrushingReminder(
            time = LocalTime.of(21, 45),
            isOn = true
        )
        val reminders = BrushingReminders(
            morningReminder = morningReminder,
            afternoonReminder = afternoonReminder,
            eveningReminder = eveningReminder
        )

        val viewState = defaultViewState()
        assertNotSame(reminders.morningReminder, viewState.morningReminder)
        assertNotSame(reminders.eveningReminder, viewState.eveningReminder)

        val viewStateWithReminders = viewState.withReminders(reminders)
        assertEquals(reminders.morningReminder, viewStateWithReminders.morningReminder)
        assertEquals(reminders.afternoonReminder, viewStateWithReminders.afternoonReminder)
        assertEquals(reminders.eveningReminder, viewStateWithReminders.eveningReminder)
    }

    fun defaultViewState(): NotificationsViewState = NotificationsViewState.initial().copy(
        morningReminder = BrushingReminder.defaultMorning(),
        afternoonReminder = BrushingReminder.defaultAfternoon(),
        eveningReminder = BrushingReminder.defaultEvening()
    )
}
