/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.calendar.logic.model.BrushingStreak
import com.kolibree.android.room.DateConvertersString
import org.threeten.bp.LocalDate

@Entity(tableName = "brushing_streaks")
@TypeConverters(DateConvertersString::class)
internal data class BrushingStreakEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "profile_id") var profileId: Long,
    @ColumnInfo(name = "start_date") val start: LocalDate,
    @ColumnInfo(name = "end_date") val end: LocalDate
) {
    @Ignore
    fun toStreak(): BrushingStreak = BrushingStreak(start, end)

    companion object {

        @Ignore
        fun from(profileId: Long, start: LocalDate, end: LocalDate) =
            BrushingStreakEntity(profileId = profileId, start = start, end = end)

        @Ignore
        fun from(profileId: Long, streak: BrushingStreak) =
            BrushingStreakEntity(profileId = profileId, start = streak.start, end = streak.end)
    }
}
