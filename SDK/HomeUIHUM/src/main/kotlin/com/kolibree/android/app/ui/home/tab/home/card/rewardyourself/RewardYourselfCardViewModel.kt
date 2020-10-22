/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.RewardYourselfItemsUseCase
import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain.UserCreditsUseCase
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.presentation.list.ShopListScrollUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class RewardYourselfCardViewModel(
    initialViewState: RewardYourselfCardViewState,
    private val rewardYourselfItemsUseCase: RewardYourselfItemsUseCase,
    private val userCreditsUseCase: UserCreditsUseCase,
    private val humHomeNavigator: HumHomeNavigator,
    private val shopListScrollUseCase: ShopListScrollUseCase,
    private val currentProfileProvider: CurrentProfileProvider
) : DynamicCardViewModel<
    RewardYourselfCardViewState,
    RewardYourselfCardInteraction,
    RewardYourselfCardBindingModel>(initialViewState), RewardYourselfCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is RewardYourselfCardBindingModel

    override fun onItemClick(item: RewardYourselfItem) {
        humHomeNavigator.navigateToShopTab()
        shopListScrollUseCase.scrollToItem(item.id)
        RewardYourselfAnalytics.click(item.id)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrieveData)
    }

    private fun retrieveData(): Disposable {
        return Flowables
            .combineLatest(
                rewardYourselfItemsUseCase.getRewardItems(),
                currentProfileProvider.currentProfileFlowable(),
                userCreditsUseCase.getUserCredits()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::setData, Timber::e)
    }

    private fun setData(data: Triple<List<RewardYourselfItem>, Profile, Price>) {
        val (items, profile, userCredits) = data
        updateViewState {
            copy(
                visible = items.isNotEmpty(),
                items = items.map { RewardYourselfItemBinding.from(it) },
                userName = profile.firstName,
                userCredits = userCredits
            )
        }
    }

    class Factory @Inject constructor(
        private val rewardYourselfItemsUseCase: RewardYourselfItemsUseCase,
        private val userCreditsUseCase: UserCreditsUseCase,
        private val humHomeNavigator: HumHomeNavigator,
        private val shopListScrollUseCase: ShopListScrollUseCase,
        private val currentProfileProvider: CurrentProfileProvider,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<RewardYourselfCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RewardYourselfCardViewModel(
            viewState ?: RewardYourselfCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            rewardYourselfItemsUseCase,
            userCreditsUseCase,
            humHomeNavigator,
            shopListScrollUseCase,
            currentProfileProvider
        ) as T
    }
}
