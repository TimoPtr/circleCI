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
import com.kolibree.android.game.Game
import com.kolibree.android.rewards.personalchallenge.domain.logic.lastTenDaysOnly
import com.kolibree.android.rewards.personalchallenge.domain.logic.lastThreeDaysOnly
import com.kolibree.android.rewards.personalchallenge.domain.logic.lastTwoWeeksOnly
import com.kolibree.android.rewards.personalchallenge.domain.logic.lastWeekOnly
import com.kolibree.android.rewards.personalchallenge.domain.logic.secondWeekOnly
import org.threeten.bp.LocalDate

@VisibleForApp
data class BrushingStats(
    val firstBrushingDate: LocalDate?,
    val lastMonth: List<BrushingStat>,
    val allBrushing: Int
) {
    val lastWeek: List<BrushingStat> by lazy { lastWeekOnly(lastMonth) }
    val lastThreeDays: List<BrushingStat> by lazy { lastThreeDaysOnly(lastMonth) }
    val lastTenDays: List<BrushingStat> by lazy { lastTenDaysOnly(lastMonth) }
    val lastTwoWeeks: List<BrushingStat> by lazy { lastTwoWeeksOnly(lastMonth) }
    val secondWeek: List<BrushingStat> by lazy { secondWeekOnly(lastMonth) }
}

@VisibleForApp
data class BrushingStat(
    val type: BrushingType,
    val date: LocalDate,
    val coverage: Int
)

@VisibleForApp
enum class BrushingType {
    CoachedBrushing,
    OfflineBrushing,
    Other;

    @VisibleForApp
    companion object {

        fun from(name: String?) = when (Game.lookup(name ?: "")) {
            Game.COACH, Game.COACH_PLUS -> CoachedBrushing
            Game.OFFLINE -> OfflineBrushing
            else -> Other
        }
    }
}
