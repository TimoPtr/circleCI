/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.toolbox

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewState
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.ToolboxItem
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushes
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaAvailable
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Qualifier
import timber.log.Timber

@VisibleForApp
class ToolboxPriorityDisplayViewModel(
    private val pulsingDotUseCase: PulsingDotUseCase,
    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
    private val toolboxViewModel: ToolboxViewModel,
    private val scheduler: Scheduler,
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(EmptyBaseViewState) {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::startToolboxExplanation)
    }

    private fun startToolboxExplanation(): Disposable {
        return pulsingDotUseCase.shouldShowExplanation()
            .filter { shouldShowExplanation -> shouldShowExplanation }
            .firstElement()
            .waitForToothbrushState()
            .delay(DELAY_BEFORE_APPEARING_MILLIS, MILLISECONDS, scheduler)
            .flatMap {
                priorityItemUseCase.submitAndWaitFor(ToolboxItem)
                    .andThen(showToolboxExplanation())
                    .andThen(markAsDisplayedWhenClosed())
            }
            .subscribe({}, Timber::e)
    }

    private fun showToolboxExplanation(): Completable {
        return Completable.fromAction {
            pulsingDotUseCase.onExplanationShown()
            toolboxViewModel.show(toolboxViewModel.factory().toolboxExplanation())
        }
    }

    private fun markAsDisplayedWhenClosed(): Maybe<ToolboxViewState> {
        return toolboxViewModel.viewStateFlowable
            .filter { !it.toolboxVisible }
            .firstElement()
            .doOnSuccess { priorityItemUseCase.markAsDisplayed(ToolboxItem) }
    }

    /**
     * Prevents the race condition which can occurs with TestBrushing and gives the chance
     * to TestBrushing to be submitted before the ToolboxExplanation.
     */
    private fun <T> Maybe<T>.waitForToothbrushState(): Maybe<ToothbrushConnectionStateViewState> {
        return flatMapPublisher { toothbrushConnectionStateViewModel.viewStateFlowable }
            .filter {
                it.state is NoBluetooth || it.state is NoToothbrushes ||
                    it.state is SingleToothbrushConnected || it.state is SingleToothbrushOtaAvailable
            }.firstElement()
    }

    @VisibleForApp
    class Factory @Inject constructor(
        @ToolboxPulsingDot private val pulsingDotUseCase: PulsingDotUseCase,
        private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
        private val toolboxViewModel: ToolboxViewModel,
        @SingleThreadScheduler private val scheduler: Scheduler,
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel
    ) : BaseViewModel.Factory<BaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ToolboxPriorityDisplayViewModel(
                pulsingDotUseCase,
                priorityItemUseCase,
                toolboxViewModel,
                scheduler,
                toothbrushConnectionStateViewModel
            ) as T

        internal companion object {
            @Qualifier
            @Retention(AnnotationRetention.RUNTIME)
            annotation class ToolboxPulsingDot
        }
    }
}

private const val DELAY_BEFORE_APPEARING_MILLIS = 1000L
