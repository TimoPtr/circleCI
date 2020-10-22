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
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.CommandSet
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

/**
 * DSP is normally asleep to save battery. It wakes up when we send a Ping, but it takes a while.
 *
 * Since we don't know how long this can take, we poll for DspState periodically until we either
 * get a valid DSP Firmware or we timeout
 *
 * This UseCase ensures that we wait long enough for DSP to wake up
 */
internal class WakeUpDspUseCase @Inject constructor(
    private val bleDriver: BleDriver,
    private val dspStateUseCase: DspStateUseCase,
    @SingleThreadScheduler private val delayScheduler: Scheduler,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) {
    fun wakeUpCompletable(): Completable {
        return bleDriver.setAndGetDeviceParameterOnce(CommandSet.ping())
            .delay(DELAY_AFTER_PING_SECONDS, SECONDS, delayScheduler)
            .flatMap { verifyDspAwakeSingle() }
            .ignoreElement()
            .timeout(TIMEOUT, SECONDS, timeoutScheduler)
    }

    private fun verifyDspAwakeSingle(): Single<DspState> {
        return dspStateUseCase.dspStateSingle()
            .map { dspState ->
                if (dspState.hasValidFirmware) {
                    dspState
                } else {
                    throw DspNotAwakeException
                }
            }
            .retryWhen { errors ->
                errors.flatMap { error -> delayedRetryOnDspNotAwakeException(error) }
            }
    }

    private fun delayedRetryOnDspNotAwakeException(error: Throwable): Flowable<Int>? {
        return if (error is DspNotAwakeException) {
            Flowable.just(1)
                .delay(DELAY_AFTER_DSP_NOT_AWAKE, SECONDS, delayScheduler)
        } else {
            Flowable.error(error)
        }
    }
}

private const val DELAY_AFTER_PING_SECONDS = 3L
private const val DELAY_AFTER_DSP_NOT_AWAKE = 1L
private const val TIMEOUT = 10L

internal object DspNotAwakeException : Exception()

private val TAG = otaTagFor(WakeUpDspUseCase::class)
