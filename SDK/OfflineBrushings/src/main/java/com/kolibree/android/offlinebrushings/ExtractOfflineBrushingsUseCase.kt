/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.offlineBrushingsTagFor
import com.kolibree.android.offlinebrushings.ExtractionProgress.Companion.MAX_PROGRESS
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncObservableInternal
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.OfflineBrushingConsumer
import com.kolibree.android.sdk.connection.isActive
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.core.KLTBConnectionPool
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Provider
import timber.log.Timber

@Suppress("DataClassPrivateConstructor")
@Keep
data class ExtractionProgress private constructor(
    val brushingsSynced: List<BrushingSyncedResult>,
    val totalBrushings: Int,
    /**
     * true if extraction is completed, false is extraction is ongoing
     *
     * We might discard some of the stored offline brushings. Progress can be flagged as finished
     * even with brushingsSynced/totalBrushings < [MAX_PROGRESS]
     */
    val isFinished: Boolean
) {

    val progress: Float? = when {
        totalBrushings == 0 -> null
        totalBrushings > 0 && isFinished -> MAX_PROGRESS
        else -> brushingsSynced.size.toFloat() / totalBrushings.toFloat()
    }

    /**
     * true if extraction is completed and we synced at least one brushing
     */
    val isSuccess: Boolean = isFinished && brushingsSynced.isNotEmpty()

    fun withCompleted() = copy(isFinished = true)

    companion object {
        fun empty() = ExtractionProgress(
            brushingsSynced = emptyList(),
            totalBrushings = 0,
            isFinished = false
        )

        fun withBrushingProgress(
            brushingsSynced: List<BrushingSyncedResult>,
            totalBrushings: Int
        ) = ExtractionProgress(
            brushingsSynced = brushingsSynced,
            totalBrushings = totalBrushings,
            isFinished = false
        )

        const val MAX_PROGRESS = 1F
    }
}

@Keep
interface ExtractOfflineBrushingsUseCase {
    fun extractOfflineBrushings(): Observable<ExtractionProgress>
}

/**
 * Receives and persists offline brushings from toothbrushes
 *
 * It relies on [KLTBConnectionPool] to avoid showing toothbrush icon notification when it's
 * doing job in the background
 */
internal class ExtractOfflineBrushingsUseCaseImpl @Inject constructor(
    private val connector: IKolibreeConnector,
    private val connectionPool: KLTBConnectionPool,
    private val offlineBrushingConsumerProvider: Provider<MultiConnectionOfflineBrushingConsumer>,
    private val lastSyncObservable: LastSyncObservableInternal
) : ExtractOfflineBrushingsUseCase {

    /**
     * [KLTBConnectionPool.init] must have been invoked prior to invoking this method, and
     * reasonable time must have passed so that all [KLTBConnection] have had the time to connect
     *
     * If there's no [KLTBConnection] [ACTIVE], the returned [Observable] will complete immediately
     *
     * The last emitted [ExtractionProgress] will report [ExtractionProgress.isFinished] = true
     *
     * @return [Observable] that emits a [ExtractionProgress] for every offlineBrushing Sync and completes
     */
    override fun extractOfflineBrushings(): Observable<ExtractionProgress> {
        // see https://github.com/kolibree-git/android-monorepo/pull/1011#discussion_r445506695
        val extractOfflineBrushingsObservable = extractProgressStreamAndComplete()
            .onTerminateDetach()
            .cache()
            .onTerminateDetach()

        /*
        After extractOfflineBrushingsObservable completes, we want to emit the last value and
        flag it as finished
         */
        return extractOfflineBrushingsObservable
            .concatWith(
                extractOfflineBrushingsObservable.lastElement()
                    .map { it.withCompleted() }
                    .toObservable()
            )
    }

    private fun extractProgressStreamAndComplete(): Observable<ExtractionProgress> {
        val offlineBrushingConsumer = offlineBrushingConsumerProvider.get()

        return Observable.combineLatest(
            getSyncedBrushings(offlineBrushingConsumer),
            registerOfflineBrushingsConsumer(offlineBrushingConsumer).toObservable(),
            BiFunction<List<BrushingSyncedResult>, Int, ExtractionProgress> { syncedBrushings, total ->
                ExtractionProgress.withBrushingProgress(syncedBrushings, total)
            })
    }

    /**
     * Emits a new list composed of the past values and the new value at the end of the list
     */
    @VisibleForTesting
    fun getSyncedBrushings(
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Observable<List<BrushingSyncedResult>> {
        return offlineBrushingConsumer.profileSyncedOfflineBrushings()
            .scan(mutableListOf<BrushingSyncedResult>(),
                { accumulator, brushingSyncedResult ->
                    accumulator.apply { add(brushingSyncedResult) }
                })
            .map { it.toList() }
    }

    /**
     * Attempts to load stored brushings from all known connections in [KLTBConnectionPool]
     * @return [Single]<[Int]> that gives the total number of brushings that will be sync across all connections
     */
    @VisibleForTesting
    fun registerOfflineBrushingsConsumer(
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Single<Int> =
        maybeLoadStoredBrushings(connectionPool.getKnownConnections(), offlineBrushingConsumer)

    /**
     * @return [Single]<[Int]> which is the sum of all available offline brushing across all connections
     * it registers [offlineBrushingConsumer] on every [KLTBConnection] that meets the conditions
     *
     * Then, it notifies [offlineBrushingConsumer] that all [KLTBConnection] has been registered
     */
    @VisibleForTesting
    fun maybeLoadStoredBrushings(
        connections: List<KLTBConnection>,
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Single<Int> = Single.mergeDelayError(
        connections.map { connection ->
            maybeLoadStoredBrushingsFromConnection(connection, offlineBrushingConsumer)
                .onErrorReturn {
                    Timber.tag(TAG).w(
                        it,
                        "error while retrieving the number of offlineBrushing of ${
                            connection.toothbrush()
                                .getName()
                        } (${connection.toothbrush().mac})"
                    )

                    0
                }
        }).reduce(0) { accumulator, value -> accumulator + value }
        .subscribeOn(Schedulers.io())

    /**
     * @return [Single]<[Int]> that will emit the number of offline brushing, stored in this toothbrush.
     * It registers [offlineBrushingConsumer] on [KLTBConnection] if it
     * meets the conditions (toothbrush support offline brushing).
     * If it doesn't, returns a [Single]<[Int]> 0
     */
    @VisibleForTesting
    fun maybeLoadStoredBrushingsFromConnection(
        connection: KLTBConnection,
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ) = readFromKLTBConnection(connection, offlineBrushingConsumer)

    /**
     * If [connection] contains offline brushings, subscribes the instance as [OfflineBrushingConsumer]
     *
     * @return [Single]<[Int]> that will emit the number of offline brushing, or on error if something wrong happened.
     */
    @VisibleForTesting
    fun readFromKLTBConnection(
        connection: KLTBConnection,
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Single<Int> {
        return shouldLoadOfflineBrushings(connection)
            .doOnSuccess { shouldLoad -> Timber.tag(TAG).d("Should load %s", shouldLoad) }
            .flatMap { shouldLoadOfflineBrushings ->
                if (shouldLoadOfflineBrushings) {
                    countAndMaybeFetchOfflineBrushings(connection, offlineBrushingConsumer)
                } else {
                    Single.just(0)
                }
            }
            .doOnSuccess { numberOfBrushings ->
                if (numberOfBrushings == 0) {
                    lastSyncObservable.send(LastSyncDate.now(connection.toothbrush().mac))
                }
            }
            .onErrorReturnItem(0)
    }

    private fun countAndMaybeFetchOfflineBrushings(
        connection: KLTBConnection,
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Single<Int> {
        return getRecordCount(connection)
            .flatMap { numberOfBrushings ->
                maybeFetchOfflineBrushings(numberOfBrushings, connection, offlineBrushingConsumer)
                    .andThen(Single.just(numberOfBrushings))
            }
    }

    private fun maybeFetchOfflineBrushings(
        numberOfBrushings: Int,
        connection: KLTBConnection,
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Completable {
        return if (numberOfBrushings > 0) {
            fetchOfflineBrushings(connection, offlineBrushingConsumer)
        } else {
            Completable.complete()
        }
    }

    private fun getRecordCount(connection: KLTBConnection): Single<Int> =
        connection.brushing().recordCount
            .doOnError { Timber.tag(TAG).w(it, "Error counting records in brushing") }
            .doOnSuccess { count -> Timber.tag(TAG).d("Number of records %s", count) }

    @VisibleForTesting
    fun fetchOfflineBrushings(
        connection: KLTBConnection,
        offlineBrushingConsumer: MultiConnectionOfflineBrushingConsumer
    ): Completable {
        return Completable.fromAction {
            /*
            in current implementation, this is a fire&forget operation that'll start a thread to
            pull records

            The fact that it completes doesn't mean that records have been pulled
             */
            connection
                .brushing()
                .pullRecords(offlineBrushingConsumer)
        }
    }

    /**
     * @return [Single]<[Boolean]> that will emit true if the toothbrush is in shared mode or
     * the owner of the toothbrush is known by the logged in account
     */
    @VisibleForTesting
    fun shouldLoadOfflineBrushings(connection: KLTBConnection): Single<Boolean> {
        return if (connection.toothbrush().isRunningBootloader || !connection.isActive()) {
            Timber.tag(TAG).d(
                "Connection is running bootloader (%s)",
                connection.toothbrush().isRunningBootloader
            )
            Single.just(false)
        } else {
            connection
                .userMode()
                .profileOrSharedModeId()
                .map(::isValidOwnerId)
                .doOnError { Timber.tag(TAG).w(it, "Error in shouldLoadOfflineBrushings") }
        }
    }

    private fun isValidOwnerId(ownerId: Long): Boolean {
        Timber.tag(TAG).d(
            "ownerId: $ownerId; knows %s",
            connector.doesCurrentAccountKnow(ownerId)
        )

        return ownerId == SHARED_MODE_PROFILE_ID || connector.doesCurrentAccountKnow(ownerId)
    }

    companion object {

        private val TAG = offlineBrushingsTagFor(ExtractOfflineBrushingsUseCaseImpl::class)
    }
}
