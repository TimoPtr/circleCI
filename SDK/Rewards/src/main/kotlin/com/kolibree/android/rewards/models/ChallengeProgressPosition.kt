/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.models

import androidx.annotation.Keep

@Keep
data class ChallengeProgressPosition(
    val challengeWithProgress: ChallengeWithProgress,
    val positionX: Float = CHALLENGE_PROGRESS_NO_POSITION,
    val positionY: Float = CHALLENGE_PROGRESS_NO_POSITION
)

@Keep
const val CHALLENGE_PROGRESS_NO_POSITION = -1f
