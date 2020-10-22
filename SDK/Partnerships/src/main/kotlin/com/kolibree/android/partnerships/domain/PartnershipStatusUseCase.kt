/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.domain

import android.annotation.SuppressLint
import com.kolibree.android.accountinternal.CurrentAccountAndProfileIdsProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.data.PartnershipStatusRepository
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@VisibleForApp
interface PartnershipStatusUseCase {

    /**
     * Re-fetches data for all registered partnerships from the API and store them
     * in respective databases.
     *
     * Please note that this stream does not allow to retrieve refreshed data. To do that,
     * please use [getPartnershipStatusStream].
     *
     * @return [Completable] which completes when data re-fetching process ends.
     */
    fun refreshPartnershipData(): Completable

    /**
     * Subscribes for status updates of particular [Partner] for current profile.
     *
     * Stream reacts on active profile changes and can complete, normally on logout.
     *
     * @param partner signature of partner we're interested
     *
     * @return [Flowable] with stream of partnership statuses.
     */
    fun getPartnershipStatusStream(partner: Partner): Flowable<PartnershipStatus>
}

internal class PartnershipStatusUseCaseImpl @Inject constructor(
    private val provider: CurrentAccountAndProfileIdsProvider,
    private val repository: PartnershipStatusRepository
) : PartnershipStatusUseCase {

    @SuppressLint("ExperimentalClassUse")
    override fun refreshPartnershipData(): Completable =
        provider.currentAccountAndProfileIdsStream()
            .subscribeOn(Schedulers.io())
            .switchMapCompletable { (accountId, profileId) ->
                repository.refreshPartnerships(accountId, profileId)
            }

    @SuppressLint("ExperimentalClassUse")
    override fun getPartnershipStatusStream(partner: Partner): Flowable<PartnershipStatus> =
        provider.currentAccountAndProfileIdsStream()
            .subscribeOn(Schedulers.io())
            .switchMap { (accountId, profileId) ->
                repository.getPartnershipStatus(accountId, profileId, partner)
            }
            .distinctUntilChanged()
}

@VisibleForApp
object NoOpPartnershipStatusUseCase : PartnershipStatusUseCase {

    override fun refreshPartnershipData(): Completable =
        Completable.complete()

    override fun getPartnershipStatusStream(partner: Partner): Flowable<PartnershipStatus> =
        Flowable.empty()
}
