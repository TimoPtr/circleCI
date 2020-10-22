/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.host

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.card.DynamicCardViewModelSet
import com.kolibree.android.dynamiccards.BR
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel
import timber.log.Timber

/**
 * Acts as a aggregator for card view models and display their content in the dynamic list.
 *
 * Each card view model emits its updates which are aggregated in [DynamicCardHostViewState] and sent
 * to the view via [cards] live data.
 *
 * If you want to add new card, its view model has to be added to [DynamicCardViewModelSet] via
 * [@IntoSet] injection annotation. See [HomeCardModule] for details.
 *
 * NOTE: if you want to add a new card to the home view, this class doesn't need any changes.
 */
@VisibleForApp
class DynamicCardHostViewModel(
    initialViewState: DynamicCardHostViewState,
    children: DynamicCardViewModelSet
) : BaseViewModel<DynamicCardHostViewState, NoActions>(
    initialViewState,
    children = children
) {

    /**
     * Returns sorted list of cards to be displayed on the UI.
     * Only cards that return true from [DynamicCardBindingModel.visible] are present in the list.
     * This way DiffUtils can calculate proper diff when card visibility state changed.
     *
     * Because we're binding to RecyclerView, this is the only live data in the entire feature.
     * Card view models don't have live datas - instead they return their state updates here,
     * where [DynamicCardHostViewModel] can aggregate them and send them to view.
     */
    @SuppressLint("ExperimentalClassUse")
    val cards = mapNonNull<DynamicCardHostViewState, List<DynamicCardBindingModel>>(
        viewStateLiveData,
        initialViewState.cards
    ) { state -> state.cards }

    /**
     * Binding object, responsible for assignment of card view models to views on appropriate
     * position. Binds layout, binding model as [BR.item] and appropriate interaction as
     * [BR.interaction].
     */
    val cardsBinding = object : OnItemBindModel<DynamicCardBindingModel>() {

        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: DynamicCardBindingModel
        ) {
            super.onItemBind(itemBinding, position, item)
            cardViewModels()
                .firstOrNull { it.interaction.interactsWith(item) }
                ?.let { itemBinding.bindExtra(BR.interaction, it) }
        }
    }

    /**
     * Responsible for determination if:
     * - any card should appear
     * - any card should disappear
     * - content of the cards has to be updated.
     */
    val diffConfig = object : DiffUtil.ItemCallback<DynamicCardBindingModel>() {

        /* Models represent the same card when they have the same ID - layout ID */
        override fun areItemsTheSame(
            oldItem: DynamicCardBindingModel,
            newItem: DynamicCardBindingModel
        ): Boolean = oldItem.layoutId == newItem.layoutId

        /* Models has the exact same content when they have the hash code */
        override fun areContentsTheSame(
            oldItem: DynamicCardBindingModel,
            newItem: DynamicCardBindingModel
        ): Boolean = oldItem.hashCode() == newItem.hashCode()
    }

    /**
     * Because VM is preserved on configuration change, this is recommended way of keeping
     * the list in a correct position between rotations.
     */
    val adapter = BindingRecyclerViewAdapter<DynamicCardBindingModel>()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        subscribeToCardStateUpdates()
    }

    private fun subscribeToCardStateUpdates() {
        cardViewModels().forEach { cardViewModel ->
            disposeOnStop {
                cardViewModel.viewStateFlowable
                    .subscribe(
                        { updateViewState { copyWithUpdatedCardState(it.asBindingModel()) } },
                        Timber::e
                    )
            }
        }
    }

    private fun cardViewModels() = children.filterIsInstance<DynamicCardViewModel<*, *, *>>()

    @VisibleForApp
    class Factory @Inject constructor(
        private val cardViewModels: DynamicCardViewModelSet
    ) : BaseViewModel.Factory<DynamicCardHostViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DynamicCardHostViewModel(
            viewState
                ?: DynamicCardHostViewState.fromViewModels(
                    cardViewModels
                ),
            cardViewModels
        ) as T
    }
}
