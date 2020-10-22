/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.idlingresources

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.kolibree.android.annotation.VisibleForApp

/**
 * IdlingResource that automatically unregisters
 * itself after going to idle.
 */
@VisibleForApp
abstract class OneTimeIdlingResource(name: String) : EagerIdlingResource(name) {

    /**
     * Blocks until resource goes idle.
     *
     * Under the hood it loops UI thread using [Espresso].
     * Thanks to that UI thread is not completely blocked
     * and app can perform UI operations.
     *
     * Example:
     * ```
     * val oneTimeIdlingResource = SomeOneTimeIdlingResource()
     * oneTimeIdlingResource.waitForIdle()
     * doSomething() // this line will be executed when resource is idle
     * ```
     *
     * When using standard [IdlingResource] you need to perform [Espresso]
     * check to synchronize idling states.
     * It is NOT required here.
     */
    fun waitForIdle() {
        try {
            register()
            Espresso.onIdle()
        } finally {
            unregister()
        }
    }

    private fun register() {
        IdlingRegistry
            .getInstance()
            .register(this.asIdlingResource())
    }

    private fun unregister() {
        IdlingRegistry
            .getInstance()
            .unregister(this.asIdlingResource())
    }
}
