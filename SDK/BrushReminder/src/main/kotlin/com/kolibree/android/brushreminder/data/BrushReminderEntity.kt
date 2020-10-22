/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kolibree.android.brushreminder.model.BrushingReminderType
import com.kolibree.android.synchronizator.data.database.UuidConverters
import org.threeten.bp.LocalTime

@Entity(tableName = "brush_reminder")
@TypeConverters(LocalTimeConverter::class, UuidConverters::class)
internal data class BrushReminderEntity(
    @PrimaryKey
    @ColumnInfo(name = "profile_id") val profileId: Long,
    @ColumnInfo(name = "is_morning_reminder_on") val isMorningReminderOn: Boolean,
    @ColumnInfo(name = "morning_reminder_time") val morningReminderTime: LocalTime,
    @ColumnInfo(name = "is_afternoon_reminder_on") val isAfternoonReminderOn: Boolean,
    @ColumnInfo(name = "afternoon_reminder_time") val afternoonReminderTime: LocalTime,
    @ColumnInfo(name = "is_evening_reminder_on") val isEveningReminderOn: Boolean,
    @ColumnInfo(name = "evening_reminder_time") val eveningReminderTime: LocalTime
) {
    companion object {
        fun new(profileId: Long) = BrushReminderEntity(
            profileId = profileId,
            isMorningReminderOn = false,
            morningReminderTime = BrushingReminderType.MORNING.defaultLocalTime(),
            isAfternoonReminderOn = false,
            afternoonReminderTime = BrushingReminderType.AFTERNOON.defaultLocalTime(),
            isEveningReminderOn = false,
            eveningReminderTime = BrushingReminderType.EVENING.defaultLocalTime()
        )
    }
}

internal object LocalTimeConverter {

    @TypeConverter
    @JvmStatic
    fun toZoneOffset(secondOfDay: Long): LocalTime = LocalTime.ofSecondOfDay(secondOfDay)

    @TypeConverter
    @JvmStatic
    fun fromZoneOffset(localTime: LocalTime): Long = localTime.toSecondOfDay().toLong()
}
