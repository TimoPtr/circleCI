/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.rewards.BR
import com.kolibree.android.rewards.R
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindClass
import timber.log.Timber

internal class SmilesHistoryViewModel(
    private val smilesHistoryUseCase: SmilesHistoryUseCase,
    initialViewState: SmilesHistoryViewState
) : BaseViewModel<SmilesHistoryViewState, NoActions>(
    initialViewState
) {

    val smilesHistoryItemBinding: OnItemBindClass<SmilesHistoryListItem> =
        OnItemBindClass<SmilesHistoryListItem>()
            .map(ItemResources::class.java) { itemBinding, _, _ ->
                itemBinding.set(BR.itemRes, R.layout.item_smiles_history)
            }
            .map(SimilesHistoryHeaderListItem::class.java) { itemBinding, _, _ ->
                itemBinding.set(BR.header, R.layout.item_header_smiles_history) // Hack
            }

    // From back end
    val items: LiveData<List<SmilesHistoryListItem>> =
        mapNonNull(viewStateLiveData, initialViewState.itemsWithHeader) { viewState ->
            viewState.itemsWithHeader
        }

    val isEmpty: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, initialViewState.isEmpty()) { viewState ->
            viewState.isEmpty()
        }

    init {
        disposeOnCleared {
            smilesHistoryUseCase.smilesHistoryStream()
                .subscribeOn(Schedulers.io())
                .subscribe(::onSmilesHistoryReceived, Timber::e)
        }
    }

    fun onBackPressed() {
        SmilesHistoryAnalytics.quit()
    }

    private fun onSmilesHistoryReceived(result: Pair<List<SmilesHistoryItem>, List<ProfileSmilesItemResources>>) {
        result.let { (smilesHistoryItems, profilesSmilesItems) ->
            updateViewState {
                copy(
                    itemsResources = smilesHistoryItems.map(SmilesItemResourcesMapper::map),
                    profileSmilesItemsAvailableForTransfer = profilesSmilesItems
                )
            }
        }
    }

    class Factory @Inject constructor(
        private val smilesHistoryUseCase: SmilesHistoryUseCase
    ) : BaseViewModel.Factory<SmilesHistoryViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SmilesHistoryViewModel(
                smilesHistoryUseCase,
                viewState ?: SmilesHistoryViewState()
            ) as T
    }
}
