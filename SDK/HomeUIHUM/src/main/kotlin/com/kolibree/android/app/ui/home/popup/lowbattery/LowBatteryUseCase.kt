/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.lowbattery

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
interface LowBatteryUseCase {
    fun isMatchingWarningRequirement(batteryLevel: BatteryLevel): Single<Boolean>

    fun setWarningShown(): Completable
}

internal class LowBatteryUseCaseImpl @Inject constructor(
    private val lowBatteryProvider: LowBatteryProvider
) : LowBatteryUseCase {

    override fun isMatchingWarningRequirement(batteryLevel: BatteryLevel): Single<Boolean> {
        val isBatteryLow = isBatteryLow(batteryLevel)
        val warningShown = lowBatteryProvider.isWarningShown()

        return when {
            // If the battery is low and the warning has not been shown, the requirement is met
            isBatteryLow && !warningShown -> Single.just(true)

            // If the battery is not low but a warning has been shown, reset the warning state
            !isBatteryLow && warningShown ->
                resetWarningShown().toSingleDefault(false)

            // Else, it does nothing if the battery is not low or if the warning has been shown
            else -> Single.just(false)
        }
            .also { logState(batteryLevel, warningShown) }
    }

    private fun logState(batteryLevel: BatteryLevel, warningShown: Boolean) {
        Timber.i("BatteryLevel : $batteryLevel")
        Timber.i("Has Low Battery warning been shown : $warningShown")
    }

    override fun setWarningShown() =
        Completable.fromCallable { lowBatteryProvider.setWarningShown(true) }

    private fun resetWarningShown() =
        Completable.fromCallable { lowBatteryProvider.setWarningShown(false) }

    private fun isBatteryLow(batteryLevel: BatteryLevel): Boolean =
        when (batteryLevel) {
            is BatteryLevel.LevelDiscrete -> batteryLevel.discrete == BatteryLevel.Discrete.LevelFewDays
            is BatteryLevel.LevelPercentage -> batteryLevel.value <= WARNING_THRESHOLD
            is BatteryLevel.LevelUnknown -> false
        }
}

private const val WARNING_THRESHOLD = 15
