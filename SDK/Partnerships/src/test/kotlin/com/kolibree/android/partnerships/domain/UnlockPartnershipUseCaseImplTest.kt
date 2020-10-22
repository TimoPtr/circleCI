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
import com.kolibree.android.accountinternal.profile.models.AccountAndProfileIds
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.partnerships.data.PartnershipStatusRepository
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.test.extensions.assertHasObserversAndComplete
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import org.junit.Test

internal class UnlockPartnershipUseCaseImplTest : BaseUnitTest() {
    private val repository: PartnershipStatusRepository = mock()
    private val accountInfoProvider: CurrentAccountAndProfileIdsProvider = mock()

    private val useCase = UnlockPartnershipUseCaseImpl(
        repository = repository,
        accountInfoProvider = accountInfoProvider
    )

    @Test
    fun `unlockCompletable subscribes to unlockPartnership after accountInfoProvider emits a value`() {
        val expectedAccountId = 1L
        val expectedProfileId = 2L
        whenever(accountInfoProvider.currentAccountAndProfileIdsSingle())
            .thenReturn(
                Single.just(
                    AccountAndProfileIds(
                        accountId = expectedAccountId,
                        profileId = expectedProfileId
                    )
                )
            )

        val expectedPartner = Partner.HEADSPACE

        val unlockSubject = CompletableSubject.create()
        whenever(
            repository.unlockPartnership(
                expectedAccountId,
                expectedProfileId,
                expectedPartner
            )
        )
            .thenReturn(unlockSubject)

        val observer = useCase.unlockCompletable(expectedPartner).test().assertNotComplete()

        unlockSubject.assertHasObserversAndComplete()

        observer.assertComplete()
    }
}
