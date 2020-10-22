/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.models.ProfileSmilesEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal interface ProfileSmilesDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(profileSmilesEntity: ProfileSmilesEntity)

    @Query("SELECT * FROM profile_smiles WHERE profileId=:profileId")
    fun read(profileId: Long): Flowable<List<ProfileSmilesEntity>>

    @Query("SELECT * FROM profile_smiles")
    fun read(): Flowable<List<ProfileSmilesEntity>>

    @Query("DELETE FROM profile_smiles")
    override fun truncate(): Completable
}
