/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.app.ui.settings.SettingsInitialAction
import com.kolibree.android.rewards.morewaystoearnpoints.logic.MoreWaysToGetPointsCardUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.tracker.Analytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class MoreWaysToEarnPointsCardViewModel(
    initialViewState: MoreWaysToEarnPointsCardViewState,
    private val useCase: MoreWaysToGetPointsCardUseCase,
    private val cardItemResourceProvider: MoreWaysToEarnPointsCardItemResourceProvider,
    private val navigator: MoreWaysToEarnPointsCardNavigator,
    private val homeNavigator: HomeNavigator
) : DynamicCardViewModel<
    MoreWaysToEarnPointsCardViewState,
    MoreWaysToEarnPointsCardInteraction,
    MoreWaysToEarnPointsCardBindingModel>(initialViewState),
    MoreWaysToEarnPointsCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is MoreWaysToEarnPointsCardBindingModel

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrieveCardsData)
    }

    override fun onItemClick(challenge: EarnPointsChallenge) {
        Analytics.send(MoreWaysToEarnPointsCardAnalytics.challengeCardClick(challenge.id))
        when (challenge.id) {
            EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE -> navigator.showSettingsScreen()
            EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS,
            EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS,
            EarnPointsChallenge.Id.TURN_ON_BRUSHING_REMINDERS -> navigator.showNotificationScreen()
            EarnPointsChallenge.Id.RATE_THE_APP -> {
                // TODO https://kolibree.atlassian.net/browse/KLTB002-12090
            }
            EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW ->
                navigator.showSettingsScreen(SettingsInitialAction.SCROLL_TO_WEEKLY_REVIEW)
            EarnPointsChallenge.Id.REFER_A_FRIEND -> {
                // TODO https://kolibree.atlassian.net/browse/KLTB002-12184
            }
            EarnPointsChallenge.Id.AMAZON_DASH -> {
                homeNavigator.showAmazonDashConnectScreen()
            }
        }
    }

    private fun retrieveCardsData(): Disposable {
        return useCase.getChallengesToBeDisplayedStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::renderCards, Timber::e)
    }

    private fun renderCards(challenges: List<EarnPointsChallenge>) {
        updateViewState {
            copy(cards = challenges.map {
                MoreWaysToEarnPointsCardItemBindingModel(
                    it,
                    cardItemResourceProvider.getIcon(it.id),
                    cardItemResourceProvider.getHeader(it.id),
                    cardItemResourceProvider.getBody(it.id)
                )
            })
        }
    }

    class Factory @Inject constructor(
        private val dynamicCardListConfiguration: DynamicCardListConfiguration,
        private val useCase: MoreWaysToGetPointsCardUseCase,
        private val cardItemResourceProvider: MoreWaysToEarnPointsCardItemResourceProvider,
        private val navigator: MoreWaysToEarnPointsCardNavigator,
        private val homeNavigator: HomeNavigator
    ) : BaseViewModel.Factory<MoreWaysToEarnPointsCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MoreWaysToEarnPointsCardViewModel(
                viewState ?: MoreWaysToEarnPointsCardViewState.initial(
                    dynamicCardListConfiguration.getInitialCardPosition(modelClass)
                ),
                useCase,
                cardItemResourceProvider,
                navigator,
                homeNavigator
            ) as T
    }
}
