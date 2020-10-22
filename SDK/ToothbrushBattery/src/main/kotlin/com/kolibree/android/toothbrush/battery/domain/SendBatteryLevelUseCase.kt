/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.toothbrush.battery.data.model.SendBatteryLevelRequest
import com.kolibree.android.toothbrush.battery.data.model.ToothbrushBatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level3Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level6Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewDays
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewWeeks
import io.reactivex.Completable
import javax.inject.Inject

internal interface SendBatteryLevelUseCase {

    /**
     * Schedules work that will send battery level
     * as soon as internet connection is available.
     */
    fun sendBatteryLevel(
        toothbrushBatteryLevel: ToothbrushBatteryLevel
    ): Completable
}

internal class SendBatteryLevelUseCaseImpl @Inject constructor(
    private val accountDatastore: AccountDatastore,
    private val workerConfigurator: SendBatteryLevelWorker.Configurator
) : SendBatteryLevelUseCase {

    override fun sendBatteryLevel(
        toothbrushBatteryLevel: ToothbrushBatteryLevel
    ): Completable {
        return accountDatastore
            .getAccountMaybe()
            .flatMapCompletable { account ->
                workerConfigurator.sendBatteryLevel(
                    accountId = account.id,
                    profileId = account.currentProfileId ?: error("Profile id is missing!"),
                    request = SendBatteryLevelRequest(
                        macAddress = StrippedMac.fromMac(toothbrushBatteryLevel.macAddress),
                        serialNumber = toothbrushBatteryLevel.serialNumber,
                        discreteLevel = toothbrushBatteryLevel.getDiscreteValue()
                    )
                )
            }
    }

    private fun ToothbrushBatteryLevel.getDiscreteValue(): Int {
        return when (batteryLevel) {
            !is BatteryLevel.LevelDiscrete -> DISCRETE_LEVEL_BATTERY_UNKNOWN
            else -> when (batteryLevel.discrete) {
                Level6Month -> DISCRETE_LEVEL_BATTERY_6_MONTHS
                Level3Month -> DISCRETE_LEVEL_BATTERY_3_MONTHS
                LevelFewWeeks -> DISCRETE_LEVEL_BATTERY_FEW_WEEKS
                LevelFewDays -> DISCRETE_LEVEL_BATTERY_FEW_DAYS
            }
        }
    }
}

/**
 * https://kolibree.atlassian.net/wiki/spaces/PROD/pages/2735816/Toothbrush+battery+management
 */
internal const val DISCRETE_LEVEL_BATTERY_6_MONTHS = 0
internal const val DISCRETE_LEVEL_BATTERY_3_MONTHS = 1
internal const val DISCRETE_LEVEL_BATTERY_FEW_WEEKS = 2
internal const val DISCRETE_LEVEL_BATTERY_FEW_DAYS = 3
internal const val DISCRETE_LEVEL_BATTERY_UNKNOWN = 6
