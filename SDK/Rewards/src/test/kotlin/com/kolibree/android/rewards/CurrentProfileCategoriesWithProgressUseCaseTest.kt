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
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mock

internal class CurrentProfileCategoriesWithProgressUseCaseTest : BaseUnitTest() {
    @Mock
    lateinit var currentProfileProvider: CurrentProfileProvider

    @Mock
    lateinit var rewardsRepository: RewardsRepository

    private lateinit var categoriesWithProgressUseCase: CurrentProfileCategoriesWithProgressUseCase

    override fun setup() {
        super.setup()

        categoriesWithProgressUseCase =
            CurrentProfileCategoriesWithProgressUseCaseImpl(
                currentProfileProvider,
                rewardsRepository
            )
    }

    @Test
    fun `requests list when current profile provider emits`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        val observer = categoriesWithProgressUseCase.categoriesWithProgress().test()

        observer.assertEmpty()

        verify(rewardsRepository, never()).categoriesWithChallengeProgress(any())

        assertTrue(profileProcessor.hasSubscribers())

        val expectedProfileId = 62L
        val profile = ProfileBuilder.create().withId(expectedProfileId).build()

        val expectedList = listOf<CategoryWithProgress>()
        whenever(rewardsRepository.categoriesWithChallengeProgress(expectedProfileId)).thenReturn(
            Flowable.just(
                expectedList
            )
        )

        profileProcessor.onNext(profile)

        verify(rewardsRepository).categoriesWithChallengeProgress(expectedProfileId)

        observer.assertValue(expectedList)
    }

    @Test
    fun `requests new list when current profile provider emits new profile`() {
        val profileProcessor = PublishProcessor.create<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(profileProcessor)

        whenever(rewardsRepository.categoriesWithChallengeProgress(any())).thenReturn(Flowable.just(listOf()))

        val observer = categoriesWithProgressUseCase.categoriesWithProgress().test()

        observer.assertEmpty()

        verify(rewardsRepository, never()).categoriesWithChallengeProgress(any())

        assertTrue(profileProcessor.hasSubscribers())

        val firstExpectedProfileId = 62L
        val firstProfile = ProfileBuilder.create().withId(firstExpectedProfileId).build()

        profileProcessor.onNext(firstProfile)

        verify(rewardsRepository).categoriesWithChallengeProgress(firstExpectedProfileId)

        observer.assertValueCount(1)

        val secondExpectedProfileId = 10L
        val secondProfile = ProfileBuilder.create().withId(secondExpectedProfileId).build()

        profileProcessor.onNext(secondProfile)

        verify(rewardsRepository).categoriesWithChallengeProgress(secondExpectedProfileId)

        observer.assertValueCount(2)
    }
}
