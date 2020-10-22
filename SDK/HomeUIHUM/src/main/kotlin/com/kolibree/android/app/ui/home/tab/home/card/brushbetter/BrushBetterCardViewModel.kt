/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class BrushBetterCardViewModel(
    initialViewState: BrushBetterCardViewState,
    private val brushBetterResourceProvider: BrushBetterResourceProvider,
    private val brushBetterUseCase: BrushBetterUseCase,
    private val pulsingDotUseCase: PulsingDotUseCase,
    private val toolboxViewModel: ToolboxViewModel
) : DynamicCardViewModel<
    BrushBetterCardViewState,
    BrushBetterCardInteraction,
    BrushBetterCardBindingModel>(initialViewState), BrushBetterCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is BrushBetterCardBindingModel

    override fun onItemClick(item: BrushBetterItem) {
        disposeOnDestroy {
            brushBetterUseCase.onItemClick(item)
                .subscribe({}, Timber::e)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrieveItems)
        disposeOnDestroy(::retrievePulsingDotStatus)
    }

    override fun onPulsingDotClick() {
        pulsingDotUseCase.onPulsingDotClicked(PulsingDot.BRUSH_BETTER)
        toolboxViewModel.show(toolboxViewModel.factory().brushingActivities())
    }

    private fun retrieveItems(): Disposable {
        return brushBetterUseCase.getItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::setItems, Timber::e)
    }

    private fun setItems(items: List<BrushBetterItem>) {
        updateViewState {
            copy(items = items.map(brushBetterResourceProvider::createItemBinding))
        }
    }

    private fun retrievePulsingDotStatus(): Disposable {
        return pulsingDotUseCase.shouldShowPulsingDot(PulsingDot.BRUSH_BETTER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onPulsingDotStatusRetrieved, Timber::e)
    }

    private fun onPulsingDotStatusRetrieved(shouldShow: Boolean) {
        updateViewState { copy(pulsingDotVisible = shouldShow) }
    }

    class Factory @Inject constructor(
        private val brushBetterResourceProvider: BrushBetterResourceProvider,
        private val brushBetterUseCase: BrushBetterUseCase,
        private val pulsingDotUseCase: PulsingDotUseCase,
        private val toolboxViewModel: ToolboxViewModel,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<BrushBetterCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BrushBetterCardViewModel(
            viewState ?: BrushBetterCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            brushBetterResourceProvider,
            brushBetterUseCase,
            pulsingDotUseCase,
            toolboxViewModel
        ) as T
    }
}
