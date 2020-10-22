/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbartoothbrush

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel
import com.kolibree.android.clock.TrustedClock
import io.reactivex.Flowable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class ToothbrushConnectionStateViewModel(
    initialState: ToothbrushConnectionStateViewState,
    private val toolbarToothbrushViewModel: ToolbarToothbrushViewModel,
    private val scheduler: Scheduler
) : BaseViewModel<ToothbrushConnectionStateViewState, HomeScreenAction>(initialState) {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        updateViewState {
            copy(
                state = Unknown,
                connectingTime = currentTime()
            )
        }

        disposeOnStop {
            Flowable.interval(0, 1, TimeUnit.SECONDS, scheduler)
                .subscribe({ toolbarToothbrushViewModel.refresh() }, Timber::e)
        }

        disposeOnStop {
            toolbarToothbrushViewModel
                .viewStateObservable()
                .map { it.toothbrushState }
                .subscribe(this::onUpdateState, Timber::e)
        }
    }

    private fun onUpdateState(newState: ToothbrushConnectionState) {
        val state = nextState(newState)
        if (state != getViewState()?.state) {
            updateViewState { copy(state = state) }
        }
    }

    @VisibleForTesting
    fun nextState(newState: ToothbrushConnectionState): ToothbrushConnectionState {
        return when {
            isStillDisconnected(newState) -> toDisconnectedState(newState)
            isConnectingFirstTime(newState) -> {
                updateViewState {
                    copy(connectingTime = currentTime())
                }
                newState
            }
            isConnectingLongTime(newState) -> toDisconnectedState(newState)
            else -> newState
        }
    }

    private fun currentTime(): Long {
        return TrustedClock.utcClock.millis()
    }

    @VisibleForTesting
    fun isStillDisconnected(newState: ToothbrushConnectionState): Boolean {
        return isDisconnected(getViewState()?.state) && isConnecting(newState)
    }

    @VisibleForTesting
    fun isDisconnected(state: ToothbrushConnectionState?) =
        state is SingleToothbrushDisconnected || state is MultiToothbrushDisconnected

    @VisibleForTesting
    fun toDisconnectedState(newState: ToothbrushConnectionState): ToothbrushConnectionState {
        return when (newState) {
            is SingleToothbrushConnecting -> SingleToothbrushDisconnected(newState.mac)
            is MultiToothbrushConnecting -> MultiToothbrushDisconnected(newState.macs)
            else -> throw IllegalStateException()
        }
    }

    @VisibleForTesting
    fun isConnectingLongTime(newState: ToothbrushConnectionState): Boolean {
        val elapsedTime = currentTime() - firstConnectingTime()
        val connectingTimeExceeded = elapsedTime > TimeUnit.SECONDS.toMillis(MAX_CONNECTING_TIME)
        return isConnecting(newState) && connectingTimeExceeded
    }

    private fun firstConnectingTime(): Long {
        return getViewState()?.connectingTime ?: 0L
    }

    @VisibleForTesting
    fun isConnectingFirstTime(newState: ToothbrushConnectionState): Boolean {
        return isConnecting(newState) && !isConnecting(getViewState()?.state)
    }

    @VisibleForTesting
    fun isConnecting(state: ToothbrushConnectionState?) =
        state is SingleToothbrushConnecting || state is MultiToothbrushConnecting

    @VisibleForApp
    class Factory @Inject constructor(
        private val toolbarToothbrushViewModel: ToolbarToothbrushViewModel,
        @SingleThreadScheduler private val scheduler: Scheduler
    ) : BaseViewModel.Factory<ToothbrushConnectionStateViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ToothbrushConnectionStateViewModel(
                viewState
                    ?: ToothbrushConnectionStateViewState.initial(),
                toolbarToothbrushViewModel,
                scheduler
            ) as T
    }
}

private const val MAX_CONNECTING_TIME = 30L
