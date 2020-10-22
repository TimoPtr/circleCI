/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.e1

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.isActive
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import timber.log.Timber

/**
 * Helper to control whether we allow an E1 to shutdown itself or not
 */
internal interface ToothbrushAwaker {
    /**
     * Prevents a toothbrush from auto shutting down
     *
     * Caller is responsible for invoking [allowShutdown] once this is no longer needed. Failing to
     * do so will have a severe impact on toothbrush battery life.
     *
     * If the [ToothbrushModel] doesn't support to be kept alive, this operation won't have any
     * effect
     */
    fun keepAlive()

    /**
     * Allows a toothbrush to auto shutdown
     */
    fun allowShutdown()
}

internal class ToothbrushAwakerImpl @JvmOverloads constructor(
    connection: KLTBConnection,
    private val timeoutScheduler: Scheduler = SingleThreadSchedulerModule.scheduler()
) : ToothbrushAwaker {

    init {
        require(connection.toothbrush().model.canBeKeptAwake) {
            val acceptedModels = ToothbrushModel.values().filter { it.canBeKeptAwake }

            "Expected $acceptedModels, was ${connection.toothbrush().model}"
        }
    }

    @VisibleForTesting
    var keepAliveDisposable: Disposable? = null

    private val weakConnection = WeakReference(connection)

    @Synchronized
    override fun allowShutdown() {
        keepAliveDisposable.forceDispose()
    }

    @Synchronized
    override fun keepAlive() {
        if (keepAliveDisposable?.isDisposed != false) {
            val period = SHUTDOWN_SECONDS - 1

            keepAliveDisposable =
                Observable.interval(0L, period, TimeUnit.SECONDS, timeoutScheduler)
                    .switchMap {
                        sendPing()
                            .andThen(Observable.just(it))
                    }
                    .subscribe(
                        {
                            // no-op
                        },
                        Timber::e
                    )
        }
    }

    @VisibleForTesting
    fun sendPing(): Completable =
        weakConnection.get()?.let {
            if (it.isActive()) {
                it.toothbrush().ping()
            } else {
                null
            }
        } ?: Completable.complete()
}

@VisibleForTesting
internal const val SHUTDOWN_SECONDS = 10L
