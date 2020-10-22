/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.game

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.app.ui.selecttoothbrush.SelectToothbrushUseCase
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import io.reactivex.Completable
import io.reactivex.Maybe
import javax.inject.Inject

@VisibleForApp
interface StartNonUnityGameUseCase {
    fun start(game: ActivityGame, allowManualMode: Boolean = true): Completable
}

internal class StartNonUnityGameUseCaseImpl @Inject constructor(
    private val selectToothbrushUseCase: SelectToothbrushUseCase,
    private val homeNavigator: HomeNavigator
) : StartNonUnityGameUseCase {

    override fun start(game: ActivityGame, allowManualMode: Boolean): Completable {
        return selectToothbrushUseCase
            .selectToothbrush()
            // Toothbrush not available or not selected
            .switchIfEmpty(
                Maybe.fromCompletable(showNoToothbrushOrManualMode(game, allowManualMode))
            )
            // Toothbrush selected
            .flatMapCompletable { selectedToothbrush ->
                validateAndStart(game, selectedToothbrush.toothbrush())
            }
    }

    private fun showNoToothbrushOrManualMode(
        game: ActivityGame,
        allowManualMode: Boolean
    ): Completable {
        return Completable.fromAction {
            if (hasManualMode(game) && allowManualMode) {
                startManualModeActivity(game)
            } else {
                homeNavigator.showNoToothbrushDialog()
            }
        }
    }

    @VisibleForTesting
    fun validateAndStart(game: ActivityGame, toothbrush: Toothbrush): Completable {
        return Completable.fromAction {
            if (toothbrush.isRunningBootloader) {
                homeNavigator.showMandatoryToothbrushUpdateDialog(toothbrush.mac, toothbrush.model)
            } else {
                startUserActivity(game, toothbrush.mac, toothbrush.model)
            }
        }
    }

    @VisibleForTesting
    fun startManualModeActivity(game: ActivityGame) {
        when (game) {
            ActivityGame.Coach -> homeNavigator.showCoachInManualMode()
            ActivityGame.CoachPlus -> homeNavigator.showCoachPlusInManualMode()
            else -> FailEarly.fail("No manual mode for $game")
        }
    }

    @VisibleForTesting
    fun hasManualMode(game: ActivityGame) = game.hasManualMode

    @VisibleForTesting
    fun startUserActivity(game: ActivityGame, mac: String, model: ToothbrushModel) {
        when (game) {
            ActivityGame.TestBrushing -> homeNavigator.showTestBrushing(mac, model)
            ActivityGame.SpeedControl -> homeNavigator.showSpeedControl(mac, model)
            ActivityGame.TestAngles -> homeNavigator.showTestAngles(mac, model)
            ActivityGame.CoachPlus -> homeNavigator.showCoachPlus(mac, model)
            ActivityGame.Coach -> homeNavigator.showCoach(mac)
            else -> FailEarly.fail("$game is not supported here!")
        }
    }
}
