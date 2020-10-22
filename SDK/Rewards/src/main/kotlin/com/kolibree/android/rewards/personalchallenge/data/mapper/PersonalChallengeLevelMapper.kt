/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.mapper

import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel

@Suppress("TopLevelPropertyNaming")
private val TYPE_TO_JSON_STRING = mapOf(
    PersonalChallengeLevel.EASY to "easy",
    PersonalChallengeLevel.HARD to "hard"
)

@Throws(NoSuchElementException::class)
internal fun PersonalChallengeLevel.stringify(): String =
    TYPE_TO_JSON_STRING[this] ?: throw NoSuchElementException("No value for level '$this'!")

@Throws(NoSuchElementException::class)
internal fun levelFromStringedValue(value: String): PersonalChallengeLevel =
    TYPE_TO_JSON_STRING.entries.first { it.value == value }.key
