package com.kolibree.android.sba.testbrushing

import android.annotation.SuppressLint

@SuppressLint("DeobfuscatedPublicSdkClass")
interface TestBrushingNavigator {

    fun navigateToDuringSessionScreen()

    fun navigateToSessionScreen()

    fun navigateToOptimizeAnalysisScreen()

    fun navigateToResultsScreen()

    fun navigateToProgressScreen()

    fun finishScreen()
}
