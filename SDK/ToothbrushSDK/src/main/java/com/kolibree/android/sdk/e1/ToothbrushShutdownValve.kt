/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.e1

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import dagger.Module
import dagger.Provides
import io.reactivex.Completable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Keep
interface ToothbrushShutdownValve {
    /**
     * Prevent toothbrush associated to the scope from automatically shutting down
     *
     * @return Completable that will prevent a toothbrush from auto shutting down as long as it's not
     * disposed
     *
     * It will emit an error if we can't find a paired toothbrush associated to the MAC address
     *
     * It'll complete immediately if the specified toothbrush can't be kept alive
     */
    fun preventShutdownValve(): Completable
}

/**
 * Valve that controls whether we allow the toothbrush associated to [mac] to auto shutdown
 *
 * An E1/E2 will be kept awake by the SDK as long as we are subscribed as Vibration listener, but there
 * are cases on which a client might not be interested in vibration events and still wants to keep
 * the toothbrush awake
 */
internal class ToothbrushShutdownValveImpl constructor(
    private val connectionProvider: KLTBConnectionProvider,
    private val mac: String,
    private val timeoutScheduler: Scheduler
) : ToothbrushShutdownValve {

    /**
     * Waits for the [KLTBConnection] associated to [mac] to be active and returns an [ToothbrushAwaker]
     *
     * @return Single that will emit [ToothbrushAwaker] if [mac] is [ACTIVE] and
     * [ToothbrushModel.canBeKeptAwake] returns true
     *
     * It will emit a [TimeoutException] if the connection is not [ACTIVE]
     * in [ACTIVE_CONNECTION_TIMEOUT_SECONDS] seconds
     *
     * It will emit [IllegalArgumentException] if [ToothbrushModel.canBeKeptAwake] returns false
     */
    private val toothbrushAwaker
        get() = connectionProvider.existingActiveConnection(mac)
            .timeout(ACTIVE_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS, timeoutScheduler)
            .map { createToothbrushAwaker(it) }

    @VisibleForTesting
    internal fun createToothbrushAwaker(connection: KLTBConnection): ToothbrushAwaker = ToothbrushAwakerImpl(connection)

    /**
     * Prevent toothbrush with MAC address specified in constructor from automatically shutting down
     *
     * @return Completable that will prevent an toothbrush from auto shutting down as long as it's not
     * disposed
     *
     * It will emit an error if we can't find a paired toothbrush associated to the MAC address
     *
     * It'll complete immediately if the specified mac address is not an E1/E2
     */
    override fun preventShutdownValve(): Completable {
        return toothbrushAwaker
            .flatMapCompletable { e1Awaker ->
                Completable.never()
                    .doOnSubscribe { e1Awaker.keepAlive() }
                    .doOnDispose { e1Awaker.allowShutdown() }
            }
            .onErrorResumeNext { throwable ->
                if (throwable is IllegalArgumentException)
                    Completable.complete()
                else
                    Completable.error(throwable)
            }
    }
}

@VisibleForTesting
const val ACTIVE_CONNECTION_TIMEOUT_SECONDS = 10L

internal object NoOpShutdownValue : ToothbrushShutdownValve {
    override fun preventShutdownValve(): Completable = Completable.complete()
}

@Module
object ToothbrushShutdownValveModule {
    @Provides
    internal fun providesToothbrushShutdownValve(
        mac: String?,
        connectionProvider: KLTBConnectionProvider,
        @SingleThreadScheduler timeoutScheduler: Scheduler
    ): ToothbrushShutdownValve {
        return mac?.let {
            ToothbrushShutdownValveImpl(connectionProvider, it, timeoutScheduler)
        } ?: NoOpShutdownValue
    }
}
