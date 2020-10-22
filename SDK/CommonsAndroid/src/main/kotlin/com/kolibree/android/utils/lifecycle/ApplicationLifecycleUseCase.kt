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
import androidx.lifecycle.LifecycleEventObserver
import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe

@VisibleForApp
interface ApplicationLifecycleUseCase {

    /**
     * Emits the current [Lifecycle.State] of the application
     * and refreshes it each time [Lifecycle.Event] occurs.
     *
     * It may emit the same [Lifecycle.State] twice because
     * [Lifecycle.getCurrentState] is updated before new [Lifecycle.Event] comes.
     *
     * All events are emitted on the main thread. It is also required by
     * [Lifecycle.addObserver] and [Lifecycle.removeObserver].
     */
    fun observeApplicationState(): Flowable<Lifecycle.State>
}

internal class ApplicationLifecycleUseCaseImpl constructor(
    private val processLifecycle: Lifecycle,
    private val mainHandler: Handler
) : ApplicationLifecycleUseCase {

    override fun observeApplicationState(): Flowable<Lifecycle.State> {
        return Flowable
            .create(createFlowable(), BackpressureStrategy.LATEST)
            .startWith(processLifecycle.currentState)
    }

    private fun createFlowable() = FlowableOnSubscribe<Lifecycle.State> { emitter ->
        val observer = LifecycleEventObserver { _, _ ->
            // Lifecycle events are always emitted on the main thread
            emitter.onNext(processLifecycle.currentState)
        }

        emitter.setCancellable {
            mainHandler.postAtFrontOfQueue {
                // Observer can be only removed on the main thread
                processLifecycle.removeObserver(observer)
            }
        }

        mainHandler.postAtFrontOfQueue {
            // Observer can be only added on the main thread
            processLifecycle.addObserver(observer)
        }
    }
}
