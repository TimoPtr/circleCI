/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

internal class OfflineBrushingsRetrieverTrigger @Inject
constructor(
    private val activeConnectionUseCase: ActiveConnectionUseCase
) {

    /**
     * The goal of the trigger is to emits each time, we should check for offlineBrushing.
     *
     * There is two possible trigger :
     * - a connection became active
     * - a session just finished
     *
     * When a connection is not active anymore we dispose the stream until the connection becomes active again
     */
    val trigger: Flowable<Unit>
        get() {
            return activeConnectionUseCase.onConnectionsUpdatedStream()
                .observeSessionFinishedWhenConnectionIsActive()
                .debounce(DEBOUNCE_TRIGGER_MS, TimeUnit.MILLISECONDS, Schedulers.computation())
                .doOnNext {
                    Timber.d("Trigger offline brushing retriever")
                }
        }

    private fun Flowable<KLTBConnection>.observeSessionFinishedWhenConnectionIsActive(): Flowable<Unit> {
        return flatMap { connection ->
            connection.state().stateStream
                .takeWhile {
                    it == KLTBConnectionState.ACTIVE
                }
                .flatMap {
                    connection.observeSessionFinished()
                }
        }
    }

    private fun KLTBConnection.observeSessionFinished(): Flowable<Unit> {
        return brushingSessionMonitor().sessionMonitorStream
            .filter { sessionStarted -> !sessionStarted }
            .map { Unit }
            .startWith(Unit)
    }
}

private const val DEBOUNCE_TRIGGER_MS = 500L
