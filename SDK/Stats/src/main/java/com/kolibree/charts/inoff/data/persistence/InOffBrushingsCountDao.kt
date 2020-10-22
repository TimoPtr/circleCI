/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal interface InOffBrushingsCountDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(entity: InOffBrushingsCountEntity)

    @Query("SELECT * FROM in_off_brushings_count WHERE profileId = :profileId")
    fun getByProfileStream(profileId: Long): Flowable<InOffBrushingsCountEntity>

    @Query("DELETE FROM in_off_brushings_count")
    override fun truncate(): Completable
}
