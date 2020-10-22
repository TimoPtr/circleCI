/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.morewaystoearnpoints.logic

import com.kolibree.android.amazondash.domain.AmazonDashAvailabilityUseCase
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.ShowAllMoreWaysCardsFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.rewards.CurrentProfileCategoriesWithProgressUseCase
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.models.ChallengeWithProgress
import com.kolibree.android.rewards.morewaystoearnpoints.logic.MoreWaysToGetPointsCardUseCaseImpl.Companion.CURRENTLY_SUPPORTED_CHALLENGES
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

class MoreWaysToGetPointsCardUseCaseImplTest : BaseUnitTest() {

    private val amazonDashAvailabilityUseCase: AmazonDashAvailabilityUseCase = mock()

    @Test
    fun `return only currently supported, uncompleted challenges as a sorted list`() {
        val challengesList = CURRENTLY_SUPPORTED_CHALLENGES.map { challenge ->
            Triple(challenge.backendId, EXPECTED_PERCENTAGE, EXPECTED_SMILES)
        }
        val tester = createUseCase(challengesList).getChallengesToBeDisplayedStream().test()

        tester.assertValue(CURRENTLY_SUPPORTED_CHALLENGES.map { challenge ->
            EarnPointsChallenge(challenge, EXPECTED_SMILES)
        })
    }

    @Test
    fun `filter out completed challenges`() {
        val challengesList = listOf(
            Triple(EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE.backendId, 100, EXPECTED_SMILES),
            Triple(
                EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS.backendId,
                EXPECTED_PERCENTAGE,
                EXPECTED_SMILES
            )
        )
        val tester = createUseCase(
            challengesList,
            showAllCards = false
        ).getChallengesToBeDisplayedStream().test()

        tester.assertValue(
            listOf(
                EarnPointsChallenge(
                    EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS,
                    EXPECTED_SMILES
                )
            )
        )
    }

    @Test
    fun `filter out unsupported challenges`() {
        val challengesList = EarnPointsChallenge.Id.values().map { challenge ->
            Triple(challenge.backendId, EXPECTED_PERCENTAGE, EXPECTED_SMILES)
        }
        val tester = createUseCase(challengesList).getChallengesToBeDisplayedStream().test()

        tester.assertValue(CURRENTLY_SUPPORTED_CHALLENGES.map { challenge ->
            EarnPointsChallenge(challenge, EXPECTED_SMILES)
        })
    }

    @Test
    fun `show all challenges if toggle is enabled`() {
        val challengesList = EarnPointsChallenge.Id.values().map { challenge ->
            Triple(challenge.backendId, EXPECTED_PERCENTAGE, EXPECTED_SMILES)
        }
        val tester = createUseCase(
            challengesList,
            showAllCards = true
        ).getChallengesToBeDisplayedStream().test()

        tester.assertValue(EarnPointsChallenge.Id.values().map { challenge ->
            EarnPointsChallenge(challenge, EXPECTED_SMILES)
        })
    }

    @Test
    fun `sort challenges based on ID priority`() {
        val challengesList = EarnPointsChallenge.Id.values().map { challenge ->
            Triple(challenge.backendId, EXPECTED_PERCENTAGE, EXPECTED_SMILES)
        }.reversed()
        val tester = createUseCase(
            challengesList,
            showAllCards = true
        ).getChallengesToBeDisplayedStream().test()

        tester.assertValue(EarnPointsChallenge.Id.values().map { challenge ->
            EarnPointsChallenge(challenge, EXPECTED_SMILES)
        })
    }

    @Test
    fun `filter out amazon dash if not available`() {
        val tester = createUseCase(
            showAllCards = true,
            amazonDashAvailable = false
        ).getChallengesToBeDisplayedStream().test()

        tester.assertLastValueWithPredicate { challenges ->
            challenges.none { it.id == EarnPointsChallenge.Id.AMAZON_DASH }
        }
    }

    @Test
    fun `show amazon dash if available`() {
        val tester = createUseCase(
            showAllCards = true,
            amazonDashAvailable = true
        ).getChallengesToBeDisplayedStream().test()

        tester.assertLastValueWithPredicate { challenges ->
            challenges.find { it.id == EarnPointsChallenge.Id.AMAZON_DASH } != null
        }
    }

    private fun createUseCase(
        challengeProgress: List<Triple<Long, Int, Int>> = emptyList(),
        showAllCards: Boolean = false,
        amazonDashAvailable: Boolean = true
    ): MoreWaysToGetPointsCardUseCase {
        val challenges = challengeProgress.map { challenge ->
            val challengeWithProgress: ChallengeWithProgress = mock()
            whenever(challengeWithProgress.id).thenReturn(challenge.first)
            whenever(challengeWithProgress.percentage).thenReturn(challenge.second)
            whenever(challengeWithProgress.isCompleted()).thenReturn(challenge.second == 100)
            whenever(challengeWithProgress.smilesReward).thenReturn(challenge.third)
            challengeWithProgress
        }

        val categoryWithProgress: CategoryWithProgress = mock()
        whenever(categoryWithProgress.challenges).thenReturn(challenges)

        val challengesProgressUseCase: CurrentProfileCategoriesWithProgressUseCase = mock()
        whenever(challengesProgressUseCase.categoriesWithProgress())
            .thenReturn(Flowable.just(listOf(categoryWithProgress)))

        whenever(amazonDashAvailabilityUseCase.isAvailable())
            .thenReturn(Flowable.just(amazonDashAvailable))

        return MoreWaysToGetPointsCardUseCaseImpl(
            challengesProgressUseCase,
            amazonDashAvailabilityUseCase,
            setOf(
                ConstantFeatureToggle(
                    ShowAllMoreWaysCardsFeature,
                    initialValue = showAllCards
                )
            )
        )
    }
}

private const val EXPECTED_PERCENTAGE = 66
private const val EXPECTED_SMILES = 33
