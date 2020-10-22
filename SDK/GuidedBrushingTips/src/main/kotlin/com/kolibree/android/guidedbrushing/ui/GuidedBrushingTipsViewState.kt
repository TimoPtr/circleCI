package com.kolibree.android.guidedbrushing.ui

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.guidedbrushing.tips.R
import com.kolibree.android.guidedbrushing.ui.adapter.BrushingTipsData
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class GuidedBrushingTipsViewState(
    val tips: List<BrushingTipsData> = listOf(molarsBrushingTipsData, incisorsBrushingTipsData)
) : BaseViewState

private const val FIRST_MOLARS_PERFECT_ANGLE_FRAME = 35

private val molarsBrushingTipsData = BrushingTipsData(
    animationRes = R.drawable.molars_tip,
    titleRes = R.string.guided_brushing_tip1_title,
    descriptionRes = R.string.guided_brushing_tip1_description,
    perfectMessageRes = R.string.guided_brushing_tip_perfect_angle,
    wrongMessageRes = R.string.guided_brushing_tip_wrong_angle,
    perfectMessageAfterFrame = FIRST_MOLARS_PERFECT_ANGLE_FRAME
)

private const val FIRST_INCISORS_PERFECT_ANGLE_FRAME = 35

private val incisorsBrushingTipsData = BrushingTipsData(
    animationRes = R.drawable.incisors_tip,
    titleRes = R.string.guided_brushing_tip2_title,
    descriptionRes = R.string.guided_brushing_tip2_description,
    perfectMessageRes = R.string.guided_brushing_tip_perfect_angle,
    wrongMessageRes = R.string.guided_brushing_tip_wrong_angle,
    perfectMessageAfterFrame = FIRST_INCISORS_PERFECT_ANGLE_FRAME
)
