/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback.personal

import androidx.annotation.WorkerThread
import com.kolibree.android.rewards.logic.R
import com.kolibree.android.rewards.persistence.SmilesHistoryEventsDao
import javax.inject.Inject

internal class CompletedPersonalChallengeProvider @Inject constructor(
    private val smilesHistoryEventsDao: SmilesHistoryEventsDao
) {
    @WorkerThread
    fun provide(ids: List<Long>): List<ChallengeCompleted> {
        val personalChallengeIds = ids.map {
            toIdFromPersonalChallengeId(
                it
            )
        }
        return smilesHistoryEventsDao.read(personalChallengeIds).map {
            PersonalChallengeCompleted(
                id = it.id,
                smilesReward = it.smiles,
                greetingMessageRes = R.string.personal_challenge_completed_message_reference,
                nameRes = R.string.personal_challenge_completed_title_reference
            )
        }
    }
}

internal const val PERSONAL_CHALLENGE_START_ID = 1000000L

internal fun isPersonalChallengeId(id: Long) = id >= PERSONAL_CHALLENGE_START_ID

internal fun toPersonalChallengeId(id: Long) = id + PERSONAL_CHALLENGE_START_ID

internal fun toIdFromPersonalChallengeId(personalChallengeId: Long) =
    personalChallengeId - PERSONAL_CHALLENGE_START_ID
