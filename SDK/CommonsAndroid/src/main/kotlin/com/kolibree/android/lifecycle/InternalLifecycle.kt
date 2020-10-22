/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.lifecycle

import android.os.Bundle
import androidx.annotation.Keep

/**
 *
 * @author lookashc
 */
@Keep
interface InternalLifecycle {

    /**
     * Delegate of the [onCreate] method
     * @param savedInstanceState previous state
     */
    fun onCreateInternal(savedInstanceState: Bundle?)

    /**
     * Delegate of the [onStart] method
     */
    fun onStartInternal()

    /**
     * Delegate of the [onResume] method
     */
    fun onResumeInternal()

    /**
     * Delegate of the [onPause] method
     */

    fun onPauseInternal()

    /**
     * Delegate of the [onStop] method
     */
    fun onStopInternal()

    /**
     * Delegate of the [onDestroy] method
     */
    fun onDestroyInternal()
}
