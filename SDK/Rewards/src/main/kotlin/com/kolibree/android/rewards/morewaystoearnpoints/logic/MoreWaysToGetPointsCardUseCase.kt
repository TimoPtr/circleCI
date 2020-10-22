/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.morewaystoearnpoints.logic

import androidx.annotation.VisibleForTesting
import com.kolibree.android.amazondash.domain.AmazonDashAvailabilityUseCase
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.ShowAllMoreWaysCardsFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.rewards.CurrentProfileCategoriesWithProgressUseCase
import com.kolibree.android.rewards.models.ChallengeWithProgress
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.AMAZON_DASH
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.sorted
import io.reactivex.Flowable
import javax.inject.Inject

private typealias ChallengePairs = List<Pair<Id, ChallengeWithProgress?>>

@VisibleForApp
interface MoreWaysToGetPointsCardUseCase {

    fun getChallengesToBeDisplayedStream(): Flowable<List<EarnPointsChallenge>>
}

internal class MoreWaysToGetPointsCardUseCaseImpl @Inject constructor(
    private val challengesProgressUseCase: CurrentProfileCategoriesWithProgressUseCase,
    private val amazonDashAvailabilityUseCase: AmazonDashAvailabilityUseCase,
    private val featureToggles: FeatureToggleSet
) : MoreWaysToGetPointsCardUseCase {

    private val showAllCards: Boolean
        get() = featureToggles.toggleIsOn(ShowAllMoreWaysCardsFeature)

    override fun getChallengesToBeDisplayedStream(): Flowable<List<EarnPointsChallenge>> {
        return Flowable.just(Id.all())
            .filterChallenges()
            .combineWithBackendChallenges()
            .mapToEarnPointsChallenges()
    }

    private fun Flowable<List<Id>>.filterChallenges(): Flowable<List<Id>> {
        return filterSupportedChallenges()
            .filterAmazonDash()
    }

    private fun Flowable<List<Id>>.filterSupportedChallenges(): Flowable<List<Id>> {
        return map { challenges ->
            if (showAllCards) challenges
            else challenges.intersect(CURRENTLY_SUPPORTED_CHALLENGES).toList()
        }
    }

    private fun Flowable<List<Id>>.filterAmazonDash(): Flowable<List<Id>> {
        return switchMap { challenges ->
            if (challenges.contains(AMAZON_DASH)) {
                amazonDashAvailabilityUseCase.isAvailable().map { isAmazonAvailable ->
                    if (isAmazonAvailable) challenges
                    else challenges.minus(AMAZON_DASH)
                }
            } else {
                Flowable.just(challenges)
            }
        }
    }

    private fun Flowable<List<Id>>.combineWithBackendChallenges(): Flowable<ChallengePairs> {
        return switchMap { challenges ->
            challengesProgressUseCase.categoriesWithProgress()
                .map { categories -> categories.flatMap { it.challenges }.toList() }
                .map { backendChallenges ->
                    challenges.map { challenge ->
                        challenge to backendChallenges.find { it.id == challenge.backendId }
                    }
                }
        }
    }

    private fun Flowable<ChallengePairs>.mapToEarnPointsChallenges(): Flowable<List<EarnPointsChallenge>> {
        return map { items ->
            items.mapNotNull { (challenge, backendChallenge) ->
                when {
                    // If BE challenge is available and not completed we want to display challenge
                    backendChallenge != null && !backendChallenge.isCompleted() ->
                        EarnPointsChallenge(challenge, backendChallenge.smilesReward)

                    // When showAllCards is enabled we also want to show ones that
                    // are not available on BE. In such case we will display points = -1
                    showAllCards ->
                        EarnPointsChallenge(challenge, backendChallenge?.smilesReward ?: -1)

                    // If there is no BE challenge available we don't wan't to display challenge
                    else -> null
                }
            }.sorted()
        }
    }

    companion object {

        @VisibleForTesting
        val CURRENTLY_SUPPORTED_CHALLENGES = setOf(
            COMPLETE_YOUR_PROFILE,
            TURN_ON_BRUSH_SYNC_REMINDERS,
            AMAZON_DASH
        )
    }
}
