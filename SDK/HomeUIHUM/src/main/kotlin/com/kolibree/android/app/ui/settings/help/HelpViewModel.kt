/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.help

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.ui.settings.help.HelpAnalytics.contactUs
import com.kolibree.android.app.ui.settings.help.HelpAnalytics.goBack
import com.kolibree.android.app.ui.settings.help.HelpAnalytics.helpCenter
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject

internal class HelpViewModel(
    initialViewState: EmptyBaseViewState,
    private val helpNavigator: HelpNavigator
) : BaseViewModel<EmptyBaseViewState, NoActions>(
    initialViewState
) {

    fun onClickHelpCenter() {
        Analytics.send(helpCenter())
        helpNavigator.showHelpCenter()
    }

    fun onClickContactUs() {
        Analytics.send(contactUs())
        helpNavigator.showContactUs()
    }

    fun onCloseClick() {
        Analytics.send(goBack())
        helpNavigator.closeScreen()
    }

    class Factory @Inject constructor(
        private val helpNavigator: HelpNavigator
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HelpViewModel(
                initialViewState = EmptyBaseViewState,
                helpNavigator = helpNavigator
            ) as T
    }
}
