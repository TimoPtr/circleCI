/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.isActive
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.core.ServiceProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Interface to awake DSP processor
 *
 * DSP processor has a long startup time, so we should awake it as soon as we suspect that the user
 * will need it
 */
@Keep
interface DspAwaker {

    /**
     * Keep DSP alive for [keepAliveTime] units. The first invocation will happen right after
     * subscription.
     *
     * Clients must not invoke this while a brushing session is ongoing.
     *
     * @param keepAliveTime the keepAliveTime between [wakeUp] invocations
     * @param unit the unit of measure of the keepAliveTime amounts
     * @return Completable that will complete after the last ping is sent to keep DSP alive
     */
    fun keepAlive(
        keepAliveTime: Long,
        unit: TimeUnit = TimeUnit.SECONDS
    ): Completable
}

/**
 * Sends a ping to wake up DSP processor to all [KLTBConnection]
 * - with state [ACTIVE]
 * - with a DSP processor
 *
 * It keeps track of recent successful pings to avoid sending ping commands too frequently
 */
internal class DspAwakerImpl
@Inject constructor(
    private val serviceProvider: ServiceProvider,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) : DspAwaker {

    override fun keepAlive(keepAliveTime: Long, unit: TimeUnit): Completable {
        val totalInvocations = keepAliveTime / KEEP_ALIVE_INVOKE_EVERY

        return Observable.interval(0L, KEEP_ALIVE_INVOKE_EVERY, unit, timeoutScheduler)
            .switchMap { wakeUp().andThen(Observable.just(it)) }
            .take(totalInvocations)
            .ignoreElements()
    }

    fun wakeUp(): Completable {
        return serviceProvider.connectOnce()
            .flatMapPublisher {
                Flowable.fromIterable(it.knownConnections.filter { connection -> accept(connection) })
            }
            .flatMapCompletable { sendPing(it) }
    }

    @VisibleForTesting
    fun sendPing(connection: KLTBConnection): Completable = connection.toothbrush().ping()

    fun accept(connection: KLTBConnection) =
        connection.isActive() && connection.toothbrush().model.hasDsp
}

@VisibleForTesting
internal const val DSP_SHUTDOWN_SECONDS = 10L

private const val KEEP_ALIVE_INVOKE_EVERY = DSP_SHUTDOWN_SECONDS / 2

@Keep
const val DSP_DEFAULT_KEEP_ALIVE_SECONDS = 30L
