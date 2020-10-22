/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration

import com.kolibree.account.utils.ToothbrushForgetter
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.mocks.createAccountInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.Test

class UnpairMultiToothbrushPerProfileMigrationTest : BaseUnitTest() {

    private val appConfiguration: AppConfiguration = mock()

    private val toothbrushRepository: ToothbrushRepository = mock()
    private val accountDatastore: AccountDatastore = mock()
    private val toothbrushForgetter: ToothbrushForgetter = mock()

    private lateinit var unpairMultiToothbrushPerProfileMigration: UnpairMultiToothbrushPerProfileMigration

    override fun setup() {

        unpairMultiToothbrushPerProfileMigration = UnpairMultiToothbrushPerProfileMigration(
            appConfiguration,
            toothbrushRepository,
            accountDatastore,
            toothbrushForgetter
        )
    }

    @Test
    fun `getMigrationCompletable should deleted the shared toothbrush if there is only one toothbrush in the list and this toothbrush is shared`() {
        val accountId: Long = 123
        val ownerProfileId: Long = 456
        val toothbrush = AccountToothbrush(
            "mac", "name", CONNECT_B1, accountId,
            profileId = SHARED_MODE_PROFILE_ID
        )
        val toothbrushes = listOf(toothbrush)

        whenever(appConfiguration.isMultiToothbrushesPerProfileEnabled).thenReturn(false)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(createAccountInternal(id = accountId, ownerProfileId = ownerProfileId))
        )
        whenever(toothbrushRepository.getAccountToothbrushes(accountId))
            .thenReturn(toothbrushes)
        whenever(toothbrushForgetter.eraseToothbrushes(any()))
            .thenReturn(Completable.complete())

        unpairMultiToothbrushPerProfileMigration.getMigrationCompletable()
            .test()
            .assertComplete()

        verify(toothbrushForgetter).eraseToothbrushes(toothbrushes)
    }

    @Test
    fun `getMigrationCompletable should unpair all the shared toothbrushes if there are more than one`() {
        val accountId: Long = 123
        val ownerProfileId: Long = 456
        val toothbrush = AccountToothbrush(
            "mac", "name", CONNECT_B1, accountId,
            profileId = SHARED_MODE_PROFILE_ID
        )

        val toothbrush2 = AccountToothbrush(
            "mac", "name", CONNECT_E2, accountId,
            profileId = SHARED_MODE_PROFILE_ID
        )
        val toothbrushes = listOf(toothbrush, toothbrush2)

        whenever(appConfiguration.isMultiToothbrushesPerProfileEnabled).thenReturn(false)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(createAccountInternal(id = accountId, ownerProfileId = ownerProfileId))
        )
        whenever(toothbrushRepository.getAccountToothbrushes(accountId))
            .thenReturn(toothbrushes)
            .thenReturn(listOf())

        whenever(toothbrushForgetter.eraseToothbrushes(any()))
            .thenReturn(Completable.complete())

        unpairMultiToothbrushPerProfileMigration.getMigrationCompletable()
            .test()
            .assertComplete()

        verify(toothbrushForgetter).eraseToothbrushes(toothbrushes)
    }

    @Test
    fun `getMigrationCompletable should unpair the toothbrushes of each profiles if the profiles contains more than on toothbrushes`() {
        val accountId: Long = 123
        val profileId1: Long = 2
        val profileId2: Long = 4
        val profileId3: Long = 6

        val toothbrush1OfProfile1 =
            AccountToothbrush("", "", CONNECT_B1, accountId, profileId = profileId1)
        val toothbrush2OfProfile1 =
            AccountToothbrush("", "", CONNECT_B1, accountId, profileId = profileId1)

        val toothbrush1OfProfile2 =
            AccountToothbrush("", "", CONNECT_B1, accountId, profileId = profileId2)

        val toothbrush1OfProfile3 =
            AccountToothbrush("", "", CONNECT_B1, accountId, profileId = profileId3)
        val toothbrush2OfProfile3 =
            AccountToothbrush("", "", CONNECT_B1, accountId, profileId = profileId3)

        val toothbrushes = listOf(
            toothbrush1OfProfile1,
            toothbrush2OfProfile1,
            toothbrush1OfProfile2,
            toothbrush1OfProfile3,
            toothbrush2OfProfile3
        )

        whenever(appConfiguration.isMultiToothbrushesPerProfileEnabled).thenReturn(false)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(
            Maybe.just(createAccountInternal(id = accountId, ownerProfileId = profileId1))
        )
        whenever(toothbrushRepository.getAccountToothbrushes(accountId))
            .thenReturn(toothbrushes)
        whenever(toothbrushForgetter.eraseToothbrushes(any()))
            .thenReturn(Completable.complete())

        unpairMultiToothbrushPerProfileMigration.getMigrationCompletable()
            .test()
            .assertComplete()

        // Verify that all the toothbrushes has been unlinked
        // except for profile 2 that have only one TB
        verify(toothbrushForgetter).eraseToothbrushes(listOf(
            toothbrush1OfProfile1,
            toothbrush2OfProfile1,
            toothbrush1OfProfile3,
            toothbrush2OfProfile3
        ))
    }

    @Test
    fun `getMigrationCompletable should complete with no interaction if multi-tb is enabled`() {
        whenever(appConfiguration.isMultiToothbrushesPerProfileEnabled).thenReturn(true)

        unpairMultiToothbrushPerProfileMigration.getMigrationCompletable()
            .test()
            .assertComplete()

        verifyZeroInteractions(toothbrushForgetter, accountDatastore, toothbrushRepository)
    }
}
