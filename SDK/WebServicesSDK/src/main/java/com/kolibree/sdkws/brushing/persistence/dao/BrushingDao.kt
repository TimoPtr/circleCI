/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.brushing.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.threeten.bp.OffsetDateTime

/**
 * Created by guillaumeagis on 31/05/18.
 */

@Dao
internal abstract class BrushingDao {

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND is_deleted_locally = 0")
    abstract fun getBrushings(profileid: Long): Maybe<List<BrushingInternal>>

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND game = :gameId AND is_deleted_locally = 0")
    abstract fun getBrushings(gameId: String, profileid: Long): Maybe<List<BrushingInternal>>

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND is_deleted_locally = 0")
    abstract fun brushingsFlowable(profileid: Long): Flowable<List<BrushingInternal>>

    @Query("SELECT * FROM brushing WHERE is_deleted_locally = 0")
    abstract fun getNonDeletedBrushings(): Flowable<List<BrushingInternal>>

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND is_deleted_locally = 0 ORDER BY timestamp ASC LIMIT 1")
    abstract fun getFirstBrushingSession(profileid: Long): Maybe<BrushingInternal>

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND is_deleted_locally = 0 ORDER BY timestamp ASC LIMIT 1")
    abstract fun getFirstBrushingSessionFlowable(profileid: Long): Flowable<BrushingInternal>

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND is_deleted_locally = 0 ORDER BY timestamp DESC LIMIT 1")
    abstract fun getLastBrushingSession(profileid: Long): Maybe<BrushingInternal>

    @Query("SELECT * FROM brushing WHERE profileid = :profileid AND is_deleted_locally = 0 ORDER BY timestamp DESC LIMIT 1")
    abstract fun getLastBrushingSessionFlowable(profileid: Long): Flowable<BrushingInternal>

    @Query("SELECT * FROM brushing WHERE game = :game AND is_deleted_locally = 0")
    abstract fun getBrushingsByGame(game: String): Maybe<List<BrushingInternal>>

    @Query("SELECT * FROM brushing WHERE timestamp = :timestamp AND profileid = :profileId AND is_deleted_locally = 0")
    protected abstract fun getBrushingsOnDateInternal(profileId: Long, timestamp: Long): Maybe<List<BrushingInternal>>

    fun getBrushingsOnDate(
        profileId: Long,
        date: OffsetDateTime
    ): Maybe<List<BrushingInternal>> = getBrushingsOnDateInternal(profileId, date.toEpochSecond())

    @Query("SELECT issync FROM brushing WHERE timestamp = :timestamp")
    protected abstract fun existsInternal(timestamp: Long): Maybe<List<Boolean>>

    fun exists(date: OffsetDateTime): Maybe<List<Boolean>> = existsInternal(date.toEpochSecond())

    // get only issync to make the result of the request
    @Query("SELECT issync FROM brushing WHERE timestamp = :timestamp AND profileid = :profileId AND is_deleted_locally = 0")
    abstract fun existsInternal(profileId: Long, timestamp: Long): Maybe<List<Boolean>>

    fun exists(profileId: Long, date: OffsetDateTime): Maybe<List<Boolean>> =
        existsInternal(profileId, date.toEpochSecond())

    @Suppress("MaxLineLength")
    @Query("SELECT * FROM brushing WHERE timestamp >= :beginTimestamp AND timestamp <= :endTimestamp AND is_deleted_locally = 0 AND profileid = :profileId ")
    protected abstract fun getBrushingBetweenInternal(
        profileId: Long,
        beginTimestamp: Long,
        endTimestamp: Long
    ): Maybe<List<BrushingInternal>>

    fun getBrushingBetween(
        profileId: Long,
        beginDate: OffsetDateTime,
        endDate: OffsetDateTime
    ): Maybe<List<BrushingInternal>> =
        getBrushingBetweenInternal(profileId, beginDate.toEpochSecond(), endDate.toEpochSecond())

    @Query("SELECT AVG(duration) FROM brushing WHERE timestamp >= :timestamp AND is_deleted_locally = 0 AND profileid = :profileId ")
    protected abstract fun getAverageBrushingDurationInternal(profileId: Long, timestamp: Long): Maybe<Int>

    fun getAverageBrushingDuration(profileId: Long, date: OffsetDateTime): Maybe<Int> =
        getAverageBrushingDurationInternal(profileId, date.toEpochSecond())

    @Query("SELECT * FROM brushing WHERE issync = 0 AND profileid = :profileId ")
    abstract fun getNonSynchronizedBrushing(profileId: Long): Maybe<List<BrushingInternal>>

    @Query("SELECT * FROM brushing WHERE is_deleted_locally = 1")
    abstract fun getDeletedLocally(): Maybe<List<BrushingInternal>>

    @Query("DELETE FROM brushing WHERE timestamp = :timestamp")
    protected abstract fun deleteByTimestampInternal(timestamp: Long)

    fun deleteByDateTime(date: OffsetDateTime) {
        deleteByTimestampInternal(date.toEpochSecond())
    }

    @Query("UPDATE brushing SET issync =:isSynched WHERE timestamp =:timestamp")
    protected abstract fun flagBrushingAsSyncPendingInternal(isSynched: Int, timestamp: Long)

    fun flagBrushingAsSyncPending(isSynched: Int, date: OffsetDateTime) {
        flagBrushingAsSyncPendingInternal(isSynched, date.toEpochSecond())
    }

    @Query("DELETE FROM brushing WHERE issync = 0 AND is_deleted_locally = 0 AND profileid = :profileId ")
    abstract fun clearNonSynchronized(profileId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addBrushing(param: BrushingInternal): Long

    @Query("DELETE FROM brushing WHERE timestamp = :timestamp AND profileid = :profileId ")
    protected abstract fun deleteInternal(profileId: Long, timestamp: Long)

    fun delete(profileId: Long, date: OffsetDateTime) {
        deleteInternal(profileId, date.toEpochSecond())
    }

    @Update
    abstract fun update(brushingInternal: BrushingInternal)

    @Query("DELETE FROM brushing")
    abstract fun deleteAll()

    @Query("UPDATE brushing SET is_deleted_locally =1 WHERE timestamp =:timestamp")
    protected abstract fun deleteLocallyByTimestampInternal(timestamp: Long)

    fun deleteLocallyByDateTime(date: OffsetDateTime) {
        deleteLocallyByTimestampInternal(date.toEpochSecond())
    }
}
