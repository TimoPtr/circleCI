/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.sdkws.appdata.AppDataImpl
import io.reactivex.Maybe

/**
 * Room app data DAO
 */
@Dao
internal interface AppDataDao {

    /**
     * Get the app data for the given profile ID, if there is data
     *
     * @param profileId Long profile ID
     * @param isSynchronized Boolean sync flag
     */
    @Query("SELECT * FROM appdata WHERE profile_id = :profileId AND is_synchronized = :isSynchronized")
    fun getAppData(profileId: Long, isSynchronized: Boolean): Maybe<AppDataImpl>

    @Query("SELECT * FROM appdata WHERE profile_id = :profileId ORDER BY timestamp DESC LIMIT 1")
    fun getLastAppData(profileId: Long): Maybe<AppDataImpl>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appData: AppDataImpl)
}
