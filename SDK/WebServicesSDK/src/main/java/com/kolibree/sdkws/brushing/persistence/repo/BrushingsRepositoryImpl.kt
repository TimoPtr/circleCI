package com.kolibree.sdkws.brushing.persistence.repo

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.MIN_BRUSHING_DURATION_SECONDS
import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor
import com.kolibree.android.network.api.ApiError
import com.kolibree.sdkws.brushing.BrushingApiManager
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.exception.AlreadySavedBrushingException
import com.kolibree.sdkws.exception.InvalidBrushingDurationException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import retrofit2.HttpException
import timber.log.Timber

/**
 * Responsible to store the data (WS / Room) about the brushing data
 * Only the BrushingsRepository knows about the brushingsDatastore
 * The idea here was to not say to the app (and the app should not mind/know) where the data are
 * stored. This class will decide where to store them. In the future, if we can want to change
 * where the data will be stored, it will be totally transparent for the app.
 *
 * Still class is now 100% Java, no android dependencies !
 */

@SuppressWarnings("TooManyFunctions")
internal class BrushingsRepositoryImpl(
    private val brushingApiManager: BrushingApiManager,
    private val brushingsDatastore: BrushingsDatastore,
    private val profileDatastore: ProfileDatastore,
    private val localBrushingsProcessor: LocalBrushingsProcessor,
    private val scope: CoroutineScope
) : BrushingsRepository {

    @Inject
    constructor(
        brushingApiManager: BrushingApiManager,
        brushingsDatastore: BrushingsDatastore,
        profileDatastore: ProfileDatastore,
        localBrushingsProcessor: LocalBrushingsProcessor
    ) : this(
        brushingApiManager = brushingApiManager,
        brushingsDatastore = brushingsDatastore,
        profileDatastore = profileDatastore,
        localBrushingsProcessor = localBrushingsProcessor,
        scope = GlobalScope
    )

    /**
     * save new brushing data to an user
     */
    override fun addBrushing(
        brushingData: CreateBrushingData,
        profile: ProfileInternal,
        accountId: Long
    ): Single<Brushing> {

        if (!brushingData.isDurationValid) {
            return Single.error(
                InvalidBrushingDurationException(
                    "Brushing duration is not valid (current = ${brushingData.duration} " +
                        "/ min = $MIN_BRUSHING_DURATION_SECONDS"
                )
            )
        }
        val localBrushing = BrushingInternal.fromBrushingData(brushingData, profile.id)
        val id = brushingsDatastore.addBrushingIfDoNotExist(localBrushing)
        when (id) {
            -1L -> return Single.error(AlreadySavedBrushingException())
            else -> {
                val updatedLocalBrushing = localBrushing.copy(id = id)
                Timber.d(
                    "New brushing (%s) temporarily added, waiting for server confirmation",
                    updatedLocalBrushing.dateTime
                )

                return uploadNewBrushing(accountId, profile, updatedLocalBrushing)
                    .doFinally {
                        Timber.d("Pre global scope launch %s", Thread.currentThread().name)
                        scope.launch {
                            Timber.d("In global scope launch %s", Thread.currentThread().name)
                            localBrushingsProcessor.onBrushingCreated(updatedLocalBrushing.extractBrushing())
                        }
                    }
            }
        }
    }

    @VisibleForTesting
    fun uploadNewBrushing(
        accountId: Long,
        profile: ProfileInternal,
        localBrushing: BrushingInternal
    ): Single<Brushing> {
        return brushingApiManager.createBrushing(accountId, profile.id, localBrushing)
            .map { createBrushingOnSuccess(it, profile, localBrushing) }
            .map { it.extractBrushing() }
            .onErrorResumeNext {
                if (it is ApiError && it.isNetworkError) {
                    Timber.i("Delayed brushing creation until network is back")
                    Single.just(localBrushing.extractBrushing())
                } else {
                    Single.error(it)
                }
            }
            .doOnError {
                if ((it is HttpException) && it.code() == HTTP_BAD_REQUEST) {
                    createBrushingOnError(profile, localBrushing)
                } else {
                    Timber.w(
                        it,
                        "An error  occurred while synchronizing brushing (" + localBrushing.dateTime +
                            "), will try later"
                    )
                }
            }
    }

    /**
     * Call when the creation of a brushing has been created with success
     */
    @VisibleForTesting
    fun createBrushingOnSuccess(
        brushingResponse: BrushingsResponse,
        profile: ProfileInternal,
        localBrushing: BrushingInternal
    ): BrushingInternal {
        val newBrushing = when {
            brushingResponse.getBrushings().isEmpty() -> localBrushing
            else -> localBrushing.updateFromResponse(brushingResponse.getBrushings()[0])
        }

        brushingsDatastore.updateBrushing(newBrushing)
        return newBrushing
    }

    /**
     * Call when the creation of a brushing has been created with error
     */
    private fun createBrushingOnError(profile: ProfileInternal, localBrushing: BrushingInternal) {
        brushingsDatastore.deleteBrushing(localBrushing).blockingAwait()
        val newProfile = profile.increasePoints(-localBrushing.points)
        profileDatastore.updateProfile(newProfile)
        Timber.d(
            "JSON error Brushing (" + localBrushing.dateTime +
                ") has been rejected, deleting and cancelling " + localBrushing.points +
                " points for profile id " + profile.id
        )
    }

    /**
     * Get all locally stored brushing
     *
     * @param profileId the profile id to retrieve brushing data
     * @return a non-null ArrayList
     */
    override fun getBrushings(profileId: Long): Single<List<Brushing>> {
        return brushingsDatastore.getBrushings(profileId)
            .map {
                it.distinct().map { brushingInternal ->
                    brushingInternal.extractBrushing()
                }
            }
    }

    override fun brushingsFlowable(profileId: Long): Flowable<List<Brushing>> {
        return brushingsDatastore.brushingsFlowable(profileId)
            .map {
                it.distinct().map { brushingInternal ->
                    brushingInternal.extractBrushing()
                }
            }
    }

    /**
     * Assign brushings to the profile wrapped in this object
     *
     * Ideally this would return a Completable, but for now this project doesn't have rxjava
     *
     * @return true if the remote call succeeded, false otherwise
     */

    override fun assignBrushings(
        brushings: List<Brushing>,
        profileInternal: ProfileInternal
    ): Single<Boolean> {
        return brushingApiManager.assignBrushings(
            profileInternal.accountId.toLong(),
            profileInternal.id,
            brushings
        )
    }

    // Synchronize brushing data for a profile
    override fun synchronizeBrushing(accountId: Long, profileId: Long): Single<Boolean> {
        val currentDate = TrustedClock.getNowLocalDate()
        val oneMonthBeforeDate = currentDate.minusMonths(1)

        return createBrushingFromNonSyncBrushing(accountId, profileId)
            .andThen(fetchRemoteBrushings(accountId, profileId, oneMonthBeforeDate, currentDate))
            .flatMap { brushingsDatastore.getDeletedLocally() }
            .flatMap {
                if (it.isNotEmpty()) {
                    deleteBrushings(it, accountId, profileId)
                } else {
                    Single.just(false)
                }
            }
    }

    override fun fetchRemoteBrushings(
        accountId: Long,
        profileId: Long,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        limit: Int?
    ): Single<Unit> =
        brushingApiManager.getBrushingsInDateRange(
            accountId,
            profileId,
            fromDate,
            toDate,
            limit
        )
            .map { result -> saveNonSyncBrushingsLocally(result) }

    override fun fetchRemoteBrushings(
        accountId: Long,
        profileId: Long,
        beforeBrushing: Brushing,
        limit: Int
    ): Single<Unit> =
        brushingApiManager.getBrushingsOlderThanBrushing(
            accountId,
            profileId,
            beforeBrushing,
            limit
        )
            .map { result -> saveNonSyncBrushingsLocally(result) }

    /**
     * Compare the brushings from the server and the one locally and then save locally the one
     * not stored for a given user
     */
    private fun saveNonSyncBrushingsLocally(result: BrushingsResponse) {
        result.getBrushings().filter {
            val exist = !brushingsDatastore.exists(it)
            exist
        }.map {
            brushingsDatastore.addBrushingIfDoNotExist(it)
            it
        }.toList().also { newBrushings ->
            scope.launch {
                localBrushingsProcessor.onBrushingsCreated(newBrushings.map { it.extractBrushing() })
            }
        }
    }

    /**
     * Add on the server the local brushing data that has not been sync with the server
     * for a given user
     */
    @VisibleForTesting
    fun createBrushingFromNonSyncBrushing(accountId: Long, profileId: Long): Completable =
        brushingsDatastore.getNonSynchronizedBrushing(profileId)
            .flatMapCompletable {
                if (it.isNotEmpty()) {
                    pushNonSynchronizedBrushingSessions(accountId, profileId, it)
                        .andThen(brushingsDatastore.clearNonSynchronized(profileId))
                } else {
                    Completable.complete()
                }
            }

    @VisibleForTesting
    fun pushNonSynchronizedBrushingSessions(
        accountId: Long,
        profileId: Long,
        brushingSessions: List<BrushingInternal>
    ): Completable = brushingApiManager
        .createBrushings(accountId, profileId, brushingSessions)
        .map { response ->
            response.getBrushings().map {
                brushingsDatastore.updateBrushing(it)
            }
        }
        .ignoreElement()

    /**
     * Deletes the specified brushings
     *
     * If the remote request succeeds, we delete them from our DB
     *
     * @return true if request succeeded, false otherwise
     */
    @VisibleForTesting
    fun deleteBrushings(brushings: List<Brushing>, accountId: Long, profileId: Long) =
        brushingApiManager.deleteBrushings(accountId, profileId, brushings)
            .map { success ->
                brushingsDatastore.delete(brushings).blockingAwait()
                success
            }

    /**
     * Store inside of ROOM
     * Encapsulate the implement of the brushingDatastore here, in case in the future
     * and make the app totally transparent about where the data is stored (Room,Ws... )
     * Delegate the task straight away to the brushingDatastore. Should not do anything else
     *
     */

    override fun countBrushingsSince(startTime: OffsetDateTime, profileId: Long) =
        brushingsDatastore.countBrushingsSince(startTime, profileId)

    override fun getNonDeletedBrushings(): Flowable<List<Brushing>> =
        brushingsDatastore.getNonDeletedBrushings().map {
            it.map { brushingInternal ->
                brushingInternal.extractBrushing()
            }
        }

    override fun countBrushings(gameId: String, profileId: Long) =
        brushingsDatastore.countBrushings(gameId, profileId)

    override fun countBrushings(profileId: Long) = brushingsDatastore.countBrushings(profileId)

    override fun getBrushingsSince(startTime: OffsetDateTime, profileId: Long) =
        brushingsDatastore.getBrushingsSince(startTime, profileId)

    override fun getBrushingsBetween(
        begin: OffsetDateTime,
        end: OffsetDateTime,
        profileId: Long
    ): Single<List<Brushing>> =
        brushingsDatastore.getBrushingsBetween(begin, end, profileId)
            .map { list -> list.map { it.extractBrushing() } }

    override fun getFirstBrushingSession(profileId: Long) =
        brushingsDatastore.getFirstBrushingSession(profileId)

    override fun getFirstBrushingSessionFlowable(profileId: Long): Flowable<Brushing> =
        brushingsDatastore.getFirstBrushingSessionFlowable(profileId).distinctUntilChanged()

    override fun getLastBrushingSession(profileId: Long) =
        brushingsDatastore.getLastBrushingSession(profileId)

    override fun getLastBrushingSessionFlowable(profileId: Long): Flowable<Brushing> =
        brushingsDatastore.getLastBrushingSessionFlowable(profileId).distinctUntilChanged()

    override fun deleteAll() = brushingsDatastore.deleteAll()

    override fun deleteBrushing(accountId: Long, profileId: Long, brushing: Brushing): Completable {
        return brushingsDatastore.deleteLocally(brushing.dateTime)
            .doOnComplete {
                Timber.d("Launching global scope %s", Thread.currentThread().name)
                scope.launch {
                    Timber.d(
                        "Running global scope %s on %s",
                        Thread.currentThread().name,
                        localBrushingsProcessor
                    )
                    localBrushingsProcessor.onBrushingRemoved(brushing)
                }
            }
            .andThen(
                when {
                    brushing.kolibreeId == null || brushing.kolibreeId == 0L
                    -> brushingsDatastore.deleteByDateTime(brushing.dateTime)
                    else -> brushingApiManager.deleteBrushing(
                        accountId,
                        profileId,
                        brushing.kolibreeId
                    )
                        .doOnSuccess { success ->
                            if (success) {
                                brushingsDatastore.deleteByDateTime(brushing.dateTime)
                                    .blockingAwait()
                            }
                        }.ignoreElement()
                }
            )
            .doFinally { Timber.d("do finally %s", Thread.currentThread().name) }
    }
}
