package com.kolibree.sdkws.brushing.persistence.repo

import com.kolibree.android.clock.TrustedClock
import com.kolibree.sdkws.brushing.persistence.dao.BrushingDao
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.Brushing
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime

/**
 *  UPDATE LOCAL BRUSHING DATA IN ROOM
 */

internal class BrushingsDatastoreImpl @Inject constructor(private val brushingDao: BrushingDao) : BrushingsDatastore {

    /**
     * Add new brushing locally
     */
    private fun addBrushing(localBrushing: BrushingInternal): Long =
        Single.defer { Single.just(brushingDao.addBrushing(localBrushing)) }
            .subscribeOn(Schedulers.io())
            .blockingGet()

    /**
     * Verify to add any duplicate
     */
    override fun addBrushingIfDoNotExist(localBrushing: BrushingInternal): Long =
        if (exists(localBrushing)) {
            -1
        } else {
            addBrushing(localBrushing)
        }

    /**
     * Get all locally stored brushing
     *
     * @param profileId the profile id to retrieve brushing data
     * @return a non-null ArrayList
     */
    override fun getBrushings(profileId: Long): Single<List<BrushingInternal>> =
        brushingDao.getBrushings(profileId)
            .subscribeOn(Schedulers.io())
            .defaultIfEmpty(emptyList())
            .toSingle()

    /**
     * Get all locally stored brushing for given game
     *
     * @param gameId ID of game we're interested in
     * @param profileId the profile id to retrieve brushing data
     * @return a non-null ArrayList
     */
    override fun getBrushings(gameId: String, profileId: Long): Single<List<BrushingInternal>> =
        brushingDao.getBrushings(gameId, profileId)
            .subscribeOn(Schedulers.io())
            .defaultIfEmpty(emptyList())
            .toSingle()

    override fun brushingsFlowable(profileId: Long): Flowable<List<BrushingInternal>> =
        brushingDao.brushingsFlowable(profileId)
            .subscribeOn(Schedulers.io())

    /**
     * Get all brushings non deleted stored locally
     *
     * @return a non-null ArrayList
     */
    override fun getNonDeletedBrushings(): Flowable<List<BrushingInternal>> =
        brushingDao.getNonDeletedBrushings()

    /**
     * Get all the local brushing by the name of a game
     */
    override fun getBrushingsByGame(gameType: String): List<BrushingInternal> =
        brushingDao.getBrushingsByGame(gameType)
            .subscribeOn(Schedulers.io())
            .defaultIfEmpty(emptyList())
            .blockingGet()

    /**
     * Get the first brushing session
     *
     * @param profileId long profile ID
     * @return first brushing session or null if none
     */
    override fun getFirstBrushingSession(profileId: Long): Brushing? =
        brushingDao.getFirstBrushingSession(profileId)
            .subscribeOn(Schedulers.io())
            .map { it.extractBrushing() }
            .blockingGet()

    /**
     * Get the first brushing session
     *
     * @param profileId long profile ID
     * @return first brushing session or null if none
     */
    override fun getFirstBrushingSessionFlowable(profileId: Long): Flowable<Brushing> =
        brushingDao.getFirstBrushingSessionFlowable(profileId)
            .subscribeOn(Schedulers.io())
            .map { it.extractBrushing() }

    /**
     * Get the last brushing session
     *
     * @param profileId long profile ID
     * @return last brushing session or null if none
     */
    override fun getLastBrushingSession(profileId: Long): Brushing? =
        brushingDao.getLastBrushingSession(profileId)
            .subscribeOn(Schedulers.io())
            .map { it.extractBrushing() }
            .blockingGet()

    /**
     * Get the last brushing session
     *
     * @param profileId long profile ID
     * @return last brushing session or null if none
     */
    override fun getLastBrushingSessionFlowable(profileId: Long): Flowable<Brushing> =
        brushingDao.getLastBrushingSessionFlowable(profileId)
            .subscribeOn(Schedulers.io())
            .map { it.extractBrushing() }

    /**
     * Get all the brushing from a given data for a given user
     */
    override fun getBrushingsSince(
        startTime: OffsetDateTime,
        profileId: Long
    ): Single<List<Brushing>> = getBrushingsBetween(
        startTime,
        OffsetDateTime.ofInstant(TrustedClock.getNowInstantUTC(), startTime.offset),
        profileId
    ).map { list -> list.map { it.extractBrushing() } }

    override fun getBrushingsBetween(
        begin: OffsetDateTime,
        end: OffsetDateTime,
        profileId: Long
    ): Single<List<BrushingInternal>> =
        brushingDao.getBrushingBetween(profileId, begin, end)
            .subscribeOn(Schedulers.io())
            .toSingle()

    /**
     * Count the number of brushing internal data since a given date for a given profile
     */
    override fun countBrushingsSince(startTime: OffsetDateTime, profileId: Long): Single<Long> =
        getBrushingsSince(startTime, profileId).map { it.size.toLong() }

    /**
     * Count the number of brushing done in given game since from the starting for a given profile
     */
    override fun countBrushings(gameId: String, profileId: Long): Single<Long> =
        getBrushings(gameId, profileId).map { it.size.toLong() }

    /**
     * Count the number of brushing  since from the starting for a given profile
     */
    override fun countBrushings(profileId: Long): Single<Long> =
        getBrushings(profileId).map { it.size.toLong() }

    /**
     * Verify if a brushing has been stored locally previously or not
     */
    override fun exists(brushing: BrushingInternal): Boolean =
        brushingDao.exists(brushing.profileId, brushing.dateTime)
            .subscribeOn(Schedulers.io())
            .blockingGet()
            .isNotEmpty()

    /**
     * Delete locally a list of brushings
     */
    override fun delete(brushings: List<Brushing>): Completable =
        Completable.fromCallable {
            for (i in brushings.indices) {
                deleteByDateTime(brushings[i].dateTime).blockingAwait()
            }
        }

    override fun deleteByDateTime(dateTime: OffsetDateTime): Completable =
        Completable.fromCallable {
            brushingDao.deleteByDateTime(dateTime)
        }

    /**
     * Delete a brushing
     */
    override fun deleteBrushing(brushing: BrushingInternal): Completable =
        Completable.fromCallable {
            brushingDao.delete(brushing.profileId, brushing.dateTime)
        }

    /**
     * Get all the brushings deleted locally
     */
    override fun getDeletedLocally(): Single<List<Brushing>> =
        brushingDao.getDeletedLocally()
            .map { list -> list.map { it.extractBrushing() } }
            .subscribeOn(Schedulers.io())
            .defaultIfEmpty(emptyList())
            .toSingle()

    override fun clearNonSynchronized(profileId: Long): Completable =
        Completable.fromCallable {
            brushingDao.clearNonSynchronized(profileId)
        }

    override fun getNonSynchronizedBrushing(profileId: Long): Single<List<BrushingInternal>> =
        brushingDao.getNonSynchronizedBrushing(profileId)
            .subscribeOn(Schedulers.io())
            .defaultIfEmpty(emptyList())
            .toSingle()

    /**
     * updateBrushing a brushing
     */
    override fun updateBrushing(brushing: BrushingInternal): Int {
        val brushingsList = brushingDao.getBrushingsOnDate(
            brushing.profileId,
            brushing.dateTime
        ).subscribeOn(Schedulers.io()).blockingGet(emptyList())

        val oldPoints = if (brushingsList.isNotEmpty()) {
            brushingsList[0].points
        } else 0

        brushingDao.update(brushing)
        return brushing.points - oldPoints // If there is a difference we have to report it
    }

    /**
     * Delete all entry in the DB
     */
    override fun deleteAll(): Completable =
        Completable.fromCallable { brushingDao.deleteAll() }

    override fun deleteLocally(dateTime: OffsetDateTime): Completable =
        Completable.fromCallable {
            brushingDao.deleteLocallyByDateTime(dateTime)
        }
}
