/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.profile.persistence.FakeLifetimeSmiles
import com.kolibree.android.app.ui.home.tab.profile.persistence.FakeLifetimeSmilesRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.BehaviorProcessor
import org.junit.Test

class LifetimeSmilesUseCaseTest : BaseUnitTest() {
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val lifetimeSmilesRepository = FakeLifetimeSmilesRepository()

    private val useCase = LifetimeSmilesUseCase(currentProfileProvider, lifetimeSmilesRepository)

    private val defaultProfileId = ProfileBuilder.DEFAULT_ID

    private val currentProfileProviderProcessor = BehaviorProcessor.createDefault(defaultProfileId)

    override fun setup() {
        super.setup()

        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(currentProfileProviderProcessor
                .map { ProfileBuilder.create().withId(it).build() }
            )
    }

    @Test
    fun `useCase emits smiles for activeProfile`() {
        val expectedPoints = 435
        lifetimeSmilesRepository.insertOrReplace(
            FakeLifetimeSmiles(
                profileId = defaultProfileId,
                lifetimePoints = expectedPoints
            )
        )

        useCase.lifetimePoints().test().assertValue(expectedPoints)
    }

    @Test
    fun `useCase doesn't emit if there's no info for active profile`() {
        lifetimeSmilesRepository.insertOrReplace(
            FakeLifetimeSmiles(
                profileId = defaultProfileId - 1,
                lifetimePoints = 30
            )
        )

        useCase.lifetimePoints().test().assertNoValues().assertNotComplete()
    }

    @Test
    fun `useCase emits new value if activeProfile changes`() {
        val initialPoints = 30
        lifetimeSmilesRepository.insertOrReplace(
            FakeLifetimeSmiles(
                profileId = defaultProfileId,
                lifetimePoints = initialPoints
            )
        )

        val expectedPoints = 435
        val secondProfileId = defaultProfileId + 1
        lifetimeSmilesRepository.insertOrReplace(
            FakeLifetimeSmiles(
                profileId = secondProfileId,
                lifetimePoints = expectedPoints
            )
        )

        val observer = useCase.lifetimePoints().test().assertValue(initialPoints)

        currentProfileProviderProcessor.onNext(secondProfileId)

        observer.assertValues(initialPoints, expectedPoints)
    }
}
