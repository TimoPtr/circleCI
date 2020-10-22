/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_COMPLETED
import com.kolibree.android.sdk.connection.toothbrush.OTA_UPDATE_INSTALLING
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.CommandSet
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PUSH_DSP
import com.kolibree.android.sdk.core.ota.ToothbrushUpdater
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushDfuUpdater
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.NOT_READY_TRY_LATER
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.NO_ERROR
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.NO_VALID_FILE_FOR_UPDATE
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.TRANSMIT_ERROR
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.UNKNOWN_ERROR
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.VALIDATION_ERROR
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Scheduler
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

/**
 * DSP OTA consists of 2 steps
 *
 * 1. Push file to TB flash using ToothbrushDfuUpdater
 * 2. Command the TB to push the file to DSP. DSP needs to be awake for this command to succeed.
 *
 * Once we command the TB to push the file, we listen to DVP notifications until "push completed"
 * is reported. That's encapsulated in dspState.isSuccess
 * @see [WakeUpDspUseCase]
 */
internal class ToothbrushDspUpdater @Inject constructor(
    private val connection: InternalKLTBConnection,
    private val bleDriver: BleDriver,
    private val dfuUpdater: ToothbrushDfuUpdater,
    private val wakeUpDspUseCase: WakeUpDspUseCase,
    @SingleThreadScheduler private val delayScheduler: Scheduler,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) : ToothbrushUpdater {

    override fun update(availableUpdate: AvailableUpdate): Observable<OtaUpdateEvent> {
        return dfuUpdater.update(availableUpdate)
            .doOnComplete { Timber.tag(TAG).d("DSP OTA File successfully pushed") }
            .map { event -> event.adjustDfuProgress() }
            .concatWith(
                pushDspStateObservable().map { pushDspState -> pushDspState.toOtaUpdateEvent() }
            )
            .doOnComplete { Timber.tag(TAG).d("PushDSP completed") }
    }

    override fun isUpdateInProgress(): Boolean {
        return dfuUpdater.isUpdateInProgress()
    }

    private fun pushDspStateObservable(): Observable<PushDspState> {
        return wakeUpDspUseCase.wakeUpCompletable()
            .andThen(pushDspObservable())
            .retryWhen(RetryOnRecoverableErrorFunction(delayScheduler = delayScheduler))
    }

    private fun pushDspObservable(): Observable<PushDspState> {
        return bleDriver.setAndGetDeviceParameterOnce(CommandSet.pushDspFirmware())
            .flatMapObservable { dspUpdateProgressObservable() }
            .map { pushDspState ->
                when {
                    pushDspState.hasRecoverableError -> throw RecoverablePushDspException
                    pushDspState.hasUnrecoverableError -> throw UnrecoverableDspUpdateException
                    else -> pushDspState
                }
            }
    }

    private fun dspUpdateProgressObservable(): Observable<PushDspState> {
        return bleDriver.deviceParametersCharacteristicChangedStream()
            .filter { payload -> payload[0] == DEVICE_PARAMETERS_PUSH_DSP }
            .map(PushDspState::fromPayload)
            .doOnNext { Timber.tag(TAG).d("DSPState: $it; ${connection.state().current}") }
            .takeUntil { dspState -> dspState.isSuccess }
            .toObservable()
    }
}

private object RecoverablePushDspException : Exception()

private class RetryOnRecoverableErrorFunction(
    @SingleThreadScheduler private val delayScheduler: Scheduler
) : Function<Observable<Throwable>, ObservableSource<Int>> {
    var retries = 0

    override fun apply(errors: Observable<Throwable>): ObservableSource<Int> {
        return errors.flatMap { error ->
            if (retries++ < MAX_RETRIES && error is RecoverablePushDspException) {
                Observable.just(1)
                    .delay(DELAY_AFTER_DSP_ERROR_SECONDS, TimeUnit.SECONDS, delayScheduler)
            } else {
                val errorToEmit = if (error is RecoverablePushDspException) {
                    UnrecoverableDspUpdateException
                } else {
                    error
                }

                Observable.error(errorToEmit)
            }
        }
    }
}

private fun PushDspState.toOtaUpdateEvent(): OtaUpdateEvent {
    if (isSuccess) return OtaUpdateEvent.fromAction(
        action = OTA_UPDATE_COMPLETED
    )

    return when (lastStatus) {
        NO_ERROR -> OtaUpdateEvent.fromProgressiveAction(
            action = OTA_UPDATE_INSTALLING,
            progress = adjustDspProgress()
        )
        NOT_READY_TRY_LATER,
        NO_VALID_FILE_FOR_UPDATE,
        TRANSMIT_ERROR,
        VALIDATION_ERROR,
        UNKNOWN_ERROR -> OtaUpdateEvent.fromError()
    }
}

private const val DSP_PROGRESS_START = 50
private const val STEPS = 2
private const val DELAY_AFTER_DSP_ERROR_SECONDS = 2L
private const val MAX_RETRIES = 4L

/*
DSP OTA consists of 2 steps

1. Push file to TB flash using ToothbrushDfuUpdater
2. Command the TB to push the file to DSP

At this point, step 1 is completed, thus we consider 50% of the work is done.
PushDspState.progress refers to "push file to dsp" step
 */
private fun PushDspState.adjustDspProgress() = DSP_PROGRESS_START + progress.div(STEPS)

private fun OtaUpdateEvent.adjustDfuProgress() = copy(progress = progress?.div(STEPS) ?: 0)

private val TAG = otaTagFor(ToothbrushDspUpdater::class)
