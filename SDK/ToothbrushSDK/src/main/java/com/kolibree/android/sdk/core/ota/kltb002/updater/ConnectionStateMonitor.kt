/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater

import androidx.annotation.VisibleForTesting
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import timber.log.Timber

private val TAG = otaTagFor(ConnectionStateMonitor::class)

internal class ConnectionStateMonitor(private val connection: KLTBConnection) {

    @VisibleForTesting
    internal val timeControlScheduler: Scheduler
        get() = Schedulers.from(Executors.newSingleThreadExecutor())

    fun waitForActiveConnection(): Completable {
        return Completable.defer {
            val currentState = connection.state().current
            Timber.tag(TAG).d("Checking our OTA recovery options for state %s", currentState)
            return@defer when (currentState) {
                KLTBConnectionState.ACTIVE -> {
                    Timber.tag(TAG).d("Connection is up, we can proceed immediately")
                    Completable.complete()
                }
                KLTBConnectionState.TERMINATING, KLTBConnectionState.TERMINATED -> {
                    val message = "OTA cannot be recovered, connection is terminating"
                    Timber.tag(TAG).d(message)
                    Completable.error(IllegalStateException(message))
                }
                else -> {
                    Timber.tag(TAG).d("Reconnection is almost there, let's wait a bit...")
                    connectionStateObservable()
                        .filter { state -> state === KLTBConnectionState.ACTIVE }
                        .take(1)
                        .ignoreElements()
                        .doOnComplete { Timber.tag(TAG).i("Connection became active!") }
                }
            }
        }
    }

    internal fun connectionStateObservable(): Observable<KLTBConnectionState> {
        return Observable.interval(1, TimeUnit.SECONDS, timeControlScheduler)
            .map { connection.state().current }
            .doOnNext { state -> Timber.tag(TAG).d("Current connection state %s", state) }
    }
}
