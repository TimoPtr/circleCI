/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.brushreminder.model.BrushingReminder
import com.kolibree.android.brushreminder.model.BrushingReminderType
import com.kolibree.android.brushreminder.model.BrushingReminders
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalTime

@Parcelize
internal data class NotificationsViewState(
    val isBrushingSyncReminderOn: Boolean = false,
    val isNewsletterSubscriptionOn: Boolean = false,
    val isChangingNewsletterSubscription: Boolean = false,
    val isChangingReminder: Boolean = false,
    val morningReminder: BrushingReminder = BrushingReminder.defaultMorning(),
    val afternoonReminder: BrushingReminder = BrushingReminder.defaultAfternoon(),
    val eveningReminder: BrushingReminder = BrushingReminder.defaultEvening()
) : BaseViewState {

    @IgnoredOnParcel
    val brushingReminders: BrushingReminders = BrushingReminders(
        morningReminder = morningReminder,
        afternoonReminder = afternoonReminder,
        eveningReminder = eveningReminder
    )

    fun brushingReminder(type: BrushingReminderType): BrushingReminder = when (type) {
        BrushingReminderType.MORNING -> morningReminder
        BrushingReminderType.AFTERNOON -> afternoonReminder
        BrushingReminderType.EVENING -> eveningReminder
    }

    fun withReminderOn(isReminderOn: Boolean, type: BrushingReminderType): NotificationsViewState {
        val updatedReminder = brushingReminder(type).copy(isOn = isReminderOn)
        return updateReminder(type, updatedReminder)
    }

    fun withReminderTime(time: LocalTime, type: BrushingReminderType): NotificationsViewState {
        val updatedReminder = brushingReminder(type).copy(time = time)
        return updateReminder(type, updatedReminder)
    }

    private fun updateReminder(
        type: BrushingReminderType,
        updatedReminder: BrushingReminder
    ): NotificationsViewState = when (type) {
        BrushingReminderType.MORNING -> copy(morningReminder = updatedReminder)
        BrushingReminderType.AFTERNOON -> copy(afternoonReminder = updatedReminder)
        BrushingReminderType.EVENING -> copy(eveningReminder = updatedReminder)
    }

    fun withReminders(reminders: BrushingReminders): NotificationsViewState = copy(
        morningReminder = reminders.morningReminder,
        afternoonReminder = reminders.afternoonReminder,
        eveningReminder = reminders.eveningReminder
    )

    companion object {
        fun initial() = NotificationsViewState()
    }
}
