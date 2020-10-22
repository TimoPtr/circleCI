/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.logic

import androidx.annotation.Keep
import com.google.common.base.Optional
import com.kolibree.android.rewards.personalchallenge.domain.logic.BrushingEventsProvider
import com.kolibree.android.rewards.personalchallenge.domain.logic.PersonalChallengeV1Repository
import com.kolibree.android.rewards.personalchallenge.presentation.CompletedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

@Keep
interface HumChallengeUseCase {

    /**
     * Retrieve the current challenge might be NotAccepted, OnGoing or Completed or null if
     * no challenge are available
     */
    fun challengeStream(): Flowable<Optional<HumChallenge>>

    /**
     * Accept an proposal challenge, OngoingChallenge will be emitted by [challengeStream]
     */
    fun acceptChallenge(humChallenge: NotAcceptedChallenge): Completable

    /**
     * Complete the challenge so we can get a new one,
     * the new challenge will be emitted by [challengeStream]
     */
    fun completeChallenge(humChallenge: CompletedChallenge): Completable
}

internal class HumChallengeUseCaseImpl @Inject constructor(
    private val personalChallengeV1Repository: PersonalChallengeV1Repository,
    private val brushingEventsProvider: BrushingEventsProvider
) : HumChallengeUseCase {

    override fun challengeStream(): Flowable<Optional<HumChallenge>> =
        currentHumChallenge().flatMap {
            if (it.isPresent) {
                Flowable.just(it)
            } else {
                brushingEventsProvider.brushingEventsStreamCurrentProfile()
                    .map { events ->
                        Optional.fromNullable<HumChallenge>(
                            NotAcceptedChallenge.fromBrushingEvent(events)
                        )
                    }
            }
        }

    override fun acceptChallenge(humChallenge: NotAcceptedChallenge): Completable =
        personalChallengeV1Repository.createChallengeForCurrentProfile(humChallenge.challenge.toV1PersonalChallenge())

    override fun completeChallenge(humChallenge: CompletedChallenge): Completable =
        personalChallengeV1Repository.deleteChallengeForCurrentProfile()

    private fun currentHumChallenge(): Flowable<Optional<HumChallenge>> =
        personalChallengeV1Repository.getChallengesForCurrentProfile()
            .map {
                val challenge = it.firstOrNull()
                if (challenge == null) {
                    Optional.absent()
                } else {
                    Optional.fromNullable(HumChallenge.fromV1PersonalChallenge(challenge))
                }
            }
}
