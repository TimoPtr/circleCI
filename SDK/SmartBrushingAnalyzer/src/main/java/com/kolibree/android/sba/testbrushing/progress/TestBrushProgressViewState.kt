package com.kolibree.android.sba.testbrushing.progress

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.ViewAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewState

@SuppressLint("DeobfuscatedPublicSdkClass")
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
data class TestBrushProgressViewState(
    val isProgressStep1Completed: Boolean = false,
    val isProgressStep2Completed: Boolean = false,
    val isProgressStep3Completed: Boolean = false,
    val isProgressStep4Completed: Boolean = false,
    val hasBuildUpStep: Boolean = false,
    override val action: ViewAction = NoneAction
) : LegacyBaseTestBrushingViewState(NoneAction)
