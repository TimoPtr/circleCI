/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.HomeSessionFlag
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.rewards.CurrentProfileCategoriesWithProgressUseCase
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.models.ChallengeWithProgress
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

class CompleteProfileBubbleUseCaseImplTest : BaseUnitTest() {

    @Test
    fun `show bubble only if all conditions are met`() {
        val tester = createUseCase().getShowCompleteProfileBubbleStream().test()
        tester.assertValue(true)
    }

    @Test
    fun `do not show bubble if challenge is missing`() {
        val tester = createUseCase(
            challengeProgress = EarnPointsChallenge.Id.RATE_THE_APP.backendId to EXPECTED_PERCENTAGE
        ).getShowCompleteProfileBubbleStream().test()
        tester.assertValue(false)
    }

    @Test
    fun `do not show bubble if challenge is completed`() {
        val tester = createUseCase(
            challengeProgress = EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE.backendId to 100
        ).getShowCompleteProfileBubbleStream().test()
        tester.assertValue(false)
    }

    @Test
    fun `do not show bubble if it was suppressed`() {
        val tester = createUseCase(
            featureWasSuppressed = true
        ).getShowCompleteProfileBubbleStream().test()
        tester.assertValue(false)
    }

    @Test
    fun `return challenge percentage if all conditions are met`() {
        val tester = createUseCase().getProfileCompletionPercentageStream().test()
        tester.assertValue(EXPECTED_PERCENTAGE)
    }

    @Test
    fun `return 0 percentage if challenge is missing`() {
        val tester = createUseCase(
            challengeProgress = EarnPointsChallenge.Id.RATE_THE_APP.backendId to EXPECTED_PERCENTAGE
        ).getProfileCompletionPercentageStream().test()
        tester.assertValue(0)
    }

    @Test
    fun `hide the bubble if suppression was called`() {
        val useCase = createUseCase()
        val tester = useCase.getShowCompleteProfileBubbleStream().test()
        useCase.suppressBubble()

        tester.assertValues(true, false)
    }

    private fun createUseCase(
        challengeProgress: Pair<Long, Int> =
            EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE.backendId to EXPECTED_PERCENTAGE,
        featureWasSuppressed: Boolean = false
    ): CompleteProfileBubbleUseCase {
        val challengeWithProgress: ChallengeWithProgress = mock()
        whenever(challengeWithProgress.id).thenReturn(challengeProgress.first)
        whenever(challengeWithProgress.percentage).thenReturn(challengeProgress.second)
        whenever(challengeWithProgress.isCompleted()).thenReturn(challengeProgress.second == 100)

        val categoryWithProgress: CategoryWithProgress = mock()
        whenever(categoryWithProgress.challenges).thenReturn(listOf(challengeWithProgress))

        val challengesProgressUseCase: CurrentProfileCategoriesWithProgressUseCase = mock()
        whenever(challengesProgressUseCase.categoriesWithProgress())
            .thenReturn(Flowable.just(listOf(categoryWithProgress)))

        val sessionFlags: SessionFlags = SessionFlagsFake(!featureWasSuppressed)

        return CompleteProfileBubbleUseCaseImpl(
            challengesProgressUseCase,
            sessionFlags
        )
    }

    private class SessionFlagsFake(initialValue: Boolean) : SessionFlags {

        private var showIncompleteBubble: Boolean = initialValue

        override fun setSessionFlag(key: String, value: Boolean) {
            if (key == HomeSessionFlag.SHOW_PROFILE_INCOMPLETE_BUBBLE) {
                showIncompleteBubble = value
            }
        }

        override fun readSessionFlag(key: String): Boolean? =
            if (key == HomeSessionFlag.SHOW_PROFILE_INCOMPLETE_BUBBLE)
                showIncompleteBubble
            else null
    }
}

private const val EXPECTED_PERCENTAGE = 66
