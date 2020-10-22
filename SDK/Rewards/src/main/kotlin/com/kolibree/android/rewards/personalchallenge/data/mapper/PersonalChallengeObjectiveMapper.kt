/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.mapper

import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType

@Suppress("TopLevelPropertyNaming")
private val OBJECTIVE_TO_JSON_STRING = mapOf(
    PersonalChallengeType.STREAK to "streak",
    PersonalChallengeType.COVERAGE to "coverage",
    PersonalChallengeType.DURATION to "duration",
    PersonalChallengeType.COACH_PLUS to "co+",
    PersonalChallengeType.OFFLINE to "of"
)

@Throws(NoSuchElementException::class)
internal fun PersonalChallengeType.stringify(): String =
    OBJECTIVE_TO_JSON_STRING[this] ?: throw NoSuchElementException("No value for level '$this'!")

@Throws(NoSuchElementException::class)
internal fun objectiveFromJsonString(value: String): PersonalChallengeType =
    OBJECTIVE_TO_JSON_STRING.entries.first { it.value == value }.key
