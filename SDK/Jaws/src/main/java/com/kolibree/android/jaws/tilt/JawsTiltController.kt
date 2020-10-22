/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt

import com.kolibree.android.annotation.VisibleForApp

/** Jaws pitch and roll controller */
@VisibleForApp
interface JawsTiltController {

    /**
     * Get the rotation vector X axis to apply to the jaws models
     *
     * @return rotation X axis [Float]
     */
    fun getJawsRotationX(): Float

    /**
     * Get the rotation vector Y axis to apply to the jaws models
     *
     * @return rotation Y axis [Float]
     */
    fun getJawsRotationY(): Float

    /**
     * Get the translation vector X axis to apply to the jaws models
     *
     * @return translation X axis [Float]
     */
    fun getTranslationX(): Float

    /**
     * To be called when the EGL context gets paused
     */
    fun onPause()

    /**
     * To be called when the EGL context gets resumed
     */
    fun onResume()
}
