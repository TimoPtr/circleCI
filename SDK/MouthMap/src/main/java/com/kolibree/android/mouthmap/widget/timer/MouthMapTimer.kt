/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.widget.timer

import android.os.Handler
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.mouthmap.di.MouthMapTimerBindsModule.Companion.MOUTH_MAP_TIMER_HANDLER
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@Keep
interface MouthMapTimer {
    fun resume()
    fun pause()
    fun observable(): Observable<Long>
}

internal class MouthMapTimerImpl @Inject constructor(
    @Named(MOUTH_MAP_TIMER_HANDLER) private var handler: Handler,
    private var realtimeProvider: RealtimeProvider
) : MouthMapTimer {

    @VisibleForTesting
    var startTime = 0L

    @VisibleForTesting
    var totalTimeElapsed = 0L

    private val publisher: PublishRelay<Long> = PublishRelay.create()
    val timerObservable: Observable<Long> by lazy {
        publisher.hide()
    }

    override fun resume() {
        startTime = realtimeProvider.elapsedRealtime()

        postUpdate()
    }

    override fun pause() {
        totalTimeElapsed += calcElapsedTime()
        handler.removeCallbacksAndMessages(null)
    }

    private fun calcElapsedTime() = realtimeProvider.elapsedRealtime() - startTime

    override fun observable() = timerObservable

    private fun postUpdate() {
        handler.postDelayed(timerAction,
            UPDATE_INTERVAL
        )
    }

    val timerAction = Runnable {
        handler.removeCallbacksAndMessages(null)

        updateElapsedTime()

        postUpdate()
    }

    @VisibleForTesting
    fun updateElapsedTime() {
        val elapsedTimeMillis = realtimeProvider.elapsedRealtime() - startTime + totalTimeElapsed
        val elapsedTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis)

        publisher.accept(elapsedTimeSeconds)
    }
}

const val UPDATE_INTERVAL = 250L
