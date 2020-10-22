/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

/**
 * Regular brush head animation for UpIncInt and LoIncInt zones.
 */
internal class IncisorInteriorAnimation(
    private val inverted: Boolean
) : TranslationAnimation() {

    override fun zOffset(): Float {
        return if (inverted)
            UP_INCISOR_DEFAULT_Z_OFFSET
        else
            LOW_INCISOR_DEFAULT_Z_OFFSET
    }

    companion object {
        private const val UP_INCISOR_DEFAULT_Z_OFFSET = -1f

        private const val LOW_INCISOR_DEFAULT_Z_OFFSET = 1.2f
    }
}
