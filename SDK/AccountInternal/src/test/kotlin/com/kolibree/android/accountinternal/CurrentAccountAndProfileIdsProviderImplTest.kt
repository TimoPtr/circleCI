/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.models.AccountAndProfileIds
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.BehaviorProcessor
import org.junit.Test

class CurrentAccountAndProfileIdsProviderImplTest : BaseUnitTest() {

    private val accountStream = BehaviorProcessor.create<AccountInternal>()

    private val profileStream = BehaviorProcessor.create<Profile>()

    private lateinit var provider: CurrentAccountAndProfileIdsProvider

    override fun setup() {
        super.setup()

        val accountDatastore: AccountDatastore = mock()
        val currentProfileProvider: CurrentProfileProvider = mock()
        whenever(accountDatastore.accountFlowable()).thenReturn(accountStream)
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileStream)

        provider = CurrentAccountAndProfileIdsProviderImpl(
            accountDatastore, currentProfileProvider
        )
    }

    @Test
    fun `wait with streams if IDs are not available`() {
        provider.currentAccountAndProfileIdsStream().test().assertNoValues().assertNotComplete()
        provider.currentAccountAndProfileIdsSingle().test().assertNoValues().assertNotComplete()
    }

    @Test
    fun `propagate IDs if they're available`() {
        mockAccountWithId(1)
        mockProfileWithId(10)

        val expected = AccountAndProfileIds(1, 10)

        provider.currentAccountAndProfileIdsStream().test().assertValue(expected)
        provider.currentAccountAndProfileIdsSingle().test().assertValue(expected)
    }

    @Test
    fun `propagate change only if one of IDs changes`() {
        mockAccountWithId(1)
        mockProfileWithId(10)

        val observer = provider.currentAccountAndProfileIdsStream().test()

        mockAccountWithId(1)
        mockProfileWithId(10)

        mockAccountWithId(2)
        mockProfileWithId(10)

        mockAccountWithId(2)
        mockProfileWithId(10)

        mockAccountWithId(2)
        mockProfileWithId(20)

        mockAccountWithId(2)
        mockProfileWithId(20)

        observer.assertValues(
            AccountAndProfileIds(1, 10),
            AccountAndProfileIds(2, 10),
            AccountAndProfileIds(2, 20)
        )
    }

    @Test
    fun `react on account change`() {
        mockProfileWithId(10)

        run {
            mockAccountWithId(1)

            val expected = AccountAndProfileIds(1, 10)

            provider.currentAccountAndProfileIdsStream().test().assertValue(expected)
            provider.currentAccountAndProfileIdsSingle().test().assertValue(expected)
        }

        run {
            mockAccountWithId(2)

            val expected = AccountAndProfileIds(2, 10)

            provider.currentAccountAndProfileIdsStream().test().assertValue(expected)
            provider.currentAccountAndProfileIdsSingle().test().assertValue(expected)
        }
    }

    @Test
    fun `react on profile change`() {
        mockAccountWithId(1)

        run {
            mockProfileWithId(10)

            val expected = AccountAndProfileIds(1, 10)

            provider.currentAccountAndProfileIdsStream().test().assertValue(expected)
            provider.currentAccountAndProfileIdsSingle().test().assertValue(expected)
        }

        run {
            mockProfileWithId(20)

            val expected = AccountAndProfileIds(1, 20)

            provider.currentAccountAndProfileIdsStream().test().assertValue(expected)
            provider.currentAccountAndProfileIdsSingle().test().assertValue(expected)
        }
    }

    @Test
    fun `terminate streams if profile stream terminates`() {
        mockAccountWithId(1)
        mockProfileWithId(10)

        run {
            val expected = AccountAndProfileIds(1, 10)
            provider.currentAccountAndProfileIdsStream().test().assertValue(expected)
            provider.currentAccountAndProfileIdsSingle().test().assertValue(expected)
        }

        run {
            profileStream.onComplete()
            provider.currentAccountAndProfileIdsStream().test()
                .assertComplete()
                .assertNoValues()
            provider.currentAccountAndProfileIdsSingle().test()
                .assertError(NoSuchElementException::class.java)
        }
    }

    private fun mockAccountWithId(id: Long) {
        val accountInternal = AccountInternal(id)
        accountStream.onNext(accountInternal)
    }

    private fun mockProfileWithId(id: Long) {
        profileStream.onNext(ProfileBuilder.create().withId(id).build())
    }
}
