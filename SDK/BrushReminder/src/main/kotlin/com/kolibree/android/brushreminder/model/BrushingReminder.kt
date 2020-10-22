/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.model

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalTime

@VisibleForApp
@Parcelize
data class BrushingReminder(
    val time: LocalTime,
    val isOn: Boolean
) : Parcelable {

    @VisibleForApp
    companion object {
        fun defaultMorning() = defaultReminder(BrushingReminderType.MORNING)
        fun defaultAfternoon() = defaultReminder(BrushingReminderType.AFTERNOON)
        fun defaultEvening() = defaultReminder(BrushingReminderType.EVENING)

        private fun defaultReminder(reminderType: BrushingReminderType) = BrushingReminder(
            time = reminderType.defaultLocalTime(),
            isOn = false
        )
    }
}
