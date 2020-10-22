package com.kolibree.android.sba.testbrushing.duringsession

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.ANALYZING_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.FINISH_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.START_STEP
import com.kolibree.android.sba.testbrushing.duringsession.SessionStep.TOOTHBRUSH_STEP
import org.junit.Assert
import org.junit.Test

class SessionStepTest : BaseUnitTest() {

    @Test
    fun next_START_STEP_returns_ANALYZING_STEP() {
        Assert.assertEquals(ANALYZING_STEP, START_STEP.next())
    }

    @Test
    fun next_ANALYZING_STEP_returns_TOOTHBRUSH_STEP() {
        Assert.assertEquals(TOOTHBRUSH_STEP, ANALYZING_STEP.next())
    }

    @Test
    fun next_TOOTHBRUSH_STEP_returns_FINISH_STEP() {
        Assert.assertEquals(FINISH_STEP, TOOTHBRUSH_STEP.next())
    }

    @Test
    fun next_FINISH_STEP_returns_START_STEP() {
        Assert.assertEquals(START_STEP, FINISH_STEP.next())
    }
}
