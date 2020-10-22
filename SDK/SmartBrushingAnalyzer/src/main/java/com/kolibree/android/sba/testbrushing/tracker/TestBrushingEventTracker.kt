package com.kolibree.android.sba.testbrushing.tracker

import androidx.annotation.VisibleForTesting
import com.kolibree.android.tracker.AnalyticsEvent

internal object TestBrushingEventTracker {

    @VisibleForTesting
    val FEATURE = AnalyticsEvent(FEATURE_NAME)

    fun introScreen() = FEATURE + INTRO_SCREEN

    fun startScreen() = FEATURE + START_SCREEN

    fun startBrushing() = FEATURE + BRUSHING_RUN

    fun pauseBrushingDialog() = FEATURE + PAUSE_BRUSHING_DIALOG

    fun brushingDone() = FEATURE + BRUSHING_DONE

    fun brushingResume() = FEATURE + BRUSHING_RESUME

    fun optimizeAnalysisScreen() = FEATURE + OPTIMIZE_ANALYSIS_SCREEN

    fun analysisInProgressScreen() = FEATURE + ANALYSIS_IN_PROGRESS_SCREEN

    fun resultsIntroScreen() = FEATURE + RESULTS_INTRO_SCREEN

    fun resultsSurfaceScreen() = FEATURE + RESULTS_SURFACE_SCREEN

    fun resultsSpeedScreen() = FEATURE + RESULTS_SPEED_SCREEN

    fun resultsAngleScreen() = FEATURE + RESULTS_ANGLE_SCREEN

    fun resultsConclusionScreen() = FEATURE + RESULTS_CONCLUSION_SCREEN
}

const val FEATURE_NAME = "TestBrushing"
const val INTRO_SCREEN = "Intro"
const val START_SCREEN = "Start"
const val BRUSHING_RUN = "Run"
const val PAUSE_BRUSHING_DIALOG = "PausePopup"
const val BRUSHING_DONE = "DoneButton"
const val BRUSHING_RESUME = "ResumeButton"
const val OPTIMIZE_ANALYSIS_SCREEN = "OptimizeAnalysis"
const val ANALYSIS_IN_PROGRESS_SCREEN = "AnalysisInProgress"
const val RESULTS_INTRO_SCREEN = "ResultsIntro"
const val RESULTS_SURFACE_SCREEN = "ResultsSurface"
const val RESULTS_SPEED_SCREEN = "ResultsSpeed"
const val RESULTS_ANGLE_SCREEN = "ResultsAngle"
const val RESULTS_CONCLUSION_SCREEN = "ResultsConclusion"
