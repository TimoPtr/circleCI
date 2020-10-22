/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.partnerships.data.persistence.PartnershipDao
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal interface HeadspacePartnershipDao : PartnershipDao {

    override fun insertOrReplace(entity: PartnershipEntity) =
        insertOrReplaceInternal(entity as HeadspacePartnershipEntity)

    override fun findBy(profileId: Long): Flowable<PartnershipEntity> =
        findByInternal(profileId).map { it }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceInternal(entity: HeadspacePartnershipEntity)

    @Query("SELECT * FROM headspace_partnership WHERE profile_id = :profileId")
    fun findByInternal(profileId: Long): Flowable<HeadspacePartnershipEntity>

    @Query("DELETE FROM headspace_partnership")
    override fun truncate(): Completable
}
