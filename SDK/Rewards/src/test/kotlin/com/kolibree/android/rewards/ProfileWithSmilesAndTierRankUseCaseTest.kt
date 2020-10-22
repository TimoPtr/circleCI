/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.ProfileSmiles
import com.kolibree.android.rewards.persistence.ProfileTier
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import org.junit.Test
import org.mockito.Mock

internal class ProfileWithSmilesAndTierRankUseCaseTest : BaseUnitTest() {

    @Mock
    lateinit var profileManager: ProfileManager

    @Mock
    lateinit var rewardsRepository: RewardsRepository

    private lateinit var useCase: ProfileWithSmilesAndTierRankUseCase

    override fun setup() {
        super.setup()

        useCase = ProfileWithSmilesAndTierRankUseCase(profileManager, rewardsRepository)
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit nothing when no other profiles`() {
        val profile = ProfileBuilder.create().build()
        whenever(profileManager.getProfilesLocally()).thenReturn(Single.never())
        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit other profile with smiles and tier`() {
        val profile = ProfileBuilder.create().withId(0L).build()
        val otherProfile = ProfileBuilder.create().withId(1L).build()
        val otherProfileTier = ProfileTier(otherProfile.id, 1, 1, 1, "", "Bronze")
        val otherProfileSmiles: ProfileSmiles = mock()
        val smiles = 1001

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile, otherProfile)))
        whenever(rewardsRepository.profileTier(otherProfile.id)).thenReturn(Flowable.just(listOf(otherProfileTier)))
        whenever(otherProfileSmiles.smiles).thenReturn(smiles)
        whenever(rewardsRepository.profileProgress(otherProfile.id)).thenReturn(Flowable.just(listOf(otherProfileSmiles)))

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(ProfileWithSmilesAndTierRank(otherProfile, smiles, otherProfileTier.rank)))
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit other profile with smiles but no tier available`() {
        val profile = ProfileBuilder.create().withId(0L).build()
        val otherProfile = ProfileBuilder.create().withId(1L).build()
        val otherProfileSmiles: ProfileSmiles = mock()
        val smiles = 1001

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile, otherProfile)))
        whenever(rewardsRepository.profileTier(otherProfile.id)).thenReturn(Flowable.just(emptyList()))
        whenever(otherProfileSmiles.smiles).thenReturn(smiles)
        whenever(rewardsRepository.profileProgress(otherProfile.id)).thenReturn(Flowable.just(listOf(otherProfileSmiles)))

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(ProfileWithSmilesAndTierRank(otherProfile, smiles, null)))
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit other profile with tier but no smiles available`() {
        val profile = ProfileBuilder.create().withId(0L).build()
        val otherProfile = ProfileBuilder.create().withId(1L).build()
        val otherProfileTier = ProfileTier(otherProfile.id, 1, 1, 1, "", "Bronze")

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile, otherProfile)))
        whenever(rewardsRepository.profileTier(otherProfile.id)).thenReturn(Flowable.just(listOf(otherProfileTier)))
        whenever(rewardsRepository.profileProgress(otherProfile.id)).thenReturn(Flowable.just(emptyList()))

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(ProfileWithSmilesAndTierRank(otherProfile, 0, otherProfileTier.rank)))
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit when an update on smile occurred`() {
        val profile = ProfileBuilder.create().withId(0L).build()
        val otherProfile = ProfileBuilder.create().withId(1L).build()
        val otherProfileTier = ProfileTier(otherProfile.id, 1, 1, 1, "", "Bronze")
        val profileSmilesProcessor = PublishProcessor.create<List<ProfileSmiles>>()
        val otherProfileSmiles: ProfileSmiles = mock()
        val smiles = 1001

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile, otherProfile)))
        whenever(rewardsRepository.profileTier(otherProfile.id)).thenReturn(Flowable.just(listOf(otherProfileTier)))
        whenever(otherProfileSmiles.smiles).thenReturn(smiles)
        whenever(rewardsRepository.profileProgress(otherProfile.id)).thenReturn(profileSmilesProcessor)

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        profileSmilesProcessor.onNext(emptyList())

        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(ProfileWithSmilesAndTierRank(otherProfile, 0, otherProfileTier.rank)))

        profileSmilesProcessor.onNext(listOf(otherProfileSmiles))

        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
        testObserver.assertValues(listOf(ProfileWithSmilesAndTierRank(otherProfile, 0, otherProfileTier.rank)), listOf(ProfileWithSmilesAndTierRank(otherProfile, smiles, otherProfileTier.rank)))
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit when an update on tier occurred`() {
        val profile = ProfileBuilder.create().withId(0L).build()
        val otherProfile = ProfileBuilder.create().withId(1L).build()
        val profileTierProcessor = PublishProcessor.create<List<ProfileTier>>()
        val otherProfileTier = ProfileTier(otherProfile.id, 1, 1, 1, "", "Bronze")
        val otherProfileSmiles: ProfileSmiles = mock()
        val smiles = 1001

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile, otherProfile)))
        whenever(rewardsRepository.profileTier(otherProfile.id)).thenReturn(profileTierProcessor)
        whenever(otherProfileSmiles.smiles).thenReturn(smiles)
        whenever(rewardsRepository.profileProgress(otherProfile.id)).thenReturn(Flowable.just(listOf(otherProfileSmiles)))

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        profileTierProcessor.onNext(emptyList())

        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(ProfileWithSmilesAndTierRank(otherProfile, smiles, null)))

        profileTierProcessor.onNext(listOf(otherProfileTier))

        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
        testObserver.assertValues(listOf(ProfileWithSmilesAndTierRank(otherProfile, smiles, null)), listOf(ProfileWithSmilesAndTierRank(otherProfile, smiles, otherProfileTier.rank)))
    }

    @Test
    fun `retrieveOtherProfilesSmilesStream emit all profiles when an update when a profile is upadted`() {
        val profile = ProfileBuilder.create().withId(0L).build()
        val otherProfile = ProfileBuilder.create().withId(2L).build()
        val otherProfile2 = ProfileBuilder.create().withId(1L).build()
        val profileTierProcessor = PublishProcessor.create<List<ProfileTier>>()
        val otherProfileTier = ProfileTier(otherProfile.id, 1, 1, 1, "", "Bronze")

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile, otherProfile, otherProfile2)))
        whenever(rewardsRepository.profileTier(otherProfile.id)).thenReturn(profileTierProcessor)
        whenever(rewardsRepository.profileTier(otherProfile2.id)).thenReturn(Flowable.just(emptyList()))
        whenever(rewardsRepository.profileProgress(any())).thenReturn(Flowable.just(emptyList()))

        val testObserver = useCase.retrieveOtherProfilesSmilesStream(profile).test()

        profileTierProcessor.onNext(emptyList())

        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(ProfileWithSmilesAndTierRank(otherProfile, 0, null), ProfileWithSmilesAndTierRank(otherProfile2, 0, null)))

        profileTierProcessor.onNext(listOf(otherProfileTier))

        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
        testObserver.assertValues(
            listOf(ProfileWithSmilesAndTierRank(otherProfile, 0, null),
                ProfileWithSmilesAndTierRank(otherProfile2, 0, null)),
            listOf(ProfileWithSmilesAndTierRank(otherProfile, 0, otherProfileTier.rank),
                ProfileWithSmilesAndTierRank(otherProfile2, 0, null)))
    }
}
