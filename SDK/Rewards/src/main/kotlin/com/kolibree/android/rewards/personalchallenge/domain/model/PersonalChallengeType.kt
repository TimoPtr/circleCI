/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.model

import com.kolibree.android.annotation.VisibleForApp

/**
 * Determines the type of objective user wants to achieve with the challenge
 * @see https://kolibree.atlassian.net/wiki/spaces/PROD/pages/30998556/Personal+Challenge
 */
@VisibleForApp
enum class PersonalChallengeType {
    STREAK,
    COVERAGE,
    DURATION,
    COACH_PLUS,
    OFFLINE;
}
