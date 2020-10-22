/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushing

import android.annotation.SuppressLint
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.failearly.FailEarly
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

@SuppressLint("DeobfuscatedPublicSdkClass")
interface BrushingsForCurrentProfileUseCase {
    fun getBrushingCount(): Flowable<Long>
    fun getBrushingOfflineCount(): Flowable<Long>
    fun getBrushingCount(game: ActivityGame, inManualMode: Boolean = false): Flowable<Long>
}

internal class BrushingsForCurrentProfileUseCaseImpl @Inject constructor(
    private val brushingRepository: BrushingsRepository,
    private val profileProvider: CurrentProfileProvider
) : BrushingsForCurrentProfileUseCase {

    override fun getBrushingCount(): Flowable<Long> {
        return profileProvider.currentProfileFlowable()
            .flatMapSingle { profile ->
                brushingRepository.countBrushings(profile.id)
            }
    }

    override fun getBrushingOfflineCount(): Flowable<Long> {
        return profileProvider.currentProfileFlowable()
            .flatMapSingle { profile ->
                brushingRepository.countBrushings(GameApiConstants.GAME_OFFLINE, profile.id)
            }
    }

    override fun getBrushingCount(game: ActivityGame, inManualMode: Boolean): Flowable<Long> {
        return profileProvider.currentProfileFlowable()
            .flatMapSingle { profile ->
                getGameBrushingConstraint(game, inManualMode)?.let { gameId ->
                    brushingRepository.countBrushings(gameId, profile.id)
                } ?: Single.just(0L)
            }
    }

    companion object {
        @VisibleForApp
        fun getGameBrushingConstraint(game: ActivityGame, inManualMode: Boolean): String? =
            when {
                game == ActivityGame.TestBrushing -> GameApiConstants.GAME_SBA
                game == ActivityGame.Pirate -> GameApiConstants.GAME_GO_PIRATE
                game == ActivityGame.Rabbids -> GameApiConstants.GAME_RABBIDS
                game == ActivityGame.CoachPlus && inManualMode -> GameApiConstants.GAME_COACH_MANUAL
                game == ActivityGame.CoachPlus -> GameApiConstants.GAME_COACH_PLUS
                game == ActivityGame.Coach && inManualMode -> GameApiConstants.GAME_COACH_MANUAL
                game == ActivityGame.Coach -> GameApiConstants.GAME_COACH
                else -> {
                    FailEarly.fail("Unsupported activity $game (with manual mode $inManualMode)")
                    null
                }
            }
    }
}
