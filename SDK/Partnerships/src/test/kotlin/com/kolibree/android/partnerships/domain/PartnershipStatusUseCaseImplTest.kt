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
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import org.junit.Test

class PartnershipStatusUseCaseImplTest : BaseUnitTest() {

    private val provider: CurrentAccountAndProfileIdsProvider = mock()

    private val repository: PartnershipStatusRepository = mock()

    private val useCase = PartnershipStatusUseCaseImpl(provider, repository)

    @Test
    fun `data refresh is triggered for current profile`() {
        val expectedAccount = 10L
        val expectedProfile = 1L

        whenever(provider.currentAccountAndProfileIdsStream())
            .thenReturn(Flowable.just(AccountAndProfileIds(expectedAccount, expectedProfile)))

        whenever(repository.refreshPartnerships(expectedAccount, expectedProfile))
            .thenReturn(Completable.complete())

        val testObserver = useCase.refreshPartnershipData().test()

        testObserver.assertComplete()
    }

    @Test
    fun `partnership data stream reacts on profile changes`() {
        val expectedAccount = 10L
        val profileIds = arrayOf(1L, 2L, 3L)

        val accountDataStream = BehaviorProcessor.create<AccountAndProfileIds>()

        whenever(provider.currentAccountAndProfileIdsStream())
            .thenReturn(accountDataStream)

        profileIds.forEach {
            whenever(repository.getPartnershipStatus(expectedAccount, it, Partner.HEADSPACE))
                .thenReturn(Flowable.just(HeadspacePartnershipStatus.Inactive(it)))
        }

        val testObserver = useCase.getPartnershipStatusStream(Partner.HEADSPACE).test()

        testObserver.assertNoValues()

        profileIds.forEach {
            accountDataStream.onNext(AccountAndProfileIds(expectedAccount, it))
        }

        testObserver.assertValues(
            *profileIds.map { HeadspacePartnershipStatus.Inactive(it) }.toTypedArray()
        )
        verify(repository, times(profileIds.size)).getPartnershipStatus(any(), any(), any())
    }

    @Test
    fun `partnership data stream filters out duplicated status values`() {
        val expectedAccount = 10L
        val expectedProfile = 1L
        val expectedStatus = HeadspacePartnershipStatus.Inactive(expectedProfile)

        whenever(provider.currentAccountAndProfileIdsStream())
            .thenReturn(Flowable.just(AccountAndProfileIds(expectedAccount, expectedProfile)))

        whenever(repository.getPartnershipStatus(expectedAccount, expectedProfile, Partner.HEADSPACE))
            .thenReturn(Flowable.fromArray(
                *(0..10).map { expectedStatus }.toTypedArray()
            ))

        val testObserver = useCase.getPartnershipStatusStream(Partner.HEADSPACE).test()

        testObserver.assertComplete()

        testObserver.assertValue(expectedStatus)
    }
}
