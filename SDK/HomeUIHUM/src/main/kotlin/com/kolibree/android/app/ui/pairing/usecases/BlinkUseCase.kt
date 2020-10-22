/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

internal class BlinkUseCase @Inject constructor(
    private val pairingAssistant: PairingAssistant,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) {
    fun blink(single: Single<KLTBConnection>, mac: String): Observable<BlinkEvent> {
        return single
            .timeout(CONNECTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS, timeoutScheduler)
            .map<BlinkEvent> { connection -> BlinkEvent.Success(connection) }
            .toObservable()
            .onErrorResumeNext { throwable: Throwable ->
                pairingAssistant.unpair(mac)
                    .andThen(Observable.just(throwable.toBlinkEvent()))
            }
            .startWith(BlinkEvent.InProgress)
            .doOnDispose { pairingAssistant.unpair(mac).blockingAwait() }
    }

    private fun Throwable.toBlinkEvent(): BlinkEvent {
        return when (this) {
            is TimeoutException -> BlinkEvent.Timeout
            else -> BlinkEvent.Error(this)
        }
    }
}

internal sealed class BlinkEvent {
    object InProgress : BlinkEvent()
    class Success(val connection: KLTBConnection) : BlinkEvent()
    class Error(val throwable: Throwable) : BlinkEvent()
    object Timeout : BlinkEvent()
}

internal val CONNECTION_TIMEOUT = Duration.of(15, ChronoUnit.SECONDS)
