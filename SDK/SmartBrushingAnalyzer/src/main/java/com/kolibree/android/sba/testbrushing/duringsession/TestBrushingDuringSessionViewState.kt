package com.kolibree.android.sba.testbrushing.duringsession

import com.kolibree.android.sba.R
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.ViewAction
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewState

internal data class TestBrushingDuringSessionViewState(
    val descriptionId: Int = R.string.empty,
    val highlightedId: Int = R.string.empty,
    val animationId: Int = R.raw.anim_step1,
    val backgroundColorRes: Int = R.color.white,
    val indicatorStep: Int = 0,
    val withIndicatorAnimation: Boolean = false,
    val withTimerVisible: Boolean = false,
    override val action: ViewAction = NoneAction
) : LegacyBaseTestBrushingViewState(action)
