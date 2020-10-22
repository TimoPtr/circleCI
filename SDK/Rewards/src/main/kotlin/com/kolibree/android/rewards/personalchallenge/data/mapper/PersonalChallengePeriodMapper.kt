/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.mapper

import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import org.threeten.bp.temporal.ChronoUnit

private const val DURATION_UNIT_DAY = "day"

@Throws(IllegalStateException::class)
internal fun PersonalChallengePeriod.stringifyDuration(): Long = when (unit) {
    ChronoUnit.DAYS -> duration.toDays()
    else -> throw IllegalStateException("We don't support $unit yet!")
}

@Throws(IllegalStateException::class)
internal fun PersonalChallengePeriod.stringifyUnit(): String = when (unit) {
    ChronoUnit.DAYS -> DURATION_UNIT_DAY
    else -> throw IllegalStateException("We don't support $unit yet!")
}

@Throws(IllegalArgumentException::class)
internal fun periodFromStringedData(
    duration: Long,
    durationUnit: String
): PersonalChallengePeriod {
    require(durationUnit == DURATION_UNIT_DAY) {
        "We do not support challenges with duration unit $durationUnit!"
    }
    return when (duration) {
        PersonalChallengePeriod.ONE_DAY.duration.toDays() -> PersonalChallengePeriod.ONE_DAY
        PersonalChallengePeriod.TWO_DAYS.duration.toDays() -> PersonalChallengePeriod.TWO_DAYS
        PersonalChallengePeriod.FIVE_DAYS.duration.toDays() -> PersonalChallengePeriod.FIVE_DAYS
        PersonalChallengePeriod.SEVEN_DAYS.duration.toDays() -> PersonalChallengePeriod.SEVEN_DAYS
        PersonalChallengePeriod.FOURTEEN_DAYS.duration.toDays() -> PersonalChallengePeriod.FOURTEEN_DAYS
        PersonalChallengePeriod.THIRTY_DAYS.duration.toDays() -> PersonalChallengePeriod.THIRTY_DAYS
        else -> throw IllegalArgumentException("Unsupported challenge duration: $duration $durationUnit")
    }
}
