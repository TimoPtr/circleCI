/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.interfaces.Truncable
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
internal interface BrushReminderDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(entity: BrushReminderEntity): Completable

    @Query("SELECT * FROM brush_reminder WHERE profile_id = :profileId")
    fun findBy(profileId: Long): Maybe<BrushReminderEntity>

    @Query("SELECT * FROM brush_reminder")
    fun readAll(): Single<List<BrushReminderEntity>>

    @Query("DELETE FROM brush_reminder")
    override fun truncate(): Completable
}
