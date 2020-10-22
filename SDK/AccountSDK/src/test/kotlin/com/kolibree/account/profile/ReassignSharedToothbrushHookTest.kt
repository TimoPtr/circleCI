/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.profile

import com.kolibree.account.AccountFacade
import com.kolibree.account.toAccount
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.android.test.mocks.createProfileInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class ReassignSharedToothbrushHookTest {

    private val accountFacade: AccountFacade = mock()
    private val serviceProvider: ServiceProvider = mock()
    private val toothbrushRepository: ToothbrushRepository = mock()

    private val hook = ReassignSharedToothbrushHook(accountFacade, serviceProvider, toothbrushRepository)

    @Test
    fun `onProfileDeleted doesn't reassign connections if there's more than 1 profile left`() {
        prepareAccountWithProfiles(listOf(createProfile(), createProfile(87L)))

        hook.onProfileDeleted(1L).test().assertComplete()

        verifyNoMoreInteractions(serviceProvider)
    }

    @Test
    fun `onProfileDeleted doesn't reassign connections if there are no connections`() {
        prepareAccountWithProfiles(listOf(createProfile()))

        prepareConnections()

        hook.onProfileDeleted(1L).test().assertComplete()
    }

    @Test
    fun `onProfileDeleted doesn't reassign connections if there's no shared connection`() {
        val ownerProfile = createProfile()
        prepareAccountWithProfiles(listOf(ownerProfile))

        val connections = listOf(
            KLTBConnectionBuilder.createAndroidLess()
                .withOwnerId(ownerProfile.id)
                .build(),
            KLTBConnectionBuilder.createAndroidLess()
                .withOwnerId(ownerProfile.id)
                .build()
        )
        prepareConnections(connections)

        hook.onProfileDeleted(1L).test().assertComplete()

        connections.forEach {
            verify(it.userMode(), never()).setProfileId(any())
        }
    }

    @Test
    fun `onProfileDeleted reassigns shared connections if there's 1 profile left`() {
        val ownerProfile = createProfile()
        prepareAccountWithProfiles(listOf(ownerProfile))

        val sharedConnection1 = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .withSupportForSetOperationsOnUserMode()
            .build()

        val sharedConnection2 = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .withSupportForSetOperationsOnUserMode()
            .build()

        val nonSharedConnection = KLTBConnectionBuilder.createAndroidLess()
            .withOwnerId(ownerProfile.id)
            .build()

        prepareConnections(listOf(sharedConnection1, sharedConnection2, nonSharedConnection))

        prepareAssociateToothbrush()

        hook.onProfileDeleted(1L).test().assertComplete()

        verify(nonSharedConnection.userMode(), never()).setProfileId(any())
        verify(sharedConnection1.userMode()).setProfileId(ownerProfile.id)
        verify(sharedConnection2.userMode()).setProfileId(ownerProfile.id)
    }

    @Test
    fun `onProfileDeleted associates shared connections to new profile`() {
        val ownerProfile = createProfile()
        prepareAccountWithProfiles(listOf(ownerProfile))

        val sharedConnection1 = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .withSupportForSetOperationsOnUserMode()
            .build()

        val sharedConnection2 = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .withSupportForSetOperationsOnUserMode()
            .build()

        val nonSharedConnection = KLTBConnectionBuilder.createAndroidLess()
            .withOwnerId(ownerProfile.id)
            .build()

        prepareConnections(listOf(sharedConnection1, sharedConnection2, nonSharedConnection))

        prepareAssociateToothbrush()

        hook.onProfileDeleted(1L).test().assertComplete()

        verify(toothbrushRepository, never()).associate(nonSharedConnection.toothbrush(), ownerProfile.id, ownerProfile.accountId.toLong())

        verify(toothbrushRepository).associate(sharedConnection1.toothbrush(), ownerProfile.id, ownerProfile.accountId.toLong())
        verify(toothbrushRepository).associate(sharedConnection2.toothbrush(), ownerProfile.id, ownerProfile.accountId.toLong())
    }

    /*
    Utils
     */
    fun prepareAccountWithProfiles(profiles: List<ProfileInternal> = listOf()) {
        val account = createAccountInternal(profiles = profiles)

        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(account.toAccount()))
    }

    fun createProfile(profileId: Long = ProfileBuilder.DEFAULT_ID): ProfileInternal {
        return createProfileInternal(id = profileId)
    }

    fun prepareConnections(connections: List<KLTBConnection> = listOf()) {
        val service = mock<KolibreeService>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))

        whenever(service.knownConnections).thenReturn(connections)
    }

    private fun prepareAssociateToothbrush() {
        whenever(toothbrushRepository.associate(any<Toothbrush>(), any(), any()))
            .thenReturn(Completable.complete())
    }
}
