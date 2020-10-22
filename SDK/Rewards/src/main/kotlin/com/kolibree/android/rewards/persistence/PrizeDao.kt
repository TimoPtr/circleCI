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
import com.kolibree.android.rewards.models.PrizeEntity
import io.reactivex.Flowable

@Dao
internal abstract class PrizeDao {

    @Transaction
    open fun replace(tierEntity: List<PrizeEntity>) {
        truncate()

        insertAll(tierEntity)
    }

    @Query("DELETE FROM prizes")
    abstract fun truncate()

    @Insert
    abstract fun insertAll(prizes: List<PrizeEntity>)

    @Query("SELECT * FROM prizes")
    abstract fun getPrizes(): Flowable<List<PrizeEntity>>
}
