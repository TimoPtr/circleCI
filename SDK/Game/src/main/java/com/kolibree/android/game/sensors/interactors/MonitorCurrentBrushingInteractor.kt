/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.toothbrush.GameToothbrushEvent
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.KLTBConnection
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import timber.log.Timber

@GameScope
@VisibleForApp
class MonitorCurrentBrushingInteractor @Inject constructor(
    lifecycle: Lifecycle,
    brushingToothbrushEventProvider: GameToothbrushEventProvider,
    @SingleThreadScheduler private val resetScheduler: Scheduler
) : ToothbrushInteractor, DefaultLifecycleObserver {

    private val disposables = CompositeDisposable()

    /**
     * Thread safety strategy for this field is that it's always invoked on main thread
     */
    private var resetMonitorCurrentSentDisposable: Disposable? = null

    init {
        lifecycle.addObserver(this)
    }

    init {
        disposables.addSafely(
            brushingToothbrushEventProvider.connectionEventStream()
                .subscribe(
                    ::onToothbrushEvent,
                    Timber::e
                )
        )
    }

    @VisibleForTesting
    var monitorCurrentDisposable: Disposable? = null

    @VisibleForTesting
    var onVibratorOffTimestamp: Long = 0

    @VisibleForTesting
    val brushingIsMonitored = AtomicBoolean(false)

    fun onToothbrushEvent(brushingToothbrushEvent: GameToothbrushEvent) {
        when (brushingToothbrushEvent) {
            is GameToothbrushEvent.ConnectionLost -> onConnectionLost()
            is GameToothbrushEvent.VibratorOn -> onVibratorOn(brushingToothbrushEvent.connection)
            is GameToothbrushEvent.VibratorOff -> onVibratorOff()
        }
    }

    @VisibleForTesting
    fun onVibratorOn(connection: KLTBConnection) {
        abortResetMonitorCurrentSendTimer()

        maybeSendMonitorCurrentBrushing(connection)
    }

    @VisibleForTesting
    fun onVibratorOff() {
        onVibratorOffTimestamp = nowMillis()
    }

    @VisibleForTesting
    fun onConnectionLost() {
        if (shouldStartResetTimer()) {
            disposables.addSafely(resetMonitorCurrentSentWithDelay())
        }
    }

    private fun resetMonitorCurrentSentWithDelay(): Disposable {
        return Single.timer(MONITOR_CURRENT_DEBOUNCE_SECONDS, TimeUnit.SECONDS, resetScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    resetMonitorCurrentSentDisposable = null

                    brushingIsMonitored.set(false)
                },
                Timber::e
            ).apply {
                resetMonitorCurrentSentDisposable = this
            }
    }

    private fun shouldStartResetTimer() = resetMonitorCurrentSentDisposable?.isDisposed != false

    private fun abortResetMonitorCurrentSendTimer() {
        resetMonitorCurrentSentDisposable.forceDispose()
    }

    /**
     * To reduce the number of commands to the minimum we only want to send monitor current if
     * MONITOR_CURRENT_DEBOUNCE_SECONDS time has elapsed.
     *
     * This is because current FW waits for 20 seconds before considering that the current brushing has ended, so we
     * only want to send the command if the pause was longer than 20. To play on the safe side, I've set the threshold
     * to 15.
     *
     * If we didn't send the command and over 20 seconds had elapsed, from the FWs point of view, it'd be an offline
     * brushing
     */
    @VisibleForTesting
    @MainThread
    fun maybeSendMonitorCurrentBrushing(connection: KLTBConnection) {
        if (shouldSendMonitorCurrentCommand()) {
            monitorCurrentDisposable.forceDispose()

            Timber.d("Sending monitor current")
            monitorCurrentDisposable =
                connection
                    .brushing()
                    .monitorCurrent()
                    .retry(MONITOR_CURRENT_RETRY_COUNT)
                    .doFinally { monitorCurrentDisposable = null }
                    .subscribe(
                        { brushingIsMonitored.set(true) },
                        {
                            brushingIsMonitored.set(false)
                            Timber.e(it)
                        }
                    )

            disposables.addSafely(monitorCurrentDisposable)
        }
    }

    /*
    If the session is not monitored because of an error and the debounce period is not elapsed,
    we need to monitor the brushing session before the pause.

    If the session before the pause has been monitored without error, but the debounce period is not
    elapsed, no need to monitor (the toothbrush did not close the previous session).

    If the session before the pause has been monitored without error, then a pause occurred and the
    debounce period is elapsed, then we have to monitor this new session as a part of the global
    brushing.
     */
    @VisibleForTesting
    fun shouldSendMonitorCurrentCommand() =
        isMonitorDebounceDelayElapsed() ||
            !brushingIsMonitored.get()

    @VisibleForTesting
    fun isMonitorDebounceDelayElapsed(): Boolean {
        val currentTimestamp = nowMillis()
        val secondsSincePause =
            TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - onVibratorOffTimestamp)
        return secondsSincePause >= MONITOR_CURRENT_DEBOUNCE_SECONDS
    }

    private fun nowMillis() = TrustedClock.getNowInstant().toEpochMilli()

    override fun onDestroy(owner: LifecycleOwner) {
        disposables.dispose()
    }
}

@VisibleForTesting
internal const val MONITOR_CURRENT_DEBOUNCE_SECONDS = 15L

@VisibleForTesting
internal const val MONITOR_CURRENT_RETRY_COUNT = 2L
