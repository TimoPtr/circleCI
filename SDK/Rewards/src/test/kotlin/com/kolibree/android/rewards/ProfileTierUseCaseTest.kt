/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository
import com.kolibree.android.rewards.persistence.ProfileTier
import com.kolibree.android.rewards.persistence.ProfileTierOptional
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.junit.Test

internal class ProfileTierUseCaseTest : BaseUnitTest() {

    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val profileSmilesRepository: ProfileSmilesRepository = mock()

    private lateinit var useCase: ProfileTierUseCase

    override fun setup() {
        super.setup()

        useCase = ProfileTierUseCase(currentProfileProvider, profileSmilesRepository)
    }

    @Test
    fun `requests profiles tiers for correct profile ID`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        useCase.currentProfileTier().test()

        profileProcessor.onNext(DUMMY_PROFILE)

        verify(profileSmilesRepository, times(1)).profileTier(eq(PROFILE_ID))
    }

    @Test
    fun `empty tier list returns empty optional`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        val profileTierProcessor = PublishProcessor.create<List<ProfileTier>>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)
        whenever(profileSmilesRepository.profileTier(any())).thenReturn(profileTierProcessor)

        val observer = useCase.currentProfileTier().test()

        profileProcessor.onNext(DUMMY_PROFILE)
        profileTierProcessor.onNext(emptyList())

        observer.assertNoErrors()
        observer.assertValueCount(1)
        observer.assertValueAt(0, EMPTY_PROFILE_TIER_OPTIONAL)
    }

    @Test
    fun `nothing emitted returns empty optional`() {
        val profileProcessor = Flowable.empty<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        val observer = useCase.currentProfileTier().test()

        observer.assertNoErrors()
        observer.assertValueCount(1)
        observer.assertValueAt(0, EMPTY_PROFILE_TIER_OPTIONAL)
    }

    @Test
    fun `single tier list returns correct tier`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        val profileTierProcessor = PublishProcessor.create<List<ProfileTier>>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)
        whenever(profileSmilesRepository.profileTier(any())).thenReturn(profileTierProcessor)

        val observer = useCase.currentProfileTier().test()

        profileProcessor.onNext(DUMMY_PROFILE)
        profileTierProcessor.onNext(listOf(TIER_1))

        observer.assertNoErrors()
        observer.assertValueCount(1)
        observer.assertValueAt(0, ProfileTierOptional(TIER_1))
    }

    @Test
    fun `multiple tier list returns correct tier`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        val profileTierProcessor = PublishProcessor.create<List<ProfileTier>>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)
        whenever(profileSmilesRepository.profileTier(any())).thenReturn(profileTierProcessor)

        val observer = useCase.currentProfileTier().test()

        profileProcessor.onNext(DUMMY_PROFILE)
        profileTierProcessor.onNext(listOf(TIER_2, TIER_1))

        observer.assertNoErrors()
        observer.assertValueCount(1)
        observer.assertValueAt(0, ProfileTierOptional(TIER_2))
    }
}

private const val PROFILE_ID = 1234L
private val EMPTY_PROFILE_TIER_OPTIONAL = ProfileTierOptional()
private val DUMMY_PROFILE = ProfileBuilder.create().withId(PROFILE_ID).build()
private const val TIER_ID_1 = 1L
private const val TIER_ID_2 = 2L
private val TIER_1 = ProfileTier(TIER_ID_1, 0, 0, 0, "", "")
private val TIER_2 = ProfileTier(TIER_ID_2, 0, 0, 0, "", "")
