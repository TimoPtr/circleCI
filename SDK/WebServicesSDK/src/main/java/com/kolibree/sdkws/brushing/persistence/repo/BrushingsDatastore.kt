package com.kolibree.sdkws.brushing.persistence.repo

import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime

/**
 * Store data locally
 */
internal interface BrushingsDatastore {

    fun getBrushings(profileId: Long): Single<List<BrushingInternal>>
    fun getBrushings(gameId: String, profileId: Long): Single<List<BrushingInternal>>
    fun brushingsFlowable(profileId: Long): Flowable<List<BrushingInternal>>
    fun getNonDeletedBrushings(): Flowable<List<BrushingInternal>>
    fun countBrushingsSince(startTime: OffsetDateTime, profileId: Long): Single<Long>
    fun countBrushings(profileId: Long): Single<Long>
    fun countBrushings(gameId: String, profileId: Long): Single<Long>
    fun getBrushingsSince(startTime: OffsetDateTime, profileId: Long): Single<List<Brushing>>
    fun delete(brushings: List<Brushing>): Completable
    fun updateBrushing(brushing: BrushingInternal): Int
    fun getNonSynchronizedBrushing(profileId: Long): Single<List<BrushingInternal>>
    fun clearNonSynchronized(profileId: Long): Completable
    fun getDeletedLocally(): Single<List<Brushing>>
    fun getFirstBrushingSession(profileId: Long): Brushing?
    fun getFirstBrushingSessionFlowable(profileId: Long): Flowable<Brushing>
    fun getLastBrushingSession(profileId: Long): Brushing?
    fun getLastBrushingSessionFlowable(profileId: Long): Flowable<Brushing>
    fun getBrushingsByGame(gameType: String): List<BrushingInternal>
    fun deleteAll(): Completable
    fun deleteByDateTime(dateTime: OffsetDateTime): Completable
    fun deleteBrushing(brushing: BrushingInternal): Completable
    fun getBrushingsBetween(
        begin: OffsetDateTime,
        end: OffsetDateTime,
        profileId: Long
    ): Single<List<BrushingInternal>>

    fun exists(brushing: BrushingInternal): Boolean
    fun deleteLocally(dateTime: OffsetDateTime): Completable
    fun addBrushingIfDoNotExist(localBrushing: BrushingInternal): Long
}
