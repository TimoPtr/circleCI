/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.HomeSessionFlag
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.rewards.CurrentProfileCategoriesWithProgressUseCase
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.models.ChallengeWithProgress
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import io.reactivex.processors.BehaviorProcessor
import javax.inject.Inject

@VisibleForApp
interface CompleteProfileBubbleUseCase {

    fun getShowCompleteProfileBubbleStream(): Flowable<Boolean>

    fun getProfileCompletionPercentageStream(): Flowable<Int>

    fun suppressBubble()
}

internal class CompleteProfileBubbleUseCaseImpl @Inject constructor(
    private val challengesProgressUseCase: CurrentProfileCategoriesWithProgressUseCase,
    private val sessionFlags: SessionFlags
) : CompleteProfileBubbleUseCase {

    private val allowBubbleToAppearRelay = BehaviorProcessor.createDefault(
        sessionFlags.readSessionFlag(HomeSessionFlag.SHOW_PROFILE_INCOMPLETE_BUBBLE) ?: true
    )

    override fun getShowCompleteProfileBubbleStream(): Flowable<Boolean> =
        Flowable.combineLatest(
            allowBubbleToAppearRelay,
            completeProfileChallengeIsAvailable(),
            completeProfileChallengeIsCompleted(),
            Function3<Boolean, Boolean, Boolean, Boolean> { bubbleIsAllowed, available, complete ->
                bubbleIsAllowed && available && !complete
            }
        ).distinctUntilChanged()

    override fun getProfileCompletionPercentageStream(): Flowable<Int> =
        challengesProgressUseCase.categoriesWithProgress()
            .map { categoriesWithProgress -> findCompleteProfileChallenge(categoriesWithProgress) }
            .map { challengeOptional -> challengeOptional.orNull()?.percentage ?: 0 }
            .distinctUntilChanged()

    override fun suppressBubble() {
        sessionFlags.setSessionFlag(HomeSessionFlag.SHOW_PROFILE_INCOMPLETE_BUBBLE, false)
        allowBubbleToAppearRelay.onNext(false)
    }

    private fun completeProfileChallengeIsAvailable(): Flowable<Boolean> =
        challengesProgressUseCase.categoriesWithProgress()
            .map { categoriesWithProgress -> findCompleteProfileChallenge(categoriesWithProgress) }
            .map { challengeOptional -> challengeOptional.isPresent }

    private fun completeProfileChallengeIsCompleted(): Flowable<Boolean> =
        challengesProgressUseCase.categoriesWithProgress()
            .map { categoriesWithProgress -> findCompleteProfileChallenge(categoriesWithProgress) }
            .map { challengeOptional -> challengeOptional.orNull()?.isCompleted() ?: false }

    private fun findCompleteProfileChallenge(
        categoriesWithProgress: List<CategoryWithProgress>
    ): Optional<ChallengeWithProgress> {
        return Optional.fromNullable(categoriesWithProgress
            .flatMap { it.challenges }
            .toList()
            .firstOrNull { it.id == EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE.backendId }
        )
    }
}
