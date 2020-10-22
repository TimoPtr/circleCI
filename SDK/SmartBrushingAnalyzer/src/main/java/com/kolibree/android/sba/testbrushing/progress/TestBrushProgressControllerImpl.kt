package com.kolibree.android.sba.testbrushing.progress

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import io.reactivex.Observable
import io.reactivex.internal.schedulers.ComputationScheduler
import java.util.concurrent.TimeUnit

/**
 * Interval used in the progres screen in SBA
 */

@SuppressLint("VisibleForTests")
internal class TestBrushProgressControllerImpl : TestBrushProgressController {

    override fun controllerObservable(): Observable<Long> {
        return Observable.interval(
            TICK_PERIOD,
            TimeUnit.MILLISECONDS,
            ComputationScheduler()
        ).takeWhile { tick -> tick <= MAX_TICKS }
    }

    companion object {
        private const val MAX_TICKS = 5L
        @VisibleForTesting
        const val TICK_PERIOD = 1000L
    }
}
