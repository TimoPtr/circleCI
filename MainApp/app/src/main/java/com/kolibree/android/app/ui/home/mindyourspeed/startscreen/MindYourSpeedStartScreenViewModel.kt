/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen

import android.content.Context
import android.text.Spannable
import androidx.lifecycle.ViewModel
import com.kolibree.R
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.startscreen.ActivityStartPreconditions
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsViewModel
import com.kolibree.android.app.ui.text.highlightString
import javax.inject.Inject

internal class MindYourSpeedStartScreenViewModel(
    private val navigator: MindYourSpeedStartScreenNavigator,
    private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel
) : BaseViewModel<EmptyBaseViewState, NoActions>(
    EmptyBaseViewState,
    children = setOf(activityStartPreconditionsViewModel)
), ActivityStartPreconditions by activityStartPreconditionsViewModel {

    fun startClicked() {
        MindYourSpeedStartScreenAnalytics.start()
        navigator.startMindYourSpeedScreen()
    }

    fun cancelClicked() {
        MindYourSpeedStartScreenAnalytics.cancel()
        navigator.closeScreen()
    }

    fun bodyDescription(context: Context): Spannable = highlightString(
        fullText = context.getString(R.string.mind_your_speed_start_screen_description),
        highlight = context.getString(R.string.mind_your_speed_start_screen_description_highlight)
    )

    class Factory @Inject constructor(
        private val navigator: MindYourSpeedStartScreenNavigator,
        private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MindYourSpeedStartScreenViewModel(navigator, activityStartPreconditionsViewModel) as T
    }
}
