/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.ongoing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.testbrushing.R
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class OngoingBrushingViewStateTest : BaseUnitTest() {

    @Test
    fun `initial sets up initial state of animation`() {
        val state = OngoingBrushingViewState.initial()
        assertEquals(
            LottieDelayedLoop(
                rawRes = R.raw.animation_brushing,
                isPlaying = true,
                loopStartFrameRes = R.integer.test_brushing_animation_start_frame,
                loopEndFrameRes = R.integer.test_brushing_animation_end_frame
            ), state.brushingAnimation
        )
    }

    @Test
    fun `withPausedAnimations sets isPlaying on both animations to false`() {
        val state = OngoingBrushingViewState.initial().copy(
            brushingAnimation = LottieDelayedLoop(
                rawRes = R.raw.animation_brushing,
                isPlaying = true
            )
        )

        val pausedState = state.withPausedAnimations()
        assertFalse(pausedState.brushingAnimation.isPlaying)
    }

    @Test
    fun `withResumedAnimations sets isPlaying based on visibility`() {
        val state = OngoingBrushingViewState.initial().copy(
            brushingAnimation = LottieDelayedLoop(
                rawRes = R.raw.animation_brushing,
                isPlaying = false
            )
        )

        val resumedState = state.withResumedAnimations()
        assertTrue(resumedState.brushingAnimation.isPlaying)
    }
}
