/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.renderer

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.coach.animation.BrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.animation.PlaqlessBrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.animation.RegularBrushHeadAnimationMapper
import com.kolibree.android.jaws.coach.brushhead.BrushHeadPositionMapper
import com.kolibree.android.jaws.coach.brushhead.PlaqlessBrushHeadPositionMapper
import com.kolibree.android.jaws.coach.brushhead.RegularBrushHeadPositionMapper
import javax.inject.Inject

/** [CoachPlusRendererFactory] implementation */
internal class CoachPlusRendererFactoryImpl @Inject constructor(
    private val memoryManager: MemoryManagerInternal
) : CoachPlusRendererFactory {

    override fun createCoachPlusRenderer(toothbrushModel: ToothbrushModel?): CoachPlusRenderer =
        when (toothbrushModel) {
            ToothbrushModel.PLAQLESS ->
                createCoachPlusRenderer(
                    Kolibree3DModel.PLAQLESS,
                    PlaqlessBrushHeadPositionMapper(),
                    PlaqlessBrushHeadAnimationMapper()
                )
            else ->
                createCoachPlusRenderer(
                    Kolibree3DModel.TOOTHBRUSH,
                    RegularBrushHeadPositionMapper(),
                    RegularBrushHeadAnimationMapper()
                )
        }

    @VisibleForTesting
    internal fun createCoachPlusRenderer(
        brushHeadModel: Kolibree3DModel,
        brushHeadPositionMapper: BrushHeadPositionMapper,
        brushHeadAnimationMapper: BrushHeadAnimationMapper
    ): CoachPlusRenderer =
        CoachPlusRendererImpl(
            memoryManager,
            brushHeadModel,
            brushHeadPositionMapper,
            brushHeadAnimationMapper
        )
}
