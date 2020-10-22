/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

import androidx.annotation.VisibleForTesting

/** [BaseBrushHeadAnimationMapper] implementation for regular brush heads */
internal class RegularBrushHeadAnimationMapper : BaseBrushHeadAnimationMapper() {

    override val upIncIntAnimation = animationLOINCINT

    override val loIncIntAnimation = animationUPINCINT

    companion object {

        @VisibleForTesting
        val animationLOINCINT = IncisorInteriorAnimation(false)

        @VisibleForTesting
        val animationUPINCINT = IncisorInteriorAnimation(true)
    }
}
