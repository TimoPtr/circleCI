/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.kolibree.android.extensions.toKolibreeDay
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Dao
internal abstract class BrushingSessionStatDao {

    @Insert(onConflict = REPLACE)
    abstract fun insert(brushingSessionStats: List<BrushingSessionStatsEntity>)

    @Query("SELECT * FROM brushing_session_stat")
    @VisibleForTesting
    abstract fun readAll(): List<BrushingSessionStatsEntity>

    fun readByDateTime(profileId: Long, day: LocalDateTime): List<BrushingSessionStatsEntity> {
        return readByKolibreeDay(profileId, day.toKolibreeDay())
    }

    @Query("SELECT * FROM brushing_session_stat WHERE assignedDate=:kolibreeDay AND profileId=:profileId")
    abstract fun readByKolibreeDay(profileId: Long, kolibreeDay: LocalDate): List<BrushingSessionStatsEntity>

    @Query("DELETE FROM brushing_session_stat WHERE creationTime=:creationTime AND profileId=:profileId")
    abstract fun removeByCreationTime(profileId: Long, creationTime: LocalDateTime): Int

    @Query("DELETE FROM brushing_session_stat")
    abstract fun truncate()
}
