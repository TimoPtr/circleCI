/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.startscreen.ActivityStartPreconditions
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsViewModel
import com.kolibree.android.app.ui.HomeSessionFlag.SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN
import com.kolibree.android.app.ui.HomeSessionFlag.SUPPRESS_TEST_BRUSHING_REMINDER
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import javax.inject.Inject

internal class TestBrushingStartScreenViewModel(
    initialViewState: TestBrushingStartScreenViewState,
    private val navigator: TestBrushingStartScreenNavigator,
    private val sessionFlags: SessionFlags,
    private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel
) : BaseViewModel<TestBrushingStartScreenViewState, NoActions>(
    initialViewState,
    children = setOf(activityStartPreconditionsViewModel)
), ActivityStartPreconditions by activityStartPreconditionsViewModel {

    val showCloseToolbarButton = mapNonNull(viewStateLiveData, initialViewState.showCloseButtonInToolbar) { state ->
        state.showCloseButtonInToolbar
    }

    val showRemindMeLater = mapNonNull(viewStateLiveData, initialViewState.showRemindMeLaterButton) { state ->
        state.showRemindMeLaterButton
    }

    init {
        setupViewState()
    }

    @VisibleForTesting
    fun setupViewState() {
        val showRemindMeLater =
            sessionFlags.readSessionFlag(SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN) == true
        val reminderSuppressed =
            sessionFlags.readSessionFlag(SUPPRESS_TEST_BRUSHING_REMINDER) == true
        updateViewState {
            copy(
                showRemindMeLaterButton = showRemindMeLater,
                showCloseButtonInToolbar = showRemindMeLater && !reminderSuppressed
            )
        }
        sessionFlags.setSessionFlag(SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN, false)
    }

    fun startClicked() {
        Analytics.send(TestBrushingStartScreenAnalytics.start())
        navigator.startTestBrushing()
    }

    fun remindMeLaterClicked() {
        Analytics.send(TestBrushingStartScreenAnalytics.later())
        navigator.finish()
    }

    fun closeClicked() {
        Analytics.send(TestBrushingStartScreenAnalytics.close())
        sessionFlags.setSessionFlag(SUPPRESS_TEST_BRUSHING_REMINDER, true)
        navigator.finish()
    }

    class Factory @Inject constructor(
        private val navigator: TestBrushingStartScreenNavigator,
        private val sessionFlags: SessionFlags,
        private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel
    ) : BaseViewModel.Factory<TestBrushingStartScreenViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TestBrushingStartScreenViewModel(
                viewState ?: TestBrushingStartScreenViewState.initial(),
                navigator,
                sessionFlags,
                activityStartPreconditionsViewModel
            ) as T
    }
}
