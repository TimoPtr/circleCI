/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings.persistence.dao

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettingsEntity
import io.reactivex.Flowable

@Dao
@VisibleForTesting
internal interface CoachSettingsDao {

    @Query("SELECT * FROM  coach_settings WHERE profile_id = :profileId")
    fun find(profileId: Long): Flowable<List<CoachSettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(param: CoachSettingsEntity): Long

    @Query("DELETE FROM coach_settings")
    fun deleteAll()
}
