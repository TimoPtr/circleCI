/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.ota.OtaSteps.Companion.create
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_COMPLETED
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromAction
import com.kolibree.android.sdk.core.ota.kltb003.RecoverableDfuException
import com.kolibree.sdkws.data.model.GruwareData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class OtaUpdater @Inject constructor() {

    fun updateToothbrushObservable(
        connection: KLTBConnection,
        onOtaStart: Completable = Completable.complete()
    ): Observable<OtaUpdateEvent> {
        return createOtaSteps(connection)
            .flatMapObservable { otaSteps: OtaSteps ->
                if (otaSteps.isEmpty()) {
                    Timber.tag(TAG).w("No OTA steps to apply")
                    Observable.empty<OtaUpdateEvent>()
                } else {
                    Timber.tag(TAG).d("Steps $otaSteps")
                    applyOtaSteps(otaSteps, connection, onOtaStart)
                }
            }
    }

    private fun applyOtaSteps(
        otaSteps: OtaSteps,
        connection: KLTBConnection,
        onOtaStart: Completable = Completable.complete()
    ): Observable<OtaUpdateEvent> = onOtaStart
        .andThen(Observable.fromIterable(otaSteps))
        .concatMap<OtaUpdateEvent> { step: AvailableUpdateStep ->
            applyStep(connection, step)
        }

    private fun applyStep(
        connection: KLTBConnection,
        step: AvailableUpdateStep
    ): Observable<OtaUpdateEvent> = updateObservable(connection, step.availableUpdate)
        .doOnSubscribe {
            Timber.tag(TAG)
                .d("Starting update for step $step. State is ${connection.state().current}")
        }
        .filter { (action) -> action != OTA_UPDATE_COMPLETED }
        .distinctUntilChanged()
        .map { event: OtaUpdateEvent -> adjustProgressFunction(event, step) }
        .doOnComplete {
            Timber.tag(TAG).d("Step completed: ${step.typeName}")
        }

    @VisibleForTesting
    internal fun createOtaSteps(connection: KLTBConnection): Single<OtaSteps> = Single.defer {
        val gruwareData = connection.tag as GruwareData
        Single.just(
            create(gruwareData.availableUpdates)
        )
    }

    /**
     * Adjusts the progress value to emit for OTA_UPDATE_INSTALLING events
     *
     * If the event doesn't have progress, return same OtaUpdateEvent
     */
    @VisibleForTesting
    internal fun adjustProgressFunction(
        event: OtaUpdateEvent,
        step: AvailableUpdateStep
    ): OtaUpdateEvent {
        val newProgress = step.startingProgress + (event.progress ?: 0) / step.progressDividend

        return event.copy(progress = newProgress)
    }

    @VisibleForTesting
    internal fun updateObservable(
        connection: KLTBConnection,
        availableUpdate: AvailableUpdate
    ): Observable<OtaUpdateEvent> = if (availableUpdate.isEmpty()) {
        Observable.just(fromAction(OTA_UPDATE_COMPLETED))
    } else Observable.defer {
        connection.toothbrush().update(availableUpdate)
            .delay(TIME_BEFORE_RETRY_SECONDS, TimeUnit.SECONDS, Schedulers.computation())
            .retry { throwable: Throwable ->
                if (throwable is RecoverableDfuException) {
                    Timber.tag(TAG).w("DFU on recoverable error: %s", throwable.message)
                    true
                } else {
                    Timber.tag(TAG).e(throwable, "Unrecoverable error!")
                    false
                }
            }
    }
}

private const val TIME_BEFORE_RETRY_SECONDS = 2L

private val TAG = otaTagFor(OtaUpdater::class.java)
