/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.widget.progressbar

import android.widget.ProgressBar
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.threeten.bp.Duration

class ProgressAnimatorTest : BaseUnitTest() {

    private lateinit var progressAnimator: ProgressAnimator
    private val progressBar: ProgressBar = mock()

    override fun setup() {
        super.setup()
        progressAnimator = spy(ProgressAnimator(progressBar, Duration.ofSeconds(4)))
        progressAnimator.progressAnimation = mock()
    }

    @Test
    fun `progressAnimator start the animation`() {
        whenever(progressAnimator.progressAnimation.isStarted).thenReturn(false)
        progressAnimator.start()
        verify(progressAnimator.progressAnimation).start()
    }

    @Test
    fun `progressAnimator resume the animation`() {
        whenever(progressAnimator.progressAnimation.isStarted).thenReturn(true)
        progressAnimator.start()
        verify(progressAnimator.progressAnimation).resume()
    }

    @Test
    fun `progressAnimator pause the animation`() {
        progressAnimator.pause()
        verify(progressAnimator.progressAnimation).pause()
    }

    @Test
    fun `progressAnimator reset the animation`() {
        progressAnimator.reset()
        verify(progressAnimator.progressAnimation).cancel()
        assert(progressAnimator.progressBarReference?.progress == 0)
    }

    @Test
    fun `progressAnimator destroy the animation`() {
        progressAnimator.destroy()
        verify(progressAnimator.progressAnimation).cancel()
        assert(progressAnimator.progressBarReference == null)
    }

    @Test
    fun `changeAnimatorState invokes start when state is START`() {
        progressAnimator.changeAnimatorState(ProgressState.START)
        verify(progressAnimator).start()
    }

    @Test
    fun `changeAnimatorState invokes pause when state is PAUSE`() {
        progressAnimator.changeAnimatorState(ProgressState.PAUSE)
        verify(progressAnimator).pause()
    }

    @Test
    fun `changeAnimatorState invokes reset when state is RESET`() {
        progressAnimator.changeAnimatorState(ProgressState.RESET)
        verify(progressAnimator).reset()
    }

    @Test
    fun `changeAnimatorState invokes destroy when state is DESTROY`() {
        progressAnimator.changeAnimatorState(ProgressState.DESTROY)
        verify(progressAnimator).destroy()
    }
}
