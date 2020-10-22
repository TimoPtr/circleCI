/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.tab.activities.card.games.GamesCardViewModel
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.tracker.BottomNavigationEventTracker
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import javax.inject.Inject
import timber.log.Timber

private typealias State = ActivitiesViewState

@SuppressLint("ExperimentalClassUse")
internal class ActivitiesViewModel(
    initialViewState: State,
    private val startNonUnityGameUseCase: StartNonUnityGameUseCase,
    val toolbarViewModel: HomeToolbarViewModel,
    val gamesCardViewModel: GamesCardViewModel
) : BaseViewModel<State, HomeScreenAction>(
    initialViewState,
    children = setOf(toolbarViewModel, gamesCardViewModel)
) {

    val testBrushingTask = mapNonNull<State, TaskViewState>(
        viewStateLiveData,
        initialViewState.testBrushingTask
    ) { state ->
        state.testBrushingTask
    }

    val testSpeedTask = mapNonNull<State, TaskViewState>(
        viewStateLiveData,
        initialViewState.testSpeedTask
    ) { state ->
        state.testSpeedTask
    }

    val testAngleTask = mapNonNull<State, TaskViewState>(
        viewStateLiveData,
        initialViewState.testAngleTask
    ) { state ->
        state.testAngleTask
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        BottomNavigationEventTracker.activitiesVisible()
    }

    fun testBrushingClick() {
        ActivitiesEventTracker.testBrushingClick()
        startActivityGame(ActivityGame.TestBrushing)
    }

    fun testSpeedClick() {
        ActivitiesEventTracker.mindYourSpeedClick()
        startActivityGame(ActivityGame.SpeedControl)
    }

    fun testAngleClick() {
        ActivitiesEventTracker.adjustBrushingAngleClick()
        startActivityGame(ActivityGame.TestAngles)
    }

    fun guidedBrushingClick() {
        ActivitiesEventTracker.guidedBrushingClick()
        startActivityGame(ActivityGame.CoachPlus)
    }

    fun startActivityGame(game: ActivityGame) {
        if (isNonUnityGame(game)) {
            tryStartNonUnityGame(game)
        } else {
            pushAction(StartGame(game))
        }
    }

    private fun tryStartNonUnityGame(game: ActivityGame) {
        disposeOnDestroy {
            startNonUnityGameUseCase.start(game)
                .subscribe({}, Timber::e)
        }
    }

    @VisibleForTesting
    fun isNonUnityGame(game: ActivityGame): Boolean {
        return game.isUnityGame.not()
    }

    class Factory @Inject constructor(
        private val startNonUnityGameUseCase: StartNonUnityGameUseCase,
        private val toolbarViewModel: HomeToolbarViewModel,
        private val featureToggles: FeatureToggleSet,
        private val gamesCardViewModel: GamesCardViewModel
    ) : BaseViewModel.Factory<ActivitiesViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ActivitiesViewModel(
            viewState ?: State.initial(
                featureToggles.toggleForFeature(ShowMindYourSpeedFeature).value
            ),
            startNonUnityGameUseCase,
            toolbarViewModel,
            gamesCardViewModel
        ) as T
    }
}
