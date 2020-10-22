/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.hum

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.MemoryManagerInternal
import com.kolibree.android.jaws.base.BaseJawsRenderer
import com.kolibree.android.jaws.base.JawsRenderer
import com.kolibree.android.jaws.models.HumLowerJawVbo
import com.kolibree.android.jaws.models.HumUpperJawVbo
import javax.inject.Inject

/** [BaseJawsRenderer] implementation for [HumJawsView] */
@Keep
interface HumJawsViewRenderer : JawsRenderer

internal class HumJawsViewRendererImpl @Inject constructor(
    memoryManager: MemoryManagerInternal
) : BaseJawsRenderer(
    memoryManager = memoryManager
), HumJawsViewRenderer {

    override val cameraZ = CAMERA_Z

    override fun prepareHumUpperJawVbo(upperJawVbo: HumUpperJawVbo) {
        super.prepareHumUpperJawVbo(upperJawVbo)

        upperJawVbo.rotationVector.y += JAWS_ROTATION_Y
        upperJawVbo.selfRotationVector.x = UPPER_JAW_ROTATION
        upperJawVbo.selfRotationVector.y = SELF_ROTATION_VECTOR_Y
        upperJawVbo.positionVector.y = UPPER_JAW_TRANSLATION_Y
        upperJawVbo.positionVector.x += UPPER_JAW_POSITION_VECTOR_X
    }

    override fun prepareHumLowerJawVbo(lowerJawVbo: HumLowerJawVbo) {
        super.prepareHumLowerJawVbo(lowerJawVbo)

        lowerJawVbo.rotationVector.y += JAWS_ROTATION_Y
        lowerJawVbo.selfRotationVector.x = LOWER_JAW_ROTATION
        lowerJawVbo.selfRotationVector.y = SELF_ROTATION_VECTOR_Y
        lowerJawVbo.positionVector.y = LOWER_JAW_TRANSLATION_Y
        lowerJawVbo.positionVector.x += LOWER_JAW_POSITION_VECTOR_X
        lowerJawVbo.positionVector.z = LOWER_JAW_POSITION_VECTOR_Z
    }

    companion object {

        @VisibleForTesting
        const val CAMERA_Z = 0f

        @VisibleForTesting
        const val JAWS_ROTATION_Y = 30f

        @VisibleForTesting
        const val UPPER_JAW_ROTATION = 370f

        @VisibleForTesting
        const val UPPER_JAW_TRANSLATION_Y = 0.33f

        @VisibleForTesting
        const val UPPER_JAW_POSITION_VECTOR_X = 0.095f

        @VisibleForTesting
        const val LOWER_JAW_ROTATION = 355f

        @VisibleForTesting
        const val LOWER_JAW_TRANSLATION_Y = -0.35f

        @VisibleForTesting
        const val LOWER_JAW_POSITION_VECTOR_Z = 0f

        @VisibleForTesting
        const val LOWER_JAW_POSITION_VECTOR_X = 0.092f

        @VisibleForTesting
        const val SELF_ROTATION_VECTOR_Y = 180f
    }
}
