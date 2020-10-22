/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.ProfileSmiles
import com.kolibree.android.rewards.models.ProfileSmilesEntity
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mock

internal class ProfileWithSmilesUseCaseTest : BaseUnitTest() {

    @Mock
    lateinit var profileManager: ProfileManager

    @Mock
    lateinit var currentProfileProvider: CurrentProfileProvider

    @Mock
    lateinit var rewardsRepository: RewardsRepository

    private lateinit var useCase: ProfileWithSmilesUseCase

    override fun setup() {
        super.setup()

        useCase = ProfileWithSmilesUseCase(profileManager, currentProfileProvider, rewardsRepository)
    }

    @Test
    fun `currentProfileWithSmilesStream emits nothing until a currentProfile is emit`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        val observer = useCase.currentProfileWithSmilesStream().test()

        observer.assertEmpty()

        verify(rewardsRepository, never()).profileProgress(any())

        assertTrue(profileProcessor.hasSubscribers())

        val expectedProfileId = 62L
        val profile = ProfileBuilder.create().withId(expectedProfileId).build()

        val expectedSmiles = 7658
        whenever(rewardsRepository.profileProgress(expectedProfileId)).thenReturn(
            BehaviorProcessor.createDefault(
                listOf(ProfileSmilesEntity(expectedProfileId, expectedSmiles))
            )
        )

        profileProcessor.onNext(profile)

        verify(rewardsRepository).profileProgress(expectedProfileId)

        val profileWithSmiles = ProfileWithSmiles(profile, expectedSmiles)

        observer.assertValue(profileWithSmiles)
    }

    @Test
    fun `currentProfileWithSmilesStream emits ProfileSmiles with 0 smiles when repository doesn't return a value`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        val observer = useCase.currentProfileWithSmilesStream().test()

        observer.assertEmpty()

        verify(rewardsRepository, never()).profileProgress(any())

        assertTrue(profileProcessor.hasSubscribers())

        val expectedProfileId = 62L
        val profile = ProfileBuilder.create().withId(expectedProfileId).build()

        whenever(rewardsRepository.profileProgress(expectedProfileId)).thenReturn(
            BehaviorProcessor.createDefault(
                listOf()
            )
        )

        profileProcessor.onNext(profile)

        verify(rewardsRepository).profileProgress(expectedProfileId)

        val profileWithSmiles = ProfileWithSmiles(profile, 0)

        observer.assertValue(profileWithSmiles)
    }

    @Test
    fun `requests new ProfileSmiles when current profile provider emits new profile`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        val observer = useCase.currentProfileWithSmilesStream().test()

        observer.assertEmpty()

        verify(rewardsRepository, never()).profileProgress(any())

        assertTrue(profileProcessor.hasSubscribers())

        val firstProfileId = 62L
        val firstProfile = ProfileBuilder.create().withId(firstProfileId).build()

        val firstProfileExpectedSmiles = 7658
        whenever(rewardsRepository.profileProgress(firstProfileId)).thenReturn(
            BehaviorProcessor.createDefault(
                listOf(ProfileSmilesEntity(firstProfileId, firstProfileExpectedSmiles))
            )
        )

        profileProcessor.onNext(firstProfile)

        verify(rewardsRepository).profileProgress(firstProfileId)

        val firstProfileWithSmiles = ProfileWithSmiles(firstProfile, firstProfileExpectedSmiles)

        observer.assertValue(firstProfileWithSmiles)

        val secondProfileId = 992L
        val secondProfile = ProfileBuilder.create().withId(secondProfileId).build()

        val secondProfileExpectedSmiles = 4
        whenever(rewardsRepository.profileProgress(secondProfileId)).thenReturn(
            BehaviorProcessor.createDefault(
                listOf(ProfileSmilesEntity(secondProfileId, secondProfileExpectedSmiles))
            )
        )

        profileProcessor.onNext(secondProfile)

        verify(rewardsRepository).profileProgress(secondProfileId)

        val secondProfileWithSmiles = ProfileWithSmiles(secondProfile, secondProfileExpectedSmiles)

        observer.assertValues(firstProfileWithSmiles, secondProfileWithSmiles)
    }

    @Test
    fun `getProfileWithSmiles emit profile with smiles and update smiles count and dont complete`() {
        val profileId = 0L
        val smiles1 = 100
        val smiles2 = 101
        val profile = ProfileBuilder.create().withId(profileId).build()
        val flowableProcessor = PublishProcessor.create<List<ProfileSmiles>>()
        whenever(profileManager.getProfileLocally(profileId)).thenReturn(Single.just(profile))

        whenever(rewardsRepository.profileProgress(profileId)).thenReturn(
            flowableProcessor
        )

        val testObserver = useCase.getProfileWithSmilesStream(profileId).test()

        flowableProcessor.onNext(listOf(ProfileSmilesEntity(profileId, smiles1)))
        flowableProcessor.onNext(listOf(ProfileSmilesEntity(profileId, smiles2)))

        testObserver.assertValues(ProfileWithSmiles(profile, smiles1), ProfileWithSmiles(profile, smiles2))
        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
    }

    @Test
    fun `retrieveOtherProfilesSmiles emit profile with smiles but not the one given`() {
        val currentProfileId = 0L
        val otherProfileId = 1L
        val smileOtherProfile = 10

        val currentProfile = ProfileBuilder.create().withId(currentProfileId).build()
        val otherProfile = ProfileBuilder.create().withId(otherProfileId).build()

        val flowableProcessorOtherProfile = PublishProcessor.create<List<ProfileSmiles>>()

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(currentProfile, otherProfile)))

        whenever(rewardsRepository.profileProgress(otherProfileId)).thenReturn(
            flowableProcessorOtherProfile
        )

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(currentProfile).test()

        flowableProcessorOtherProfile.onNext(listOf(ProfileSmilesEntity(otherProfileId, smileOtherProfile)))

        testObserver.assertValue(listOf(ProfileWithSmiles(otherProfile, smileOtherProfile)))
        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
    }

    @Test
    fun `retrieveOtherProfilesSmiles emit all other profile when only one updated`() {
        val currentProfileId = 0L
        val otherProfileId1 = 1L
        val otherProfileId2 = 2L
        val smileOtherProfile1 = 10
        val smileOtherProfile2 = 100
        val smileOtherProfile1Update = 10000

        val currentProfile = ProfileBuilder.create().withId(currentProfileId).build()
        val otherProfile1 = ProfileBuilder.create().withId(otherProfileId1).build()
        val otherProfile2 = ProfileBuilder.create().withId(otherProfileId2).build()

        val flowableProcessorOtherProfile1 = PublishProcessor.create<List<ProfileSmiles>>()
        val flowableProcessorOtherProfile2 = PublishProcessor.create<List<ProfileSmiles>>()

        whenever(profileManager.getProfilesLocally()).thenReturn(
            Single.just(
                listOf(
                    currentProfile,
                    otherProfile1,
                    otherProfile2
                )
            )
        )

        whenever(rewardsRepository.profileProgress(otherProfileId1)).thenReturn(
            flowableProcessorOtherProfile1
        )

        whenever(rewardsRepository.profileProgress(otherProfileId2)).thenReturn(
            flowableProcessorOtherProfile2
        )

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(currentProfile).test()

        flowableProcessorOtherProfile1.onNext(listOf(ProfileSmilesEntity(otherProfileId1, smileOtherProfile1)))
        flowableProcessorOtherProfile2.onNext(listOf(ProfileSmilesEntity(otherProfileId2, smileOtherProfile2)))
        flowableProcessorOtherProfile1.onNext(listOf(ProfileSmilesEntity(otherProfileId1, smileOtherProfile1Update)))

        testObserver.assertValues(
            listOf(
                ProfileWithSmiles(otherProfile1, smileOtherProfile1),
                ProfileWithSmiles(otherProfile2, smileOtherProfile2)
            ),
            listOf(
                ProfileWithSmiles(otherProfile1, smileOtherProfile1Update),
                ProfileWithSmiles(otherProfile2, smileOtherProfile2)
            )
        )

        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
    }
}
