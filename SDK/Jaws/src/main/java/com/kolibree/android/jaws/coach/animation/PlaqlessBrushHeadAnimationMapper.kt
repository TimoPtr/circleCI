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

/** [BaseBrushHeadAnimationMapper] implementation for Plaqless brush head */
internal class PlaqlessBrushHeadAnimationMapper : BaseBrushHeadAnimationMapper() {

    override val upIncIntAnimation = animationLOINCINT

    override val loIncIntAnimation = animationUPINCINT

    companion object {

        @VisibleForTesting
        val animationLOINCINT = PlaqlessIncisorInteriorAnimation()

        @VisibleForTesting
        val animationUPINCINT = PlaqlessIncisorInteriorAnimation()
    }
}
