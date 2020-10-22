/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.offlineBrushingsTagFor
import com.kolibree.android.offlinebrushings.persistence.OrphanBrushingRepository
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.LastSyncObservableInternal
import com.kolibree.android.offlinebrushings.sync.StartSync
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.OfflineBrushingConsumer
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Provider
import timber.log.Timber

/**
 * [OfflineBrushingConsumer] that supports listening to multiple [KLTBConnection] extracting
 * [OfflineBrushing]
 *
 * Subscribers to [MultiConnectionOfflineBrushingConsumer.profileSyncedOfflineBrushings] will
 * complete after all [KLTBConnection] have completed syncing
 *
 * It's responsible for creating Brushings and OrphanBrushings
 */
internal class MultiConnectionOfflineBrushingConsumer
@Inject constructor(
    context: Context,
    private val lastSyncObservable: LastSyncObservableInternal,
    private val connector: IKolibreeConnector,
    private val orphanBrushingRepository: OrphanBrushingRepository,
    private val dataMapperBuilderProvider: Provider<OfflineBrushingsDataMapper.Builder>,
    private val synchronizator: Synchronizator
) :
    OfflineBrushingConsumer {
    private val appContext: Context = context.applicationContext

    @VisibleForTesting
    @Volatile
    var expectedSyncs: Int = 0

    private val brushingsSyncedSubject = PublishSubject.create<BrushingSyncedResult>()

    /**
     * [Observable] that will emit multiple [BrushingSyncedResult] for each [KLTBConnection]
     * for which we have requested to extract offline brushings, even if it extracted 0 brushings.
     *
     * The [Observable] will complete once all extraction operations have completed
     *
     * Behavior is undefined for multiple subscribers
     *
     * Scheduler: [profileSyncedOfflineBrushings] emits by default on [Schedulers.io()]
     */
    fun profileSyncedOfflineBrushings(
        scheduler: Scheduler = Schedulers.io()
    ): Observable<BrushingSyncedResult> =
        brushingsSyncedSubject
            .observeOn(scheduler)
            .hide()

    override fun onNewOfflineBrushing(
        connection: KLTBConnection,
        offlineBrushing: OfflineBrushing,
        remaining: Int
    ): Boolean {
        try {
            val profileId = getToothbrushProfile(connection)
            val offlineMapper = buildOfflineMapper(profileId, offlineBrushing, connection)
            createBrushing(offlineMapper)
        } catch (ex: Exception) {
            Timber.e(ex)
        }

        return true
    }

    private fun getToothbrushProfile(connection: KLTBConnection): Long =
        extractOwnerId(connection).blockingGet()

    private fun buildOfflineMapper(
        profileId: Long,
        record: OfflineBrushing,
        connection: KLTBConnection
    ): OfflineBrushingsDataMapper =
        dataMapperBuilderProvider
            .get()
            .offlineBrushing(record)
            .isMultiUserMode(profileId == SHARED_MODE_PROFILE_ID) // See UserBaseImpl
            .userId(profileId)
            .toothbrushMac(connection.toothbrush().mac)
            .toothbrushSerial(connection.toothbrush().serialNumber)
            .build()

    @VisibleForTesting
    @WorkerThread
    internal fun createBrushing(offlineMapper: OfflineBrushingsDataMapper) {
        if (offlineMapper.containsOrphanBrushing()) {
            createOrphanBrushing(offlineMapper)
        } else {
            createOfflineBrushing(offlineMapper)
        }
    }

    @VisibleForTesting
    internal fun createOrphanBrushing(offlineMapper: OfflineBrushingsDataMapper) {
        /*
        For now, We don't want to send orphan brushings to the backend, see https://jira.kolibree.com/browse/KLTB002-1534
        */
        orphanBrushingRepository.insert(offlineMapper.createOrphanBrushing())
        brushingsSyncedSubject.onNext(
            OrphanBrushingSyncedResult(
                offlineMapper.ownerId(),
                offlineMapper.toothbrushMac,
                offlineMapper.datetime()
            )
        )
    }

    @VisibleForTesting
    @WorkerThread
    internal fun createOfflineBrushing(offlineMapper: OfflineBrushingsDataMapper) {
        val createBrushingData = createBrushingData(appContext, offlineMapper)
        val profileWrapper = connector.withProfileId(offlineMapper.ownerId())
        profileWrapper.createBrushingSync(createBrushingData)
        brushingsSyncedSubject.onNext(
            OfflineBrushingSyncedResult(
                offlineMapper.ownerId(),
                offlineMapper.toothbrushMac,
                offlineMapper.datetime()
            )
        )
    }

    @VisibleForTesting
    internal fun createBrushingData(
        context: Context,
        offlineMapper: OfflineBrushingsDataMapper
    ): CreateBrushingData {
        val appVersions = KolibreeAppVersions(context)
        return offlineMapper.createBrushingData(appVersions)
    }

    override fun onSuccess(connection: KLTBConnection, retrievedCount: Int) {
        lastSyncObservable.send(LastSyncDate.now(connection.toothbrush().mac))
    }

    /**
     * Returns the ownerId of the profile, or [SHARED_MODE_PROFILE_ID] if the toothbrush is in
     * shared mode
     */
    private fun extractOwnerId(connection: KLTBConnection) =
        connection
            .userMode()
            .profileOrSharedModeId()

    override fun onFailure(connection: KLTBConnection, failureReason: FailureReason) {
        // BrushingImpl hasn't invoked onSyncStart, so no need to decrement expectedSyncs
        Timber.tag(TAG).w(
            failureReason,
            "Failed extracting brushings from %s",
            connection.toothbrush().mac
        )
        completeIfNoSyncsPending()
    }

    override fun onSyncStart(connection: KLTBConnection) {
        synchronized(expectedSyncs) {
            expectedSyncs++
        }
        Timber.tag(TAG).d("onSyncStart expectedSyncs=$expectedSyncs")

        lastSyncObservable.send(StartSync(connection.toothbrush().mac))

        synchronizator.standBy()
    }

    override fun onSyncEnd(connection: KLTBConnection) {
        lastSyncObservable.send(LastSyncDate.now(connection.toothbrush().mac))

        synchronizator.resume()

        synchronized(expectedSyncs) {
            expectedSyncs--

            Timber.tag(TAG).d("onSyncEnd expectedSyncs=$expectedSyncs")
            completeIfNoSyncsPending()
        }
    }

    @VisibleForTesting
    fun completeIfNoSyncsPending() {
        val shouldComplete: Boolean = synchronized(expectedSyncs) { expectedSyncs == 0 }
        Timber.tag(TAG).d("completeIfNoSyncsPending shouldComplete=$shouldComplete")
        if (shouldComplete) brushingsSyncedSubject.onComplete()
    }

    companion object {

        private val TAG = offlineBrushingsTagFor(MultiConnectionOfflineBrushingConsumer::class)
    }
}
