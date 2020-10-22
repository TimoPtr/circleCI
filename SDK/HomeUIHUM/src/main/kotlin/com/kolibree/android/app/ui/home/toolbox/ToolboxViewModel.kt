/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbox

import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject

@VisibleForApp
class ToolboxViewModel(
    initialViewState: ToolboxViewState?,
    private val navigator: HomeNavigator,
    private val configurationFactory: ToolboxConfiguration.Factory
) : BaseViewModel<ToolboxViewState, BaseAction>(
    initialViewState ?: ToolboxViewState.hidden()
) {

    val toolboxVisible = map(viewStateLiveData) { viewState ->
        viewState?.toolboxVisible ?: false
    }

    val iconVisible = map(viewStateLiveData) { viewState ->
        viewState?.iconRes != null
    }

    val subTitleVisible = map(viewStateLiveData) { viewState ->
        viewState?.subTitle != null
    }

    val titleVisible = map(viewStateLiveData) { viewState ->
        viewState?.title != null
    }

    val titleAppearance = map(viewStateLiveData) { viewState ->
        viewState?.titleTextAppearance ?: R.attr.textAppearanceHeadline3
    }

    val bodyVisible = map(viewStateLiveData) { viewState ->
        viewState?.body != null
    }

    val detailsButtonVisible = map(viewStateLiveData) { viewState ->
        viewState?.detailsButton != null
    }

    val confirmButtonVisible = map(viewStateLiveData) { viewState ->
        viewState?.confirmButton != null
    }

    val iconRes = map(viewStateLiveData) { viewState ->
        viewState?.iconRes
    }

    val subTitle = map(viewStateLiveData) { viewState ->
        viewState?.subTitle
    }

    val title = map(viewStateLiveData) { viewState ->
        viewState?.title
    }

    val body = map(viewStateLiveData) { viewState ->
        viewState?.body
    }

    val detailsButton = map(viewStateLiveData) { viewState ->
        viewState?.detailsButton
    }

    val confirmButton = map(viewStateLiveData) { viewState ->
        viewState?.confirmButton
    }

    val pulsingDotVisible = map(viewStateLiveData) { viewState ->
        viewState?.pulsingDotVisible ?: false
    }

    private var currentConfiguration: ToolboxConfiguration? = null

    fun show(configuration: ToolboxConfiguration) {
        updateViewState {
            currentConfiguration = configuration
            Analytics.send(AnalyticsEvent(name = configuration.analyticsName))
            ToolboxViewState.fromConfiguration(configuration)
        }
    }

    fun onDetailsClick() {
        currentConfiguration?.detailsButton?.onClick()
        hide()
    }

    fun onConfirmClick() {
        currentConfiguration?.confirmButton?.onClick()
        hide()
    }

    fun onBackPressed(): Boolean {
        if (getViewState()?.toolboxVisible == true) {
            hide()
            return true
        }

        return false
    }

    // TODO Improve this
    fun factory() = configurationFactory

    private fun ToolboxConfiguration.Button.onClick() {
        Analytics.send(AnalyticsEvent(analytics))
        if (intent != null) {
            navigator.navigateTo(intent)
        }
    }

    private fun hide() {
        updateViewState {
            currentConfiguration = null
            ToolboxViewState.hidden()
        }
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val navigator: HomeNavigator,
        private val configurationFactory: ToolboxConfiguration.Factory
    ) : BaseViewModel.Factory<ToolboxViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ToolboxViewModel(viewState, navigator, configurationFactory) as T
    }
}
