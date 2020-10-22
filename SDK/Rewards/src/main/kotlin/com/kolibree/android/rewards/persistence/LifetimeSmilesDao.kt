/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal interface LifetimeSmilesDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(lifetimeSmilesEntity: LifetimeSmilesEntity)

    @Query("SELECT * from lifetime_stats where profileId = :profileId")
    fun readByProfileStream(profileId: Long): Flowable<LifetimeSmilesEntity>

    @Query("DELETE FROM lifetime_stats")
    override fun truncate(): Completable
}
