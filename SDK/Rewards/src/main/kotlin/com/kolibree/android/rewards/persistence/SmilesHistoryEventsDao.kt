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
import androidx.room.Transaction
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.models.SmilesHistoryEvent
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal abstract class SmilesHistoryEventsDao : Truncable {

    @Transaction
    open fun replace(smilesHistoryEvents: List<SmilesHistoryEventEntity>) {
        if (smilesHistoryEvents.isEmpty()) return

        smilesHistoryEvents.distinctBy { it.profileId }.forEach { truncate(it.profileId) }

        insertAll(smilesHistoryEvents)
    }

    @Query("SELECT * FROM smiles_history_events WHERE id IN (:ids) ORDER BY id ASC")
    abstract fun read(ids: List<Long>): List<SmilesHistoryEventEntity>

    @Query("SELECT * FROM smiles_history_events")
    abstract fun read(): List<SmilesHistoryEventEntity>

    @Query("SELECT * FROM smiles_history_events WHERE profileId=:profileId")
    protected abstract fun historyEventEntityStream(profileId: Long): Flowable<List<SmilesHistoryEventEntity>>

    fun historyEntityStream(profileId: Long): Flowable<List<SmilesHistoryEvent>> =
        historyEventEntityStream(profileId).map { entities -> entities.map(SmilesHistoryEvent::toSpecificEvent) }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(historyEvents: List<SmilesHistoryEventEntity>)

    @Query("DELETE FROM smiles_history_events WHERE profileId=:profileId")
    abstract fun truncate(profileId: Long)

    @Query("DELETE FROM smiles_history_events")
    abstract override fun truncate(): Completable
}
