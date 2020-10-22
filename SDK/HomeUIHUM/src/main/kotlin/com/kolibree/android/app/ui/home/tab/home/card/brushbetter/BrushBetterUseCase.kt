/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.ADJUST_BRUSHING_ANGLE
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.GUIDED_BRUSHING
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.MIND_YOUR_SPEED
import com.kolibree.android.app.ui.home.tab.home.card.brushbetter.BrushBetterItem.TEST_BRUSHING
import com.kolibree.android.feature.FeatureToggle
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

internal interface BrushBetterUseCase {
    fun getItems(): Observable<List<BrushBetterItem>>
    fun onItemClick(item: BrushBetterItem): Completable
}

internal class BrushBetterUseCaseImpl @Inject constructor(
    private val startNonUnityGameUseCase: StartNonUnityGameUseCase,
    private val mindYourSpeedFeatureToggle: FeatureToggle<Boolean>
) : BrushBetterUseCase {

    /**
     * By default returns all available [BrushBetterItem]s
     *
     * [ADJUST_BRUSHING_ANGLE] was removed on purpose
     */
    override fun getItems(): Observable<List<BrushBetterItem>> {
        val withMindYourSpeed = mindYourSpeedFeatureToggle.value

        val items = if (withMindYourSpeed) {
            listOf(GUIDED_BRUSHING, TEST_BRUSHING, MIND_YOUR_SPEED)
        } else {
            listOf(GUIDED_BRUSHING, TEST_BRUSHING)
        }
        return Observable.just(items)
    }

    override fun onItemClick(item: BrushBetterItem): Completable {
        item.sendAnalytics()
        return item.startGame()
    }

    private fun BrushBetterItem.sendAnalytics() {
        when (this) {
            GUIDED_BRUSHING -> BrushBetterAnalytics.guidedBrushing()
            MIND_YOUR_SPEED -> BrushBetterAnalytics.mindYourSpeed()
            ADJUST_BRUSHING_ANGLE -> BrushBetterAnalytics.adjustBrushingAngle()
            TEST_BRUSHING -> BrushBetterAnalytics.testBrushing()
        }
    }

    private fun BrushBetterItem.startGame(): Completable {
        val game = when (this) {
            GUIDED_BRUSHING -> ActivityGame.CoachPlus
            TEST_BRUSHING -> ActivityGame.TestBrushing
            MIND_YOUR_SPEED -> ActivityGame.SpeedControl
            ADJUST_BRUSHING_ANGLE -> ActivityGame.TestAngles
        }

        return startNonUnityGameUseCase.start(game)
    }
}
