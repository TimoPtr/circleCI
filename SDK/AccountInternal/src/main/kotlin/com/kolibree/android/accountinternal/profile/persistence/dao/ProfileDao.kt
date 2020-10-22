/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * Created by guillaumeagis on 31/05/18.
 */

@Dao
internal interface ProfileDao {

    @Query("SELECT DISTINCT * FROM profiles")
    fun profilesFlowable(): Flowable<List<ProfileInternal>>

    @Query("SELECT DISTINCT * FROM profiles")
    fun getProfiles(): Maybe<List<ProfileInternal>>

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    fun getProfile(profileId: Long): Maybe<ProfileInternal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfiles(param: List<ProfileInternal>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfile(param: ProfileInternal): Long

    @Query("UPDATE profiles SET needs_update = 0 WHERE id = :id")
    fun markAsUpdated(id: Long): Completable

    @Query("DELETE FROM profiles WHERE id = :id")
    fun delete(id: Long)

    @Update
    fun update(profileInternal: ProfileInternal)

    @Query("DELETE FROM profiles")
    fun deleteAll()
}
