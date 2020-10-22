package com.kolibree.android.sba.testbrushing.results

import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.ViewAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewState

internal data class ResultsViewState(
    val analysisSuccessfulAlpha: Float = 1f,
    val mouthCoverageAlpha: Float = 0f,
    val speedAlpha: Float = 0f,
    val angleAlpha: Float = 0f,
    val currentIndicator: Int = 0,
    val isChangeViewHintVisible: Boolean = false,
    val mouthCoverageCardDescription: String = "",
    val speedCardDescription: String = "",
    val results: BrushingResults = BrushingResults(),
    override val action: ViewAction = NoneAction
) : LegacyBaseTestBrushingViewState(action)
