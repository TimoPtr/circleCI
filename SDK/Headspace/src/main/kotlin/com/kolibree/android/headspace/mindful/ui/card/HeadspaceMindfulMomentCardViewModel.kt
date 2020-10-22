/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui.card

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.headspace.mindful.HeadspaceMindfulMomentNavigator
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus.Available
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus.NotAvailable
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentUseCase
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentAnalytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class HeadspaceMindfulMomentCardViewModel(
    initialViewState: HeadspaceMindfulMomentCardViewState,
    private val mindfulMomentUseCase: HeadspaceMindfulMomentUseCase,
    private val mindfulMomentNavigator: HeadspaceMindfulMomentNavigator
) : DynamicCardViewModel<
    HeadspaceMindfulMomentCardViewState,
    HeadspaceMindfulMomentCardInteraction,
    HeadspaceMindfulMomentCardBindingModel>(initialViewState),
    HeadspaceMindfulMomentCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is HeadspaceMindfulMomentCardBindingModel

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        fetchMindfulMomentStatus()
    }

    override fun onClick() {
        getViewState()?.mindfulMoment?.also {
            HeadspaceMindfulMomentAnalytics.open()
            mindfulMomentNavigator.showMindfulMomentScreen(it)
        }
    }

    private fun fetchMindfulMomentStatus() {
        disposeOnPause {
            mindfulMomentUseCase.getHeadspaceMindfulMomentStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onNewHeadspaceMindfulMomentStatus, Timber::e)
        }
    }

    private fun onNewHeadspaceMindfulMomentStatus(status: HeadspaceMindfulMomentStatus) {
        updateViewState {
            when (status) {
                is Available -> withAvailableStatus(status)
                is NotAvailable -> withNotAvailableStatus(status)
            }
        }
    }

    internal class Factory @Inject constructor(
        private val dynamicCardListConfiguration: DynamicCardListConfiguration,
        private val mindfulMomentUseCase: HeadspaceMindfulMomentUseCase,
        private val mindfulMomentNavigator: HeadspaceMindfulMomentNavigator
    ) : BaseViewModel.Factory<HeadspaceMindfulMomentCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HeadspaceMindfulMomentCardViewModel(
                initialViewState = viewState ?: HeadspaceMindfulMomentCardViewState.initial(
                    dynamicCardListConfiguration
                        .getInitialCardPosition(HeadspaceMindfulMomentCardViewModel::class.java)
                ),
                mindfulMomentUseCase = mindfulMomentUseCase,
                mindfulMomentNavigator = mindfulMomentNavigator
            ) as T
    }
}
