/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.isActive
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level3Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level6Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewDays
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewWeeks
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelDiscrete
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelPercentage
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelUnknown
import io.reactivex.Single
import javax.inject.Inject

@VisibleForApp
interface BatteryLevelUseCase {
    fun batteryLevel(connection: KLTBConnection): Single<BatteryLevel>
}

internal class BatteryLevelUseCaseImpl @Inject constructor() : BatteryLevelUseCase {

    override fun batteryLevel(connection: KLTBConnection): Single<BatteryLevel> {
        return when {
            !connection.isActive() -> Single.just(LevelUnknown)
            usesDiscreteLevels(connection) -> readDiscreteBatteryState(connection)
            else -> readBatteryState(connection)
        }
    }

    private fun usesDiscreteLevels(connection: KLTBConnection): Boolean {
        return connection.toothbrush().battery().usesDiscreteLevels
    }

    private fun readDiscreteBatteryState(connection: KLTBConnection): Single<BatteryLevel> {
        return connection
            .toothbrush()
            .battery()
            .discreteBatteryLevel
            .map(::discreteBatteryLevel)
    }

    private fun readBatteryState(connection: KLTBConnection): Single<BatteryLevel> {
        return connection
            .toothbrush()
            .battery()
            .batteryLevel
            .map(::LevelPercentage)
    }

    @VisibleForTesting
    fun discreteBatteryLevel(level: DiscreteBatteryLevel): LevelDiscrete =
        LevelDiscrete(
            when (level) {
                DiscreteBatteryLevel.BATTERY_6_MONTHS -> Level6Month
                DiscreteBatteryLevel.BATTERY_3_MONTHS -> Level3Month
                DiscreteBatteryLevel.BATTERY_FEW_WEEKS -> LevelFewWeeks
                else -> LevelFewDays
            }
        )
}

@VisibleForApp
sealed class BatteryLevel {

    @VisibleForApp
    enum class Discrete {
        Level6Month, Level3Month, LevelFewWeeks, LevelFewDays
    }

    @VisibleForApp
    data class LevelDiscrete(val discrete: Discrete) : BatteryLevel()

    @VisibleForApp
    data class LevelPercentage(val value: Int) : BatteryLevel()

    @VisibleForApp
    object LevelUnknown : BatteryLevel()
}
