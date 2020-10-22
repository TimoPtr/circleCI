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
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

/**
 * Determines the period of personal challenge
 * @param duration duration of the period
 * @see https://kolibree.atlassian.net/wiki/spaces/PROD/pages/30998556/Personal+Challenge
 */
@Suppress("MagicNumber")
@VisibleForApp
enum class PersonalChallengePeriod(
    val duration: Duration,
    val unit: ChronoUnit,
    val v2Only: Boolean
) {
    ONE_DAY(Duration.ofDays(1), ChronoUnit.DAYS, true),
    TWO_DAYS(Duration.ofDays(2), ChronoUnit.DAYS, true),
    FIVE_DAYS(Duration.ofDays(5), ChronoUnit.DAYS, true),
    SEVEN_DAYS(Duration.ofDays(7), ChronoUnit.DAYS, false),
    FOURTEEN_DAYS(Duration.ofDays(14), ChronoUnit.DAYS, false),
    THIRTY_DAYS(Duration.ofDays(30), ChronoUnit.DAYS, false);
}
