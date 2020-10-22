/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.container

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.presentation.list.ShopListScrollUseCase
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

private typealias State = ShopContainerViewState

@VisibleForApp
class ShopContainerViewModel(
    initialViewState: State,
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val smilesUseCase: SmilesUseCase,
    private val shopListScrollUseCase: ShopListScrollUseCase,
    featureToggles: FeatureToggleSet
) : BaseViewModel<ShopContainerViewState, ShopContainerActions>(initialViewState) {

    val tabsEnabled: Boolean = featureToggles.toggleIsOn(ShowShopTabsFeature)

    val discountBannerVisible = mapNonNull<State, Boolean>(
        viewStateLiveData,
        defaultValue = false
    ) { viewState ->
        viewState.discountBannerVisible &&
            viewState.storeDetails != null &&
            viewState.currentSmilesCount > 0
    }

    val discountAmount = map<State, String>(viewStateLiveData) { viewState ->
        viewState?.storeDetails?.let { storeDetails ->
            Price.createFromSmiles(viewState.currentSmilesCount, storeDetails)
                .formattedPrice()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        disposeOnDestroy { fetchStoreDetails() }
        disposeOnDestroy { watchCurrentAmountOfSmiles() }
        disposeOnDestroy { watchScrollToItem() }
    }

    fun onCloseDiscountBannerClick() {
        updateViewState { copy(discountBannerVisible = false) }
    }

    private fun fetchStoreDetails() = shopifyClientWrapper.getStoreDetails().subscribe(
        { storeDetails -> updateViewState { copy(storeDetails = storeDetails) } },
        Timber::e
    )

    private fun watchCurrentAmountOfSmiles(): Disposable =
        smilesUseCase
            .smilesAmountStream()
            .subscribe(
                { smiles -> updateViewState { copy(currentSmilesCount = smiles) } },
                Timber::e
            )

    private fun watchScrollToItem(): Disposable {
        return shopListScrollUseCase.getItemIdToScroll()
            .doOnNext { switchToProductsTab() }
            .ignoreElements()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, Timber::e)
    }

    private fun switchToProductsTab() {
        pushActionWhenResumed(ShopContainerActions.SwitchToProductTab)
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val shopifyClientWrapper: ShopifyClientWrapper,
        private val smilesUseCase: SmilesUseCase,
        private val shopListScrollUseCase: ShopListScrollUseCase,
        private val featureToggles: FeatureToggleSet
    ) : BaseViewModel.Factory<ShopContainerViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShopContainerViewModel(
            viewState ?: State.initial(),
            shopifyClientWrapper,
            smilesUseCase,
            shopListScrollUseCase,
            featureToggles
        ) as T
    }
}
