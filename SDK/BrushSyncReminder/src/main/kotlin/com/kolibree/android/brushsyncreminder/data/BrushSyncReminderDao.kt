/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.synchronizator.data.database.UuidConverters
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.UUID

@Dao
internal interface BrushSyncReminderDao : Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(entity: BrushSyncReminderEntity): Completable

    @Query("DELETE FROM brush_sync_reminder WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    fun deleteBy(uuid: UUID): Completable

    @Query("SELECT * FROM brush_sync_reminder WHERE profile_id = :profileId")
    fun findBy(profileId: Long): Maybe<BrushSyncReminderEntity>

    @Query("SELECT * FROM brush_sync_reminder WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    fun findBy(uuid: UUID): Maybe<BrushSyncReminderEntity>

    @Query("SELECT * FROM brush_sync_reminder")
    fun readAll(): Single<List<BrushSyncReminderEntity>>

    @Query("SELECT * FROM brush_sync_reminder WHERE is_enabled = 1")
    fun enabledReminders(): Single<List<BrushSyncReminderEntity>>

    @Query("DELETE FROM brush_sync_reminder")
    override fun truncate(): Completable
}
