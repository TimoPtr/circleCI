package com.kolibree.android.sba.testbrushing.duringsession

internal enum class SessionStep {
    START_STEP,
    ANALYZING_STEP,
    TOOTHBRUSH_STEP,
    FINISH_STEP;

    fun next() = when (this) {
        START_STEP -> ANALYZING_STEP
        ANALYZING_STEP -> TOOTHBRUSH_STEP
        TOOTHBRUSH_STEP -> FINISH_STEP
        FINISH_STEP -> START_STEP
    }
}
