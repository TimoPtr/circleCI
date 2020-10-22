/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.charts.inoff.domain.InOffBrushingsCountProvider
import com.kolibree.charts.inoff.domain.model.InOffBrushingsCount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Stats card showing various data
 * - current points available
 * - lifetime points
 * - lifetime offline brushings
 * - lifetime in-app brushings
 *
 * @see <a href="https://kolibree.atlassian.net/wiki/spaces/PROD/pages/323649546/Stats">Stats</a>
 */
internal class LifetimeStatsCardViewModel(
    initialViewState: LifetimeStatsCardViewState,
    private val smilesUseCase: SmilesUseCase,
    private val lifetimeSmilesUseCase: LifetimeSmilesUseCase,
    private val inOffBrushingsCountProvider: InOffBrushingsCountProvider,
    private val currentProfileProvider: CurrentProfileProvider
) : DynamicCardViewModel<
    LifetimeStatsCardViewState,
    LifetimeStatsCardInteraction,
    LifetimeStatsCardBindingModel>(initialViewState),
    LifetimeStatsCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is LifetimeStatsCardBindingModel

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop(::listenToCurrentPoints)
        disposeOnStop(::listenToLifetimePoints)
        disposeOnStop(::listenToInOffBrushingsCounts)
    }

    private fun listenToInOffBrushingsCounts() =
        currentProfileProvider.currentProfileFlowable()
            .switchMap { inOffBrushingsCountProvider.brushingsCountStream(it.id) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onInOffBrushingsCountUpdated, Timber::e)

    private fun onInOffBrushingsCountUpdated(value: InOffBrushingsCount) {
        updateViewState {
            copy(
                isLoading = false,
                inAppCount = value.onlineBrushingCount,
                offlineCount = value.offlineBrushingCount
            )
        }
    }

    private fun listenToCurrentPoints() =
        smilesUseCase.smilesAmountStream()
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onCurrentSmilesUpdated,
                Timber::e
            )

    private fun onCurrentSmilesUpdated(value: Int) {
        updateViewState {
            copy(
                isLoading = false,
                currentPoints = value
            )
        }
    }

    private fun listenToLifetimePoints() =
        lifetimeSmilesUseCase.lifetimePoints()
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onLifetimeSmilesUpdated,
                Timber::e
            )

    private fun onLifetimeSmilesUpdated(value: Int) {
        updateViewState {
            copy(
                isLoading = false,
                lifetimePoints = value
            )
        }
    }

    class Factory @Inject constructor(
        private val smilesUseCase: SmilesUseCase,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration,
        private val lifetimeSmilesUseCase: LifetimeSmilesUseCase,
        private val inOffBrushingsCountProvider: InOffBrushingsCountProvider,
        private val currentProfileProvider: CurrentProfileProvider
    ) : BaseViewModel.Factory<LifetimeStatsCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LifetimeStatsCardViewModel(
                initialViewState = viewState ?: LifetimeStatsCardViewState.initial(
                    position = dynamicCardListConfiguration.getInitialCardPosition(modelClass)
                ),
                smilesUseCase = smilesUseCase,
                lifetimeSmilesUseCase = lifetimeSmilesUseCase,
                inOffBrushingsCountProvider = inOffBrushingsCountProvider,
                currentProfileProvider = currentProfileProvider
            ) as T
    }
}
