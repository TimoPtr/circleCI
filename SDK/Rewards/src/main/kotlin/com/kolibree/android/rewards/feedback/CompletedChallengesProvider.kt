/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.rewards.feedback.personal.BackendChallengeCompleted
import com.kolibree.android.rewards.feedback.personal.ChallengeCompleted
import com.kolibree.android.rewards.feedback.personal.CompletedPersonalChallengeProvider
import com.kolibree.android.rewards.feedback.personal.isPersonalChallengeId
import com.kolibree.android.rewards.persistence.ChallengesDao
import javax.inject.Inject

internal class CompletedChallengesProvider @Inject constructor(
    private val challengesDao: ChallengesDao,
    private val personalChallengeProvider: CompletedPersonalChallengeProvider
) {
    fun provide(ids: List<Long>): List<ChallengeCompleted> {
        val challengeIds = ids.filter { !isPersonalChallengeId(it) }
        val challenges = challengesDao.read(challengeIds).map {
            BackendChallengeCompleted(it)
        }
        val personalChallengeIds = ids.filter {
            isPersonalChallengeId(it)
        }
        val personalChallenges = personalChallengeProvider.provide(personalChallengeIds)
        return challenges + personalChallenges
    }
}
