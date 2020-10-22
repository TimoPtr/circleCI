package com.kolibree.android.sba.testbrushing.duringsession.timer

import android.annotation.SuppressLint
import android.os.Handler
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.mouthmap.widget.timer.RealtimeProvider
import com.kolibree.android.sba.testbrushing.CarouselTimerModule
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@SuppressLint("DeobfuscatedPublicSdkClass")
interface CarouselTimer {
    fun resume()
    fun pause()
    fun observable(): Observable<Int>
}

internal class CarouselTimerImpl @Inject constructor(
    @Named(CarouselTimerModule.CAROUSEL_TIMER) private var handler: Handler,
    private var realtimeProvider: RealtimeProvider
) : CarouselTimer {

    @VisibleForTesting
    var lastTimeUpdate = 0L

    @VisibleForTesting
    var elapsedTime = 0L

    private val publisher: PublishRelay<Int> = PublishRelay.create()
    val timerObservable: Observable<Int> by lazy {
        publisher.hide()
    }

    override fun observable() = timerObservable

    override fun resume() {
        handler.removeCallbacksAndMessages(null)
        lastTimeUpdate = realtimeProvider.elapsedRealtime() - elapsedTime
        startTimer()
    }

    @VisibleForTesting
    fun startTimer() {
        delayed(calcTimeLeft()) {
            handler.removeCallbacksAndMessages(null)

            publisher.accept(0)

            lastTimeUpdate = realtimeProvider.elapsedRealtime()
            elapsedTime = 0

            startTimer()
        }
    }

    override fun pause() {
        elapsedTime = realtimeProvider.elapsedRealtime() - lastTimeUpdate
        handler.removeCallbacksAndMessages(null)
    }

    @VisibleForTesting
    fun calcTimeLeft(): Long {
        return TimeUnit.SECONDS.toMillis(INTERVAL) - elapsedTime
    }

    private fun delayed(time: Long, block: () -> Unit) {
        handler.postDelayed({ block.invoke() }, time)
    }

    companion object {
        const val INTERVAL = 10L
    }
}
