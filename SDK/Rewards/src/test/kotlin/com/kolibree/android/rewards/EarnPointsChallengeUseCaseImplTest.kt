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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.feedback.ChallengeCompletedFeedback
import com.kolibree.android.rewards.feedback.FeedbackRepository
import com.kolibree.android.rewards.feedback.personal.BackendChallengeCompleted
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.RATE_THE_APP
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.REFER_A_FRIEND
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Test

class EarnPointsChallengeUseCaseImplTest : BaseUnitTest() {

    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val feedbackRepository: FeedbackRepository = mock()

    private lateinit var useCase: EarnPointsChallengeUseCase

    override fun setup() {
        super.setup()
        useCase = EarnPointsChallengeUseCaseImpl(
            currentProfileProvider,
            feedbackRepository
        )

        whenever(feedbackRepository.markAsConsumed(any()))
            .thenReturn(Completable.complete())
    }

    @Test
    fun `uses distinct ids when marking as consumed`() {
        val mockChallenges = listOf(
            CompleteEarnPointsChallenge(mock(), feedbackId = 1),
            CompleteEarnPointsChallenge(mock(), feedbackId = 1),
            CompleteEarnPointsChallenge(mock(), feedbackId = 1),
            CompleteEarnPointsChallenge(mock(), feedbackId = 2),
            CompleteEarnPointsChallenge(mock(), feedbackId = 2),
            CompleteEarnPointsChallenge(mock(), feedbackId = 3)
        )

        val observer = useCase.markAsConsumed(mockChallenges).test()

        verify(feedbackRepository).markAsConsumed(listOf(1, 2, 3))
        observer.assertComplete()
    }

    @Test
    fun `filters complete challenges`() {
        val mockProfile = ProfileBuilder.create().build()
        val mockFeedback = ChallengeCompletedFeedback(
            id = 123,
            challengesCompleted = listOf(
                backendChallenge(REFER_A_FRIEND.backendId, points = 10),
                backendChallenge(REFER_A_FRIEND.backendId, points = 10),
                backendChallenge(RATE_THE_APP.backendId, points = 20),
                backendChallenge(RATE_THE_APP.backendId, points = 20)
            )
        )

        whenever(currentProfileProvider.currentProfileFlowable())
            .thenReturn(Flowable.just(mockProfile))

        whenever(feedbackRepository.feedbackNotConsumedStream(mockProfile.id))
            .thenReturn(Flowable.just(listOf(mockFeedback)))

        val observer = useCase
            .observeForCompleteChallenges(listOf(REFER_A_FRIEND))
            .test()

        observer.assertValue(
            listOf(
                CompleteEarnPointsChallenge(
                    challenge = EarnPointsChallenge(REFER_A_FRIEND, 10),
                    feedbackId = mockFeedback.id
                ),
                CompleteEarnPointsChallenge(
                    challenge = EarnPointsChallenge(REFER_A_FRIEND, 10),
                    feedbackId = mockFeedback.id
                )
            )
        )
    }

    private fun backendChallenge(
        id: Long,
        points: Int
    ) = BackendChallengeCompleted(
        id = id,
        name = "mock name",
        category = "mock category",
        greetingMessage = "mock greetingMessage",
        description = "mock description",
        pictureUrl = "mock pictureUrl",
        smilesReward = points,
        action = null
    )
}
