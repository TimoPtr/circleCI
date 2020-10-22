package com.kolibree.android.sba.testbrushing.duringsession.timer

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.mouthmap.widget.timer.RealtimeProvider
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.concurrent.TimeUnit
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock

class CarouselCheckupTimerImplTest : BaseUnitTest() {

    @Mock
    lateinit var handler: Handler

    @Mock
    internal lateinit var realtimeProvider: RealtimeProvider

    internal lateinit var timer: CarouselTimerImpl

    override fun setup() {
        super.setup()

        timer = spy(CarouselTimerImpl(handler, realtimeProvider))
    }

    @Test
    fun resume_invokesStartTimer() {
        doNothing().whenever(timer).startTimer()

        timer.resume()

        verify(timer).startTimer()
    }

    @Test
    fun resume_updateLastTimeUpdate() {
        val elapsedTime = 1234L
        whenever(realtimeProvider.elapsedRealtime()).thenReturn(elapsedTime)
        timer.lastTimeUpdate = 0L

        timer.resume()

        Assert.assertEquals(elapsedTime, timer.lastTimeUpdate)
    }

    @Test
    fun pause_invokesRemoveCallbacksAndMessages() {
        doNothing().whenever(handler).removeCallbacksAndMessages(null)

        timer.pause()

        verify(handler).removeCallbacksAndMessages(null)
    }

    @Test
    fun pause_updateElapsedTime() {
        doNothing().whenever(handler).removeCallbacksAndMessages(null)
        whenever(realtimeProvider.elapsedRealtime()).thenReturn(2000L)
        timer.elapsedTime = 0L
        timer.lastTimeUpdate = 1500L

        timer.pause()

        Assert.assertEquals(500, timer.elapsedTime)
    }

    @Test
    fun calcTimeLeft_returnsAppropriateValue() {
        timer.elapsedTime = TimeUnit.SECONDS.toMillis(3)

        Assert.assertEquals(TimeUnit.SECONDS.toMillis(7), timer.calcTimeLeft())
    }
}
