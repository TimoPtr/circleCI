/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.transition.TransitionManager
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.rewards.ProfileTierUseCase
import com.kolibree.android.rewards.persistence.ProfileTierOptional
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class EarningPointsCardViewModel(
    initialViewState: EarningPointsCardViewState,
    private val navigator: HumHomeNavigator,
    private val profileTierUseCase: ProfileTierUseCase
) : DynamicCardViewModel<
    EarningPointsCardViewState,
    EarningPointsCardInteraction,
    EarningPointsCardBindingModel>(initialViewState), EarningPointsCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is EarningPointsCardBindingModel

    override fun toggleExpanded(view: View) {
        val viewState = getViewState()

        if (viewState?.expanded == true) {
            EarningPointsCardAnalytics.close()
        } else {
            EarningPointsCardAnalytics.open()
        }

        if (view is ViewGroup && viewState?.expanded != true) {
            TransitionManager.beginDelayedTransition(view)
        }
        updateViewState {
            copy(expanded = !expanded)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        disposeOnDestroy(::listenForEarningCardPointsChanged)
    }

    private fun listenForEarningCardPointsChanged() =
        profileTierUseCase.currentProfileTier()
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onEarningPointsTierChanged,
                Timber::e
            )

    private fun onEarningPointsTierChanged(profileTier: ProfileTierOptional) {
        updateViewState { copy(pointsPerBrush = profileTier.value?.smilesPerBrushing ?: 1) }
    }

    override fun onTermsAndConditionsClick() {
        navigator.openEarningPointsTermsAndConditions()
    }

    class Factory @Inject constructor(
        private val profileTierUseCase: ProfileTierUseCase,
        private val navigator: HumHomeNavigator,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<EarningPointsCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EarningPointsCardViewModel(
            viewState ?: EarningPointsCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            navigator,
            profileTierUseCase
        ) as T
    }
}
