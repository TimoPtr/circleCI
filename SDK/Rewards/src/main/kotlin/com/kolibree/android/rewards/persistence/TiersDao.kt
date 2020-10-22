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
import androidx.room.Query
import androidx.room.Transaction
import com.kolibree.android.rewards.models.TierEntity
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
internal abstract class TiersDao {

    @Transaction
    open fun replace(tierEntity: List<TierEntity>) {
        truncate()

        insertAll(tierEntity)
    }

    @Query("DELETE FROM tiers")
    abstract fun truncate()

    @Insert
    abstract fun insertAll(tiers: List<TierEntity>)

    @Query("SELECT * FROM tiers WHERE level > :level")
    abstract fun tiersHigherThan(level: Int): Maybe<List<TierEntity>>

    @Query("SELECT * from tiers where level = :level")
    abstract fun read(level: Int): TierEntity

    @Query("SELECT * FROM tiers")
    abstract fun getTiers(): Flowable<List<TierEntity>>
}
