package com.kolibree.android.sba.testbrushing.tracker

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.Analytics
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class TestBrushingEventTrackerTest : BaseUnitTest() {

    private fun verifyEvent(action: String) {
        verify(eventTracker).sendEvent(TestBrushingEventTracker.FEATURE + action)
    }

    @Test
    fun `introScreen() sends event INTRO_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.introScreen())
        verifyEvent(INTRO_SCREEN)
    }

    @Test
    fun `startScreen() sends event START_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.startScreen())
        verifyEvent(START_SCREEN)
    }

    @Test
    fun `startBrushing() sends event BRUSHING_RUN`() {
        Analytics.send(TestBrushingEventTracker.startBrushing())
        verifyEvent(BRUSHING_RUN)
    }

    @Test
    fun `pauseBrushingDialog() sends event PAUSE_BRUSHING_DIALOG`() {
        Analytics.send(TestBrushingEventTracker.pauseBrushingDialog())
        verifyEvent(PAUSE_BRUSHING_DIALOG)
    }

    @Test
    fun `brushingDone() sends event BRUSHING_DONE`() {
        Analytics.send(TestBrushingEventTracker.brushingDone())
        verifyEvent(BRUSHING_DONE)
    }

    @Test
    fun `brushingResume() sends event BRUSHING_RESUME`() {
        Analytics.send(TestBrushingEventTracker.brushingResume())
        verifyEvent(BRUSHING_RESUME)
    }

    @Test
    fun `optimizeAnalysisScreen() sends event OPTIMIZE_ANALYSIS_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.optimizeAnalysisScreen())
        verifyEvent(OPTIMIZE_ANALYSIS_SCREEN)
    }

    @Test
    fun `analysisInProgressScreen() sends event ANALYSIS_IN_PROGRESS_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.analysisInProgressScreen())
        verifyEvent(ANALYSIS_IN_PROGRESS_SCREEN)
    }

    @Test
    fun `resultsIntroScreen() sends event RESULTS_INTRO_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.resultsIntroScreen())
        verifyEvent(RESULTS_INTRO_SCREEN)
    }

    @Test
    fun `resultsSurfaceScreen() sends event RESULTS_SURFACE_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.resultsSurfaceScreen())
        verifyEvent(RESULTS_SURFACE_SCREEN)
    }

    @Test
    fun `resultsSpeedScreen() sends event RESULTS_SPEED_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.resultsSpeedScreen())
        verifyEvent(RESULTS_SPEED_SCREEN)
    }

    @Test
    fun `resultsAngleScreen() sends event RESULTS_ANGLE_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.resultsAngleScreen())
        verifyEvent(RESULTS_ANGLE_SCREEN)
    }

    @Test
    fun `resultsConclusionScreen() sends event RESULTS_CONCLUSION_SCREEN`() {
        Analytics.send(TestBrushingEventTracker.resultsConclusionScreen())
        verifyEvent(RESULTS_CONCLUSION_SCREEN)
    }
}
