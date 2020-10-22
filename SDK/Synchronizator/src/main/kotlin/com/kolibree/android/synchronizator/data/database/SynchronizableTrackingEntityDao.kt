/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.UploadStatus
import io.reactivex.Completable
import java.util.UUID

@Dao
internal interface SynchronizableTrackingEntityDao :
    SynchronizableTrackingEntityRepository, Truncable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: SynchronizableTrackingEntity)

    @Update
    override fun updateCompletable(entity: SynchronizableTrackingEntity): Completable

    @Query("DELETE FROM synchronizable_item_tracking WHERE uuid=:uuid")
    override fun delete(uuid: UUID)

    @Update
    override fun update(entity: SynchronizableTrackingEntity)

    @Query("SELECT * FROM synchronizable_item_tracking WHERE bundleKey = :bundlekey AND uuid=:uuid")
    override fun read(bundlekey: SynchronizableKey, uuid: UUID): SynchronizableTrackingEntity?

    @Query("SELECT * FROM synchronizable_item_tracking WHERE bundleKey = :bundlekey AND uploadStatus IN (:uploadStatus)")
    override fun readByUploadStatus(
        bundlekey: SynchronizableKey,
        vararg uploadStatus: UploadStatus
    ): List<SynchronizableTrackingEntity>

    @Query("SELECT * FROM synchronizable_item_tracking")
    fun readAll(): List<SynchronizableTrackingEntity>

    @Query("SELECT * FROM synchronizable_item_tracking WHERE bundleKey = :bundlekey AND isDeletedLocally = 1")
    override fun getPendingDelete(bundlekey: SynchronizableKey): List<SynchronizableTrackingEntity>

    @Query("DELETE FROM synchronizable_item_tracking")
    override fun truncate(): Completable
}

internal interface SynchronizableTrackingEntityRepository {
    fun updateCompletable(entity: SynchronizableTrackingEntity): Completable
    fun update(entity: SynchronizableTrackingEntity)

    fun read(bundlekey: SynchronizableKey, uuid: UUID): SynchronizableTrackingEntity?

    fun readByUploadStatus(
        bundlekey: SynchronizableKey,
        vararg uploadStatus: UploadStatus
    ): List<SynchronizableTrackingEntity>

    fun getPendingDelete(bundlekey: SynchronizableKey): List<SynchronizableTrackingEntity>

    fun delete(uuid: UUID)
}

internal fun SynchronizableTrackingEntityRepository.updateWrapper(wrapper: SynchronizableItemWrapper) {
    update(wrapper.trackingEntity)
}
