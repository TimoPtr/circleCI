/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.game.shorttask.data.persistence.model.ShortTaskEntity
import com.kolibree.android.synchronizator.data.database.UuidConverters
import io.reactivex.Completable
import java.util.UUID

@Dao
internal abstract class ShortTaskDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(entity: ShortTaskEntity)

    @Query("SELECT * FROM short_tasks WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    abstract fun getByUuid(uuid: UUID): ShortTaskEntity

    @Query("DELETE FROM short_tasks WHERE profileId = :profileId")
    abstract fun delete(profileId: Long)

    @Query("DELETE FROM short_tasks WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    abstract fun delete(uuid: UUID)

    @Query("DELETE FROM short_tasks")
    abstract override fun truncate(): Completable
}
