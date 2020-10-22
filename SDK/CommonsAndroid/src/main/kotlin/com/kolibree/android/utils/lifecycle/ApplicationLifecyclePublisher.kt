/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils.lifecycle

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import com.kolibree.android.annotation.VisibleForApp
import timber.log.Timber

@VisibleForApp
interface ApplicationLifecyclePublisher {

    /**
     * Starts [ApplicationLifecyclePublisher].
     *
     * After this method publisher will start notifying all observers
     * about application lifecycle changes.
     */
    fun initialize()
}

/**
 * This implementation will receive process lifecycle changes
 * and dispatch them to the [ApplicationLifecyclePublisher]s.
 *
 * It is important that [ON_DESTROY] is not by [processLifecycle].
 */
internal class ApplicationLifecyclePublisherImpl constructor(
    private val processLifecycle: Lifecycle,
    private val mainHandler: Handler,
    private val observers: Set<ApplicationLifecycleObserver>
) : ApplicationLifecyclePublisher {

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            ON_CREATE -> publish(event) { onApplicationCreated() }
            ON_START -> publish(event) { onApplicationStarted() }
            ON_RESUME -> publish(event) { onApplicationResumed() }
            ON_PAUSE -> publish(event) { onApplicationPaused() }
            ON_STOP -> publish(event) { onApplicationStopped() }
            ON_DESTROY,
            ON_ANY -> Unit
        }
    }

    override fun initialize() {
        processLifecycle.addObserver(lifecycleEventObserver)
    }

    private fun publish(
        event: Lifecycle.Event,
        action: ApplicationLifecycleObserver.() -> Unit
    ) {
        observers.forEach { observer ->
            mainHandler.post {
                Timber.d("Publish $event to ${observer::class.simpleName}")
                observer.action()
            }
        }
    }
}
