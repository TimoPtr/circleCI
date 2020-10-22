/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.toothbrush

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.mocks.BrushingBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.toAccountToothbrush
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.BehaviorSubject
import java.util.UUID
import org.junit.Test

class ToothbrushesForProfileUseCaseImplTest : BaseUnitTest() {
    private val toothbrushRepository: ToothbrushRepository = mock()
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val serviceProvider: ServiceProvider = mock()

    private val serviceConnectionsStream: PublishProcessor<List<KLTBConnection>> =
        PublishProcessor.create()

    private val toothbrushesForProfile =
        com.kolibree.account.utils.ToothbrushesForProfileUseCaseImpl(
            toothbrushRepository,
            currentProfileProvider,
            serviceProvider
        )

    /*
    currentProfileToothbrushesOnceAndStream
     */

    @Test
    fun `profileToothbrushesOnceAndStream emits empty list if profile can't access any of the toothbrushes`() {
        val profileId = BrushingBuilder.DEFAULT_PROFILE_ID

        val nonOwnedToothbrush1 = createKLTBConnection(profileId + 5)

        val nonOwnedToothbrush2 = createKLTBConnection(profileId + 10)

        val connections = listOf(nonOwnedToothbrush1, nonOwnedToothbrush2)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        toothbrushesForProfile.profileToothbrushesOnceAndStream(profileId).test()
            .assertValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `profileToothbrushesOnceAndStream emits toothbrushes owned by profileId as well as those shared`() {
        val profileId = BrushingBuilder.DEFAULT_PROFILE_ID

        val ownedToothbrush = createKLTBConnection(profileId)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(profileId + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        toothbrushesForProfile.profileToothbrushesOnceAndStream(profileId).test()
            .assertValue(listOf(ownedToothbrush, sharedToothbrush))
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `profileToothbrushesOnceAndStream emits new list after service reconnected`() {
        val profileId = BrushingBuilder.DEFAULT_PROFILE_ID

        val ownedToothbrush = createKLTBConnection(profileId)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(profileId + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        val serviceStateSubject = mockConnectedService(connections)

        val initialExpectedList = listOf(ownedToothbrush, sharedToothbrush)

        val observer = toothbrushesForProfile.profileToothbrushesOnceAndStream(profileId).test()
            .assertValueCount(1)
            .assertValue(initialExpectedList)

        val expectedList = listOf(ownedToothbrush)
        serviceStateSubject.onNext(mockServiceConnected(expectedList))

        observer
            .assertValueCount(2)
            .assertLastValue(expectedList)
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `profileToothbrushesOnceAndStream emits list after toothbrush repository emits new list`() {
        val profileId = BrushingBuilder.DEFAULT_PROFILE_ID

        val ownedToothbrush = createKLTBConnection(profileId)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(profileId + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        val accountToothbrushesSubject = mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        val initialExpectedList = listOf(ownedToothbrush, sharedToothbrush)

        val observer = toothbrushesForProfile.profileToothbrushesOnceAndStream(profileId).test()
            .assertValueCount(1)
            .assertValue(initialExpectedList)

        val newExpectedList = listOf(ownedToothbrush)
        accountToothbrushesSubject.onNext(newExpectedList.toAccountToothbrush())

        /*
         * We just want to test that a new item is emitted after toothbrushRepo emits
         */
        observer
            .assertValueCount(2)
            .assertLastValue(newExpectedList)
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `profileToothbrushesOnceAndStream emits list after service emits new list`() {
        val profileId = BrushingBuilder.DEFAULT_PROFILE_ID

        val ownedToothbrush = createKLTBConnection(profileId)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(profileId + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        val initialExpectedList = listOf(ownedToothbrush, sharedToothbrush)

        val observer = toothbrushesForProfile.profileToothbrushesOnceAndStream(profileId).test()
            .assertValueCount(1)
            .assertValue(initialExpectedList)

        val newExpectedList = listOf(ownedToothbrush)
        serviceConnectionsStream.onNext(newExpectedList)

        /*
         * We just want to test that a new item is emitted after toothbrushRepo emits
         */
        observer
            .assertValueCount(2)
            .assertLastValue(newExpectedList)
            .assertNotComplete()
            .assertNoErrors()
    }

    /*
    profileAccountToothbrushesOnceAndStream
     */

    @Test
    fun `profileAccountToothbrushesOnceAndStream emits empty list if profile can't access any of the toothbrushes`() {
        val profile = ProfileBuilder.create().build()

        val nonOwnedToothbrush1 = createKLTBConnection(profile.id + 5)

        val nonOwnedToothbrush2 = createKLTBConnection(profile.id + 10)

        val connections = listOf(nonOwnedToothbrush1, nonOwnedToothbrush2)

        mockToothbrushRepository(connections.toAccountToothbrush())

        toothbrushesForProfile.profileAccountToothbrushesOnceAndStream(profile.id).test()
            .assertValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `profileAccountToothbrushesOnceAndStream emits toothbrushes owned by active profile as well as those shared`() {
        val profile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(profile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(profile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        toothbrushesForProfile.profileAccountToothbrushesOnceAndStream(profile.id).test()
            .assertValue(listOf(ownedToothbrush, sharedToothbrush).toAccountToothbrush())
            .assertNotComplete()
            .assertNoErrors()
    }

    /*
    currentProfileAccountToothbrushesOnceAndStream
     */

    @Test
    fun `currentProfileAccountToothbrushesOnceAndStream emits empty list if profile can't access any of the toothbrushes`() {
        val activeProfile = ProfileBuilder.create().build()

        val nonOwnedToothbrush1 = createKLTBConnection(activeProfile.id + 5)

        val nonOwnedToothbrush2 = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(nonOwnedToothbrush1, nonOwnedToothbrush2)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        toothbrushesForProfile.currentProfileAccountToothbrushesOnceAndStream().test()
            .assertValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileAccountToothbrushesOnceAndStream emits toothbrushes owned by active profile as well as those shared`() {
        val activeProfile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(activeProfile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        toothbrushesForProfile.currentProfileAccountToothbrushesOnceAndStream().test()
            .assertValue(listOf(ownedToothbrush, sharedToothbrush).toAccountToothbrush())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileAccountToothbrushesOnceAndStream emits toothbrushes owned by new profile after switching`() {
        val peter = ProfileBuilder.create().build()

        val johnId = 76L
        val john = ProfileBuilder.create().withId(johnId).build()

        val peterToothbrush = createKLTBConnection(peter.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val johnToothbrush = createKLTBConnection(johnId)

        val connections = listOf(peterToothbrush, johnToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        val activeProfileSubject = mockActiveProfile(peter)

        val observer =
            toothbrushesForProfile.currentProfileAccountToothbrushesOnceAndStream().test()
                .assertValueCount(1)

        activeProfileSubject.onNext(john)

        observer.assertLastValue(listOf(johnToothbrush, sharedToothbrush).toAccountToothbrush())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileAccountToothbrushesOnceAndStream emits list after toothbrush repository emits new list`() {
        val activeProfile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(activeProfile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        val accountToothbrushesSubject = mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        val initialList = listOf(ownedToothbrush, sharedToothbrush)

        val observer =
            toothbrushesForProfile.currentProfileAccountToothbrushesOnceAndStream().test()
                .assertValueCount(1)
                .assertValue(initialList.toAccountToothbrush())

        accountToothbrushesSubject.onNext(listOf())

        /*
         * We just want to test that a new item is emitted after toothbrushRepo emits
         */
        observer
            .assertValueCount(2)
            .assertLastValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    /*
    currentProfileToothbrushesOnceAndStream
     */

    @Test
    fun `currentProfileToothbrushesOnceAndStream emits empty list if profile can't access any of the toothbrushes`() {
        val activeProfile = ProfileBuilder.create().build()

        val nonOwnedToothbrush1 = createKLTBConnection(activeProfile.id + 5)

        val nonOwnedToothbrush2 = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(nonOwnedToothbrush1, nonOwnedToothbrush2)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        toothbrushesForProfile.currentProfileToothbrushesOnceAndStream().test()
            .assertValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileToothbrushesOnceAndStream emits toothbrushes owned by active profile as well as those shared`() {
        val activeProfile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(activeProfile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        toothbrushesForProfile.currentProfileToothbrushesOnceAndStream().test()
            .assertValue(listOf(ownedToothbrush, sharedToothbrush))
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileToothbrushesOnceAndStream emits toothbrushes owned by new profile after switching`() {
        val peter = ProfileBuilder.create().build()

        val johnId = 76L
        val john = ProfileBuilder.create().withId(johnId).build()

        val peterToothbrush = createKLTBConnection(peter.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val johnToothbrush = createKLTBConnection(johnId)

        val connections = listOf(peterToothbrush, johnToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        val activeProfileSubject = mockActiveProfile(peter)

        val observer = toothbrushesForProfile.currentProfileToothbrushesOnceAndStream().test()
            .assertValueCount(1)

        activeProfileSubject.onNext(john)

        observer.assertLastValue(listOf(johnToothbrush, sharedToothbrush))
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileToothbrushesOnceAndStream emits new list after service reconnected`() {
        val activeProfile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(activeProfile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        val serviceStateSubject = mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        val observer = toothbrushesForProfile.currentProfileToothbrushesOnceAndStream().test()
            .assertValueCount(1)

        val expectedList = listOf(sharedToothbrush)
        serviceStateSubject.onNext(mockServiceConnected(expectedList))

        observer
            .assertLastValue(expectedList)
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileToothbrushesOnceAndStream emits list after toothbrush repository emits new list`() {
        val activeProfile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(activeProfile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        val accountToothbrushesSubject = mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        val initialList = listOf(ownedToothbrush, sharedToothbrush)

        val observer = toothbrushesForProfile.currentProfileToothbrushesOnceAndStream().test()
            .assertValueCount(1)
            .assertValue(initialList)

        accountToothbrushesSubject.onNext(listOf())

        /*
         * We just want to test that a new item is emitted after toothbrushRepo emits
         */
        observer
            .assertValueCount(2)
            .assertLastValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `currentProfileToothbrushesOnceAndStream emits list after service emits new list`() {
        val activeProfile = ProfileBuilder.create().build()

        val ownedToothbrush = createKLTBConnection(activeProfile.id)

        val sharedToothbrush = KLTBConnectionBuilder.createAndroidLess()
            .withSharedMode()
            .build()

        val nonOwnedToothbrush = createKLTBConnection(activeProfile.id + 10)

        val connections = listOf(ownedToothbrush, nonOwnedToothbrush, sharedToothbrush)

        mockToothbrushRepository(connections.toAccountToothbrush())

        mockConnectedService(connections)

        mockActiveProfile(activeProfile)

        val initialList = listOf(ownedToothbrush, sharedToothbrush)

        val observer = toothbrushesForProfile.currentProfileToothbrushesOnceAndStream().test()
            .assertValueCount(1)
            .assertValue(initialList)

        serviceConnectionsStream.onNext(listOf())

        /*
         * We just want to test that a new item is emitted after toothbrushRepo emits
         */
        observer
            .assertValueCount(2)
            .assertLastValue(listOf())
            .assertNotComplete()
            .assertNoErrors()
    }

    /*
    Utils
     */

    private fun mockToothbrushRepository(connections: List<AccountToothbrush> = listOf()): BehaviorProcessor<List<AccountToothbrush>> {
        val subject = BehaviorProcessor.createDefault(connections)
        whenever(toothbrushRepository.listAllOnceAndStream()).thenReturn(subject)

        return subject
    }

    private fun mockConnectedService(connections: List<KLTBConnection>): BehaviorSubject<ServiceProvisionResult> {
        val subject = mockService()

        val serviceConnected = mockServiceConnected(connections)

        subject.onNext(serviceConnected)

        return subject
    }

    private fun mockServiceConnected(connections: List<KLTBConnection>): ServiceConnected {
        val service = mock<KolibreeService>()
        whenever(service.knownConnectionsOnceAndStream)
            .thenReturn(serviceConnectionsStream.startWith(connections))

        return ServiceConnected(service)
    }

    private fun mockService(): BehaviorSubject<ServiceProvisionResult> {
        val subject = BehaviorSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(subject)

        return subject
    }

    private fun mockActiveProfile(activeProfile: Profile?): BehaviorProcessor<Profile> {
        val subject = BehaviorProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(subject)

        if (activeProfile != null) {
            subject.onNext(activeProfile)
        }

        return subject
    }

    /**
     * Creates a KLTBConnection associated to [ownerId] and with a random mac
     */
    private fun createKLTBConnection(ownerId: Long): KLTBConnection {
        return KLTBConnectionBuilder.createAndroidLess()
            .withOwnerId(ownerId)
            .withMac(UUID.randomUUID().toString())
            .build()
    }
}
