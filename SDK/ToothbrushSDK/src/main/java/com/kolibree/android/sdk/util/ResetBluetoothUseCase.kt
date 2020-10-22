/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util

import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.sdk.core.detectLeaks
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.threeten.bp.Duration
import timber.log.Timber

internal class ResetBluetoothUseCase constructor(
    private val bluetoothUtils: IBluetoothUtils,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) {
    @Volatile
    private var resetCompletable: Completable? = null

    /**
     * Turns bluetooth off and then on
     *
     * Multiple invocations will return the same Completable as long as a previous reset is
     * still ongoing.
     *
     * Precondition: bluetooth is ON
     *
     * @return [Completable] that will complete after a full lifecycle: bluetooth on - off - on
     */
    fun reset(): Completable {
        var localRef: Completable? = resetCompletable
        if (localRef == null) {
            synchronized(this) {
                localRef = resetCompletable
                if (localRef == null) {
                    localRef = internalReset()
                        .doFinally { nullifyCompletable() }
                        .publish()
                        .refCount()
                        .ignoreElements()

                    resetCompletable = localRef
                }
            }
        }

        return localRef!!
    }

    /**
     * Observable that will emit 1 value & complete after the reset lifecycle has completed. To
     * achieve this
     * 1. Listens to bluetooth state
     * 2. Requests Bluetooth to be disabled
     * 3. Requests bluetooth to be enabled after it detects it has been disabled
     * 4. Waits for bluetooth to be enabled
     * 5. Completes
     */
    private fun internalReset(): Observable<Boolean> {
        return Observable.combineLatest(
            bluetoothUtils.bluetoothStateObservable(),
            disableBluetoothObservable(),
            trueAfterResetFunction()
        )
            .filter { isBluetoothEnabled: Boolean -> isBluetoothEnabled }
            .take(1)
            .timeout(RESET_BLUETOOTH_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS, timeoutScheduler)
    }

    private fun disableBluetoothObservable(): Observable<Unit> {
        return Completable.fromAction { bluetoothUtils.enableBluetooth(false) }
            .andThen(Observable.just(Unit))
    }

    private fun trueAfterResetFunction() = object : BiFunction<Boolean, Unit, Boolean> {
        private val enableCommanded = AtomicBoolean(false)

        override fun apply(bluetoothState: Boolean, ignore: Unit): Boolean {
            if (!bluetoothState) {
                if (enableCommanded.compareAndSet(false, true)) {
                    Timber.d("Reset requesting enable")
                    bluetoothUtils.enableBluetooth(true)
                }

                return false
            }

            Timber.d("Reset commanded: %s, state: %s", enableCommanded, bluetoothState)
            return enableCommanded.get() && bluetoothState
        }
    }

    private fun nullifyCompletable() {
        synchronized(this) {
            resetCompletable.detectLeaks("resetBluetoothCompletable")

            resetCompletable = null
        }
    }

    companion object {
        private val TAG = bluetoothTagFor(ResetBluetoothUseCase::class)
    }
}

@SuppressWarnings("MagicNumber")
private val RESET_BLUETOOTH_TIMEOUT = Duration.ofSeconds(20)
