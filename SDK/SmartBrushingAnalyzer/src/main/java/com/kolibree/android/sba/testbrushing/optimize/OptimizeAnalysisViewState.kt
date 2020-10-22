package com.kolibree.android.sba.testbrushing.optimize

import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewState

internal data class OptimizeAnalysisViewState(
    val isLeftHanded: Boolean? = null,
    val amountBrushing: Int = 0
) : LegacyBaseTestBrushingViewState(NoneAction) {
    fun isFormValid() = isLeftHanded != null && amountBrushing > 0
}
