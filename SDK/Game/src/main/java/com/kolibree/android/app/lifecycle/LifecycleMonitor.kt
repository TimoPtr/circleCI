/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.lifecycle

import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import com.kolibree.android.failearly.FailEarly
import com.kolibree.game.BuildConfig
import timber.log.Timber

/* A piece of debug code to validate internal lifecycle */
@Keep
class LifecycleMonitor {

    internal var lastLifecycleState: Lifecycle.State = Lifecycle.State.INITIALIZED
        private set

    fun markTransitionToState(state: Lifecycle.State) {
        validateTransitionToState(state)
        lastLifecycleState = state
    }

    @SuppressWarnings("all")
    private fun validateTransitionToState(state: Lifecycle.State) {
        if (BuildConfig.DEBUG && VALIDATE_LIFECYCLE_TRANSITIONS) {
            Timber.d("Lifecycle transition from $lastLifecycleState to $state")
            if (!VALID_LIFECYCLE_TRANSITIONS.contains(lastLifecycleState to state)) {
                FailEarly.fail("Lifecycle transition from $lastLifecycleState to $state is not valid!")
            }
        }
    }

    companion object {

        const val TAG_LIFECYCLE = "LifecycleMonitoring"

        internal const val VALIDATE_LIFECYCLE_TRANSITIONS = true

        internal val VALID_LIFECYCLE_TRANSITIONS = setOf(
            Lifecycle.State.INITIALIZED to Lifecycle.State.CREATED,
            Lifecycle.State.CREATED to Lifecycle.State.STARTED,
            Lifecycle.State.STARTED to Lifecycle.State.RESUMED,
            Lifecycle.State.RESUMED to Lifecycle.State.STARTED,
            Lifecycle.State.RESUMED to Lifecycle.State.DESTROYED,
            Lifecycle.State.STARTED to Lifecycle.State.CREATED,
            Lifecycle.State.CREATED to Lifecycle.State.DESTROYED,
            Lifecycle.State.DESTROYED to Lifecycle.State.CREATED
        )
    }
}
