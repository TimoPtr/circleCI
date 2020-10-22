/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.charts.persistence.models.StatInternal
import io.reactivex.Maybe

/**
 * Created by guillaumeagis on 13/05/18.
 * Store stats into Room
 */

@Dao
internal interface StatDao {

    @Query("SELECT * FROM stat WHERE profile_id = :profileId AND timestamp >= :fromTimestamp")
    fun readStatsSince(profileId: Long, fromTimestamp: Long): Maybe<List<StatInternal>>

    @Query("SELECT * FROM stat WHERE profile_id = :profileId")
    fun getForProfile(profileId: Long): List<StatInternal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(param: List<StatInternal>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(param: StatInternal): Long

    @Query("DELETE FROM stat WHERE profile_id = :profileId")
    fun deleteForProFile(profileId: Long)

    @Query("DELETE FROM stat")
    fun truncate()
}
