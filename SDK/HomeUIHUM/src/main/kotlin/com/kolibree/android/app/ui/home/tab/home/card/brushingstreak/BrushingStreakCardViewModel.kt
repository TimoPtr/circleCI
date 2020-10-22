/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.transition.TransitionManager
import com.google.common.base.Optional
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.rewards.personalchallenge.logic.HumChallengeUseCase
import com.kolibree.android.rewards.personalchallenge.presentation.CompletedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallengeRecommendationAction
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.OnGoingChallenge
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class BrushingStreakCardViewModel(
    initialViewState: BrushingStreakCardViewState,
    private val humChallengeUseCase: HumChallengeUseCase,
    private val homeNavigator: HumHomeNavigator,
    private val startNonUnityGameUseCase: StartNonUnityGameUseCase
) : DynamicCardViewModel<
    BrushingStreakCardViewState,
    BrushingStreakCardInteraction,
    BrushingStreakCardBindingModel>(initialViewState), BrushingStreakCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is BrushingStreakCardBindingModel

    override fun onAcceptChallengeClick(challenge: NotAcceptedChallenge) {
        BrushingStreakAnalytics.accept(challenge.isMoreThanOneDay())

        disposeOnCleared {
            humChallengeUseCase.acceptChallenge(challenge)
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::e)
        }
    }

    override fun onActionClick(challenge: OnGoingChallenge) {
        BrushingStreakAnalytics.action()

        if (challenge.action == HumChallengeRecommendationAction.COACH_PLUS) {
            startCoach()
        }
    }

    override fun onCompleteChallengeClick(challenge: CompletedChallenge) {
        BrushingStreakAnalytics.complete(challenge.isMoreThanOneDay())

        disposeOnCleared {
            humChallengeUseCase.completeChallenge(challenge)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    homeNavigator.showChallengeCompletedScreen(challenge.smiles)
                }, Timber::e)
        }
    }

    override fun toggleExpanded(view: View) {
        TransitionManager.beginDelayedTransition(view as ViewGroup)

        if (getViewState()?.isExpanded == false) {
            onCardExpanded()
        }

        updateViewState {
            copy(isExpanded = !isExpanded)
        }
    }

    @VisibleForTesting
    fun onCardExpanded() {
        BrushingStreakAnalytics.open()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop {
            humChallengeUseCase.challengeStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::refreshChallenge, Timber::e)
        }
    }

    private fun refreshChallenge(optionalChallenge: Optional<HumChallenge>) {
        when (val challenge = optionalChallenge.orNull()) {
            null -> updateViewState { copy(visible = false) }
            else -> updateViewState { withChallenge(challenge) }
        }
    }

    private fun startCoach() {
        disposeOnDestroy {
            startNonUnityGameUseCase.start(ActivityGame.CoachPlus, allowManualMode = false)
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::e)
        }
    }

    class Factory @Inject constructor(
        private val humChallengeUseCase: HumChallengeUseCase,
        private val homeNavigator: HumHomeNavigator,
        private val startNonUnityGameUseCase: StartNonUnityGameUseCase,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<BrushingStreakCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BrushingStreakCardViewModel(
            viewState ?: BrushingStreakCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            humChallengeUseCase,
            homeNavigator,
            startNonUnityGameUseCase
        ) as T
    }
}
