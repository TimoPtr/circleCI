/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot.SMILE
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterStateProvider
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SpeechBubbleAnimation
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.home.tracker.BottomNavigationEventTracker as Tracker
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class HomeFragmentViewModel(
    initialViewState: HomeViewState,
    val cardHostViewModel: DynamicCardHostViewModel,
    val toolbarViewModel: HomeToolbarViewModel,
    private val toolboxViewModel: ToolboxViewModel,
    private val pulsingDotUseCase: PulsingDotUseCase,
    private val smilesCounterStateProvider: SmilesCounterStateProvider,
    private val homeNavigator: HumHomeNavigator
) : BaseViewModel<HomeViewState, HomeScreenAction>(
    initialViewState,
    children = setOf(toolbarViewModel)
) {

    val currentPoints = mapNonNull(viewStateLiveData, 0) { viewState ->
        viewState.currentPoints
    }

    val smilesCounterState = map(viewStateLiveData) { viewState ->
        viewState?.smilesCounterState
    }

    val pulsingDotVisible: LiveData<Boolean?> =
        mapNonNull(viewStateLiveData, false) { viewState -> viewState.pulsingDotVisible }

    val smilesBackgroundAnimation: LiveData<LottieDelayedLoop?> =
        map(viewStateLiveData) { viewState ->
            viewState?.smilesBackgroundAnimation
        }

    val smilesRestartAnimation: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, false) { viewState ->
            viewState.restartAnimation
        }

    val animateSmilesSpeechBubble: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, false) { viewState ->
            viewState.restartAnimation &&
                viewState.smilesCounterState is SmilesCounterState.PlayIncrease
        }

    val animateNoInternet: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, false) { viewState ->
            viewState.smilesCounterState is SmilesCounterState.NoInternet
        }

    val pointsIncrease: LiveData<Int> =
        mapNonNull(viewStateLiveData, 0) { viewState ->
            viewState.smilesCounterState.let { smilesCounterState ->
                if (smilesCounterState is SmilesCounterState.PlayIncrease) {
                    smilesCounterState.finalPoints - smilesCounterState.initialPoints
                } else 0
            }
        }

    val speechBubbleAnimation: LiveData<SpeechBubbleAnimation<*>> =
        map(viewStateLiveData) { viewState ->
            viewState?.smilesCounterState?.let { smilesCounterState ->
                when (smilesCounterState) {
                    is SmilesCounterState.PlayIncrease -> SpeechBubbleAnimation.PointsIncrease(
                        smilesCounterState.finalPoints - smilesCounterState.initialPoints
                    )
                    SmilesCounterState.Pending -> SpeechBubbleAnimation.Pending
                    SmilesCounterState.NoInternet -> SpeechBubbleAnimation.NoInternet
                    else -> SpeechBubbleAnimation.Hide
                }
            }
        }

    val replayLabelVisibility: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, false) { viewState ->
            viewState.smilesCounterState is SmilesCounterState.PlayIncrease
        }

    val noInternetAnimation: LiveData<Boolean> = mapNonNull(viewStateLiveData, false) { viewState ->
        viewState.noInternetAnimation
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrievePulsingDot)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop(::listenToAccountPoints)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Tracker.dashboardVisible()
    }

    private fun listenToAccountPoints() =
        smilesCounterStateProvider.smilesStateObservable
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onSmileCounterStateUpdated,
                Timber::e
            )

    private fun onSmileCounterStateUpdated(newState: SmilesCounterState) {
        Timber.tag("SmilesState").d("New state $newState")
        updateViewState { withSmilesCounterState(newState) }
    }

    private fun retrievePulsingDot() =
        pulsingDotUseCase.shouldShowPulsingDot(SMILE)
            .subscribeOn(Schedulers.io())
            .subscribe(::onPulsingDotStatusRetrieved, Timber::e)

    private fun onPulsingDotStatusRetrieved(shouldShow: Boolean) {
        updateViewState {
            copy(pulsingDotVisible = shouldShow)
        }
    }

    fun onClickPulsingDot() {
        pulsingDotUseCase.onPulsingDotClicked(SMILE)
        toolboxViewModel.show(toolboxViewModel.factory().smilePoints())
    }

    fun onClickSmilesCounter() {
        homeNavigator.navigatesToSmilesHistory()
    }

    class Factory @Inject constructor(
        private val cardHostViewModel: DynamicCardHostViewModel,
        private val toolbarViewModel: HomeToolbarViewModel,
        private val toolboxViewModel: ToolboxViewModel,
        private val pulsingDotUseCase: PulsingDotUseCase,
        private val smilesCounterStateProvider: SmilesCounterStateProvider,
        private val homeNavigator: HumHomeNavigator
    ) : BaseViewModel.Factory<HomeViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeFragmentViewModel(
            initialViewState = viewState ?: HomeViewState.initial(),
            cardHostViewModel = cardHostViewModel,
            toolbarViewModel = toolbarViewModel,
            toolboxViewModel = toolboxViewModel,
            pulsingDotUseCase = pulsingDotUseCase,
            smilesCounterStateProvider = smilesCounterStateProvider,
            homeNavigator = homeNavigator
        ) as T
    }
}
