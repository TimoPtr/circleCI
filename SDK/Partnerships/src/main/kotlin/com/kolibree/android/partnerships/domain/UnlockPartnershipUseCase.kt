/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.domain

import com.kolibree.android.accountinternal.CurrentAccountAndProfileIdsProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.data.PartnershipStatusRepository
import com.kolibree.android.partnerships.domain.model.Partner
import io.reactivex.Completable
import javax.inject.Inject

@VisibleForApp
interface UnlockPartnershipUseCase {
    fun unlockCompletable(partner: Partner): Completable
}

internal class UnlockPartnershipUseCaseImpl
@Inject constructor(
    private val repository: PartnershipStatusRepository,
    private val accountInfoProvider: CurrentAccountAndProfileIdsProvider
) : UnlockPartnershipUseCase {
    override fun unlockCompletable(partner: Partner): Completable {
        return accountInfoProvider.currentAccountAndProfileIdsSingle()
            .flatMapCompletable {
                repository.unlockPartnership(
                    accountId = it.accountId,
                    profileId = it.profileId,
                    partner = partner
                )
            }
    }
}
