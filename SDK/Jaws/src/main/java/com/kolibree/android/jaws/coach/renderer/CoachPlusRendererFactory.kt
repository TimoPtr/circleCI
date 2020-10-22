/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.renderer

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel

/** Factory to be used to create [CoachPlusRenderer] instances */
@Keep
interface CoachPlusRendererFactory {

    /**
     * Create a Coach+ OpenGL renderer
     *
     * @param toothbrushModel [ToothbrushModel]
     */
    fun createCoachPlusRenderer(toothbrushModel: ToothbrushModel?): CoachPlusRenderer
}
