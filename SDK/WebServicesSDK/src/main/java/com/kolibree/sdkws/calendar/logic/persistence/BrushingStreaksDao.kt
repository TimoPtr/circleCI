/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.kolibree.android.calendar.logic.persistence.model.BrushingStreakEntity

@Dao
internal interface BrushingStreaksDao {

    @Insert(onConflict = REPLACE)
    fun insertAll(streaks: List<BrushingStreakEntity>): List<Long>

    @Transaction
    fun replaceForProfile(profileId: Long, streaks: List<BrushingStreakEntity>): List<Long> {
        deleteByProfile(profileId)
        return insertAll(streaks)
    }

    @Query("SELECT * FROM brushing_streaks WHERE profile_id = :profileId")
    fun queryByProfile(profileId: Long): List<BrushingStreakEntity>

    @Query("DELETE FROM  brushing_streaks WHERE profile_id = :profileId")
    fun deleteByProfile(profileId: Long)

    @Query("DELETE FROM brushing_streaks")
    fun truncate()
}
