/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.timer

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.Clock

class GuidedBrushingTimerViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: GuidedBrushingTimerViewModel

    private val testScheduler = TestScheduler()
    private val systemClock: Clock = mock()

    override fun setup() {
        super.setup()
        viewModel = GuidedBrushingTimerViewModel(
            GuidedBrushingTimerViewState.initial(),
            testScheduler,
            systemClock
        )
        whenever(systemClock.millis()).thenReturn(0)
    }

    @Test
    fun `bindStream with isPlaying to true launch the timer and have the correct value after 10sec`() {
        assertEquals(0, viewModel.getViewState()!!.secondsElapsed())

        viewModel.bindStreams(Flowable.just(true), Observable.empty())
        systemClock.advanceTimeBy(10)

        assertEquals(10, viewModel.getViewState()!!.secondsElapsed())
    }

    @Test
    fun `bindStream with isPlaying to false should not launch the timer`() {
        assertEquals(0, viewModel.getViewState()!!.secondsElapsed())

        viewModel.bindStreams(Flowable.just(false), Observable.empty())
        systemClock.advanceTimeBy(10)

        assertEquals(0, viewModel.getViewState()!!.secondsElapsed())
    }

    @Test
    fun `bindStream with isPlayingStream should have correct values over time`() {
        val isPlayingStream = PublishProcessor.create<Boolean>()

        viewModel.bindStreams(isPlayingStream, Observable.empty())

        isPlayingStream.offer(true)
        systemClock.advanceTimeBy(10)
        assertEquals(10, viewModel.getViewState()!!.secondsElapsed())

        isPlayingStream.offer(false)
        systemClock.advanceTimeBy(5)
        assertEquals(10, viewModel.getViewState()!!.secondsElapsed())

        isPlayingStream.offer(true)
        systemClock.advanceTimeBy(2)
        assertEquals(12, viewModel.getViewState()!!.secondsElapsed())
    }

    @Test
    fun `bindStream with isRestartingProcessor should reset timer`() {
        val isRestartingProcessor = BehaviorRelay.create<Unit>()

        viewModel.bindStreams(Flowable.just(true), isRestartingProcessor)

        systemClock.advanceTimeBy(10)
        assertEquals(10, viewModel.getViewState()!!.secondsElapsed())

        isRestartingProcessor.accept(Unit)
        assertEquals(0, viewModel.getViewState()!!.secondsElapsed())
    }

    @Test
    fun `the ViewState should be updated after a tick`() {
        val millis: Long = 5000

        viewModel.bindStreams(Flowable.just(true), Observable.empty())

        whenever(systemClock.millis()).thenReturn(millis)
        testScheduler.advanceTimeBy(TIME_SCHEDULER_TICK, TimeUnit.MILLISECONDS)

        assertEquals(millis, viewModel.getViewState()!!.elapsedMillis)
    }

    @Test
    fun `the ViewState should not be updated if the tick has not occurred`() {
        val millis: Long = 5000

        viewModel.bindStreams(Flowable.just(true), Observable.empty())

        whenever(systemClock.millis()).thenReturn(millis)
        testScheduler.advanceTimeBy(TIME_SCHEDULER_TICK - 1, TimeUnit.MILLISECONDS)

        assertEquals(0, viewModel.getViewState()!!.elapsedMillis)
    }

    @Test
    fun `time information should not be lost between paused process`() {
        val advanceTime1: Long = 587
        val advanceTime2: Long = 613
        val totalTime = advanceTime1 + advanceTime2

        val isPlayingStream = PublishProcessor.create<Boolean>()
        viewModel.bindStreams(isPlayingStream, Observable.empty())

        isPlayingStream.offer(true)
        systemClock.advanceTimeBy(advanceTime1, TimeUnit.MILLISECONDS)
        isPlayingStream.offer(false)

        assertEquals(0, viewModel.getViewState()!!.secondsElapsed())
        assertEquals(advanceTime1, viewModel.getViewState()!!.elapsedMillis)

        isPlayingStream.offer(true)
        systemClock.advanceTimeBy(advanceTime2, TimeUnit.MILLISECONDS)
        isPlayingStream.offer(false)

        assertEquals(1, viewModel.getViewState()!!.secondsElapsed())
        assertEquals(totalTime, viewModel.getViewState()!!.elapsedMillis)
    }

    private fun Clock.advanceTimeBy(delayTime: Long, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        val currentTime = this.millis()
        whenever(this.millis()).thenReturn(currentTime + timeUnit.toMillis(delayTime))
        testScheduler.advanceTimeBy(delayTime, timeUnit)
    }
}

private const val TIME_SCHEDULER_TICK = 100L
