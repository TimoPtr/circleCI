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
import io.reactivex.Completable
import javax.inject.Inject

@VisibleForApp
interface DisablePartnershipUseCase {
    /**
     * Disables partnership specified in [partner] for current active profile
     *
     * Once disable completes successfully, it automatically refreshes Partnership status
     *
     * Internet is required for this operation to complete. Offline mode is not supported.
     *
     * @return [Completable] which completes when partnership is disabled and partnership refreshed
     */
    fun disableCompletable(partner: Partner): Completable
}

internal class DisablePartnershipUseCaseImpl
@Inject constructor(
    private val repository: PartnershipStatusRepository,
    private val accountInfoProvider: CurrentAccountAndProfileIdsProvider
) : DisablePartnershipUseCase {
    @SuppressLint("ExperimentalClassUse")
    override fun disableCompletable(partner: Partner): Completable {
        return accountInfoProvider.currentAccountAndProfileIdsSingle()
            .flatMapCompletable {
                repository.disablePartnership(
                    accountId = it.accountId,
                    profileId = it.profileId,
                    partner = partner
                )
            }
    }
}
