/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities.card.games

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import javax.inject.Inject
import timber.log.Timber

internal class GamesCardViewModel(
    viewState: GamesCardViewState,
    private val getItemsUseCase: GetGamesCardItemsUseCase
) : BaseViewModel<GamesCardViewState, HomeScreenAction>(viewState),
    GamesCardInteraction {

    val cardVisible = mapNonNull(viewStateLiveData, viewState.cardVisible) { viewState ->
        viewState.cardVisible
    }

    val items = mapNonNull(viewStateLiveData, viewState.items) { viewState ->
        viewState.items
    }

    init {
        disposeOnCleared {
            getItemsUseCase.getItems()
                .subscribe(::updateItems, Timber::e)
        }
    }

    override fun onCardClick(item: GamesCardItem) {
        Timber.d("On card click: $item")
    }

    private fun updateItems(newItems: List<GamesCardItem>) {
        updateViewState { copy(items = newItems) }
    }

    class Factory @Inject constructor(
        private val getItemsUseCase: GetGamesCardItemsUseCase
    ) : BaseViewModel.Factory<GamesCardViewState>() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GamesCardViewModel(
                viewState ?: GamesCardViewState.initial(),
                getItemsUseCase
            ) as T
    }
}
