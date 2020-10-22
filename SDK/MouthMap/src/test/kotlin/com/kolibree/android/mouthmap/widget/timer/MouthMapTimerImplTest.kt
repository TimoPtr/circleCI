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
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MouthMapTimerImplTest : BaseUnitTest() {

    lateinit var timer: MouthMapTimerImpl
    private val realtimeProvider = mock<RealtimeProvider>()
    private val handler = mock<Handler>()

    override fun setup() {
        super.setup()

        timer = spy(
            MouthMapTimerImpl(
                handler,
                realtimeProvider
            )
        )
    }

    @Test
    fun `updateElapsedTime sends new value`() {
        val currentTime = 123450L
        whenever(realtimeProvider.elapsedRealtime()).thenReturn(currentTime)

        val test = timer.observable().test()

        timer.updateElapsedTime()

        test.assertValue(currentTime / 1000)
    }

    @Test
    fun `updateElapsedTime sends new value including startTime`() {
        val currentTime = 123450L
        val startTime = 12050L
        whenever(realtimeProvider.elapsedRealtime()).thenReturn(currentTime)
        timer.startTime = startTime

        val test = timer.observable().test()

        timer.updateElapsedTime()

        val expectedValue = (currentTime - startTime) / 1000
        test.assertValue(expectedValue)
    }

    @Test
    fun `updateElapsedTime sends new value including startTime and totalTimeElapsed`() {
        val currentTime = 123450L
        val startTime = 12050L
        val totalTimeElapsed = 45050L
        whenever(realtimeProvider.elapsedRealtime()).thenReturn(currentTime)
        timer.startTime = startTime
        timer.totalTimeElapsed = totalTimeElapsed

        val test = timer.observable().test()

        timer.updateElapsedTime()

        val expectedValue = ((currentTime - startTime) + totalTimeElapsed) / 1000
        test.assertValue(expectedValue)
    }

    @Test
    fun `UPDATE_INTERVAL is 250`() {
        assertEquals(250, UPDATE_INTERVAL)
    }
}
