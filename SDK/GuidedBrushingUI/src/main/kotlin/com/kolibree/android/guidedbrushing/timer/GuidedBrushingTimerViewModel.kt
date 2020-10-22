/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.clock.TrustedClock
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Clock
import timber.log.Timber

internal class GuidedBrushingTimerViewModel(
    initialViewState: GuidedBrushingTimerViewState?,
    private val timeScheduler: Scheduler,
    private val systemClock: Clock
) : BaseViewModel<GuidedBrushingTimerViewState, NoActions>(
    initialViewState ?: GuidedBrushingTimerViewState.initial()
) {
    val secondsElapsed: LiveData<Long> = map(viewStateLiveData) { viewState ->
        viewState?.secondsElapsed()
    }

    fun bindStreams(isPlayingStream: Flowable<Boolean>, isRestartingStream: Observable<Unit>) {
        disposeOnCleared { getIsPlayingStream(isPlayingStream) }
        disposeOnCleared { getRestartStream(isRestartingStream) }
    }

    private fun getIsPlayingStream(isPlayingStream: Flowable<Boolean>): Disposable? {
        return isPlayingStream.distinctUntilChanged()
            .toObservable()
            .switchMap { isPlaying ->
                if (isPlaying) {
                    getTickerObservable()
                } else {
                    Observable.empty()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // no-op
            }, Timber::e)
    }

    private fun getTickerObservable(): Observable<Long> {
        return Observable.interval(TIMER_TICK_RATE, TimeUnit.MILLISECONDS, timeScheduler)
            .doOnSubscribe { onTimerStarted() }
            .doOnNext { updateElapsedMillis() }
            .doFinally { updateElapsedMillis() }
    }

    private fun getRestartStream(isRestartingStream: Observable<Unit>): Disposable? {
        return isRestartingStream
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ restartTimer() }, Timber::e)
    }

    private fun onTimerStarted() {
        updateViewState { withTimerStarted(systemClock.millis()) }
    }

    private fun restartTimer() {
        updateViewState { withRestart() }
    }

    private fun updateElapsedMillis() {
        updateViewState { withTimeUpdate(systemClock.millis()) }
    }

    @VisibleForApp
    class Factory @Inject constructor(
        @SingleThreadScheduler private val timeoutScheduler: Scheduler
    ) : BaseViewModel.Factory<GuidedBrushingTimerViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GuidedBrushingTimerViewModel(
                viewState,
                timeoutScheduler,
                TrustedClock.systemClock()
            ) as T
    }
}

const val TIMER_TICK_RATE = 100L
