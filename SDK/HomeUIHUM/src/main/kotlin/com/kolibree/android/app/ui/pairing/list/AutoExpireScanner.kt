/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime

/**
 * Bluetooth Scanner that will automatically clear [ToothbrushScanResult] that are older than
 * [CLEANUP_INTERVAL]
 *
 * It handles Bluetooth Off/On recovery automatically
 */
internal class AutoExpireScanner @Inject constructor(
    private val pairingAssistant: PairingAssistant,
    private val sharedFacade: PairingFlowSharedFacade,
    private val bluetoothUtils: IBluetoothUtils,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) {
    /**
     * Observable that emits Batches of [ToothbrushScanResult] as long as Bluetooth is enabled
     *
     * If Bluetooth is switched off, it will emit [AutoExpireScanResult.BluetoothOff]
     *
     * As soon as Bluetooth is available again, it will resume scanning
     *
     * @return [Observable]<[AutoExpireScanResult]> that will emit a new [AutoExpireScanResult.Batch]
     * right after a scan result arrives; or [AutoExpireScanResult.BluetoothOff]
     */
    fun scan(): Observable<AutoExpireScanResult> {
        return bluetoothUtils
            .bluetoothStateObservable()
            .startWith(bluetoothUtils.isBluetoothEnabled)
            .distinctUntilChanged()
            .switchMap { isBluetoothEnabled ->
                sharedFacade.unpairBlinkingConnectionCompletable()
                    .andThen(maybeStartScanObservable(isBluetoothEnabled))
            }
            .distinctUntilChanged()
    }

    private fun maybeStartScanObservable(isBluetoothEnabled: Boolean): Observable<AutoExpireScanResult> {
        return Observable.defer {
            if (isBluetoothEnabled) {
                startAutoExpireScan()
                    .map { AutoExpireScanResult.Batch(it) }
            } else {
                Observable.just(AutoExpireScanResult.BluetoothOff)
            }
        }
    }

    private fun startAutoExpireScan(): Observable<List<ToothbrushScanResult>> {
        return Observable.merge(cleanupObservable(), scanResultsObservable())
            .scan(
                mutableListOf<AutoExpireScanEvent.Result>(),
                { accumulator, event ->
                    when (event) {
                        is AutoExpireScanEvent.Cleanup -> accumulator.filterExpired()
                        is AutoExpireScanEvent.Result -> accumulator.apply { addOrReplace(event) }
                        else -> accumulator
                    }
                })
            .map { resultsWithTimestamp -> resultsWithTimestamp.map { it.toothbrushScanResult } }
            .distinctUntilChanged()
    }

    private fun cleanupObservable(): Observable<AutoExpireScanEvent.Cleanup> =
        Observable.interval(CLEANUP_INTERVAL.toMillis(), TimeUnit.MILLISECONDS, timeoutScheduler)
            .map { AutoExpireScanEvent.Cleanup }

    private fun scanResultsObservable(): Observable<AutoExpireScanEvent.Result> =
        pairingAssistant.scannerObservable()
            .map { scanResult -> AutoExpireScanEvent.Result(scanResult) }
}

private fun MutableList<AutoExpireScanEvent.Result>.addOrReplace(
    result: AutoExpireScanEvent.Result
): MutableList<AutoExpireScanEvent.Result> {
    return apply {
        removeAll { it.mac == result.mac }

        add(result)
    }
}

private fun MutableList<AutoExpireScanEvent.Result>.filterExpired(): MutableList<AutoExpireScanEvent.Result> {
    val timeThreshold = TrustedClock.getNowLocalDateTime().minusSeconds(CLEANUP_INTERVAL.seconds)
    return filter { result -> result.timestamp.isAfter(timeThreshold) }
        .toMutableList()
}

@VisibleForTesting
@Suppress("MagicNumber")
val CLEANUP_INTERVAL: Duration = Duration.ofSeconds(16)

private sealed class AutoExpireScanEvent {
    internal data class Result(
        val toothbrushScanResult: ToothbrushScanResult,
        val timestamp: LocalDateTime = TrustedClock.getNowLocalDateTime()
    ) : AutoExpireScanEvent() {
        val mac = toothbrushScanResult.mac
    }

    /**
     * Event to signal that expired scan results should be purged
     */
    internal object Cleanup
}

internal sealed class AutoExpireScanResult {
    data class Batch(val results: List<ToothbrushScanResult>) : AutoExpireScanResult()

    object BluetoothOff : AutoExpireScanResult()
}
