/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import android.view.ViewPropertyAnimator
import android.widget.ImageView
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class SpeedMeterAnimatorTest : BaseUnitTest() {
    private lateinit var testAnimator: SpeedMeterAnimator
    private var pointer: ImageView = mock()
    private val animation: ViewPropertyAnimator = mock()

    override fun setup() {
        super.setup()
        testAnimator = mock()
        testAnimator = spy(SpeedMeterAnimator(pointer))
        testAnimator.pointerAnimation = animation
    }

    @Test
    fun `SpeedMeterAnimator cancel the animation of view`() {
        testAnimator.cancel()
        verify(testAnimator.pointerAnimation).cancel()
    }

    @Test
    fun `SpeedMeterAnimator indicate correct state`() {
        testAnimator.showNormalSpeed()
        verify(testAnimator).rotate(0F)
    }

    @Test
    fun `SpeedMeterAnimator indicate overspeed state`() {
        testAnimator.showHighSpeed()
        verify(testAnimator).rotate(88F)
    }

    @Test
    fun `SpeedMeterAnimator indicate underspeed state`() {
        testAnimator.showLowSpeed()
        verify(testAnimator).rotate(-88F)
    }
}
