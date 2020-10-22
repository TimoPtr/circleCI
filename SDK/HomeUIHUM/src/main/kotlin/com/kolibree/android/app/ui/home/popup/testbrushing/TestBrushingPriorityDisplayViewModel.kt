/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.testbrushing

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.HomeSessionFlag
import com.kolibree.android.app.ui.brushing.BrushingsForCurrentProfileUseCase
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmileCounterChangedUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.NoInternet
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.TestBrushing
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushOtaAvailable
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.synchronization.SynchronizationState.Failure
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import timber.log.Timber

internal interface TestBrushingCallback {
    fun onTestBrushingFinished()
}

@VisibleForApp
class TestBrushingPriorityDisplayViewModel(
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
    private val brushingsForCurrentProfileUseCase: BrushingsForCurrentProfileUseCase,
    private val startNonUnityGameUseCase: StartNonUnityGameUseCase,
    private val sessionFlags: SessionFlags,
    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
    private val smileCounterChangedUseCase: SmileCounterChangedUseCase,
    private val synchronizationStateUseCase: SynchronizationStateUseCase,
    private val scheduler: Scheduler
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(EmptyBaseViewState), TestBrushingCallback {

    private var disposable: Disposable? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (sessionFlags.readSessionFlag(HomeSessionFlag.SUPPRESS_TEST_BRUSHING_REMINDER) != true) {
            disposeOnDestroy {
                submitTestBrushingForNewUser().also { disposable = it }
            }
        }
    }

    private fun submitTestBrushingForNewUser(): Disposable {
        return maybeShowTestBrushing()
            .flatMapCompletable {
                priorityItemUseCase.submitAndWaitFor(TestBrushing)
                    .andThen(startTestBrushing())
                    .andThen(consumeTestBrushingWhenProcessEnd())
                    .flatMapCompletable { Completable.fromCallable { onTestBrushingFinished() } }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    private fun maybeShowTestBrushing(): Maybe<Long> {
        return Maybe.defer {
            toothbrushConnectionStateViewModel.viewStateFlowable
                .filter { it.state is SingleToothbrushConnected || it.state is SingleToothbrushOtaAvailable }
                .firstElement()
                .flatMapPublisher {
                    brushingsForCurrentProfileUseCase.getBrushingCount(ActivityGame.TestBrushing)
                }
                .filter { brushingNumber -> brushingNumber == 0L }
                .firstElement()
        }
    }

    private fun startTestBrushing(): Completable {
        return Completable.defer {
            sessionFlags.setSessionFlag(
                HomeSessionFlag.SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN,
                true
            )
            startNonUnityGameUseCase.start(ActivityGame.TestBrushing)
        }
    }

    /**
     * TestBrushing is considered finished when when the animation counter is played
     * or there is a failure in the request
     */
    private fun consumeTestBrushingWhenProcessEnd(): Maybe<Boolean> {
        return Observable.merge(
            smileCounterChangedUseCase.counterStateObservable.map {
                it is PlayIncrease || it is Error || it is NoInternet
            },
            synchronizationStateUseCase.onceAndStream.map {
                it is Failure
            })
            .filter { shouldEndTestBrushing -> shouldEndTestBrushing }
            .firstElement()
            .delay(DELAY_PLAY_MILLIS, MILLISECONDS, scheduler)
    }

    override fun onTestBrushingFinished() {
        priorityItemUseCase.markAsDisplayed(TestBrushing)
        disposable.forceDispose()
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
        private val brushingsForCurrentProfileUseCase: BrushingsForCurrentProfileUseCase,
        private val startNonUnityGameUseCase: StartNonUnityGameUseCase,
        private val sessionFlags: SessionFlags,
        private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
        private val smileCounterChangedUseCase: SmileCounterChangedUseCase,
        private val synchronizationStateUseCase: SynchronizationStateUseCase,
        @SingleThreadScheduler private val scheduler: Scheduler
    ) : BaseViewModel.Factory<BaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TestBrushingPriorityDisplayViewModel(
                toothbrushConnectionStateViewModel,
                brushingsForCurrentProfileUseCase,
                startNonUnityGameUseCase,
                sessionFlags,
                priorityItemUseCase,
                smileCounterChangedUseCase,
                synchronizationStateUseCase,
                scheduler
            ) as T
    }
}

private const val DELAY_PLAY_MILLIS = 1000L
