/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentActions.OpenHeadspaceWebsite
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import javax.inject.Inject

internal class HeadspaceMindfulMomentViewModel(
    initialViewState: HeadspaceMindfulMomentViewState,
    private val navigator: HeadspaceMindfulMomentActivityNavigator
) : BaseViewModel<HeadspaceMindfulMomentViewState, HeadspaceMindfulMomentActions>(
    initialViewState
) {
    val mindfulMoment = mapNonNull(viewStateLiveData, initialViewState.mindfulMoment) { viewState ->
        viewState.mindfulMoment
    }

    fun onShareClick() {
        pushAction(OpenHeadspaceWebsite)
        HeadspaceMindfulMomentAnalytics.visitHeadspace()
    }

    fun onCollectSmilesClick() {
        navigator.finishWithSuccess(TrustedClock.getNowOffsetDateTime())
        HeadspaceMindfulMomentAnalytics.collectPoints()
    }

    fun onCloseClick() {
        navigator.finish()
        HeadspaceMindfulMomentAnalytics.close()
    }

    class Factory @Inject constructor(
        private val mindfulMoment: HeadspaceMindfulMoment,
        private val activityNavigator: HeadspaceMindfulMomentActivityNavigator
    ) : BaseViewModel.Factory<HeadspaceMindfulMomentViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HeadspaceMindfulMomentViewModel(
                HeadspaceMindfulMomentViewState(mindfulMoment),
                activityNavigator
            ) as T
    }
}
