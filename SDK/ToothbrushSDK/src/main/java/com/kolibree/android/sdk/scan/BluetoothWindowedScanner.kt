/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.scan

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.VisibleForTesting
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.sdk.util.IBluetoothUtils
import java.util.concurrent.atomic.AtomicInteger
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import timber.log.Timber

/**
 * Bluetooth Scanner that avoids "Too many scans" issues by delaying stopScan invocations by a given time.
 *
 * If it's asked to perform a startScan with the same parameters during that time window
 * 1. It cancels the stopScan invocation
 * 2. It doesn't need to start scanning because we never stop it
 *
 * See
 * - https://jira.kolibree.com/browse/KLTB002-5979
 * - https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/18
 */
internal class BluetoothWindowedScanner @JvmOverloads constructor(
    private val scanner: BluetoothLeScannerCompat,
    private val bluetoothUtils: IBluetoothUtils,
    private val handler: Handler = Handler(Looper.getMainLooper())
) {
    companion object {
        private val TAG = bluetoothTagFor(BluetoothWindowedScanner::class.java)
        private const val START_SCAN_RESTRICTION_MILLIS = 30000L
        private const val MAX_STARTS_PER_RESTRICTION_WINDOW = 5

        @VisibleForTesting
        const val STOP_DELAY_MILLIS = 3000L

        @VisibleForTesting
        const val ANY_TOOTHBRUSH_CALLBACK_TOKEN = 99

        @VisibleForTesting
        const val DECREMENT_COUNTER_TOKEN = 98
    }

    /**
     * Keeps track of the number of times startScan was invoked during the restriction window
     */
    @VisibleForTesting
    val startsDuringRestrictionWindow = AtomicInteger(0)

    /**
     * Set of all tokens for which we requested a stopScan
     *
     * We'll use this to cancel the handler messages when bluetooth is off
     */
    @VisibleForTesting
    val stopScanTokens = mutableSetOf<Int>()

    fun startScan(
        filters: List<ScanFilter>,
        settings: ScanSettings,
        callback: KLScanCallback
    ) {
        handler.post {
            tokenForCallback(callback).let { token ->
                if (hasPendingStopScan(token)) {
                    /*
                    cancel stop request and save a startScan. Hurray!
                     */
                    Timber.tag(TAG).i("Avoided a startScan on %s", callback.bluetoothDevice)
                    removeDelayedStopScan(token)
                } else {
                    detectTooManyScans(callback)

                    Timber.tag(TAG).d(
                        "startScan for %s, accepted macs %s",
                        callback.bluetoothDevice,
                        filters.map { it.deviceAddress })
                    if (bluetoothUtils.isBluetoothEnabled) {
                        try {
                            scanner.startScan(filters, settings, callback)
                        } catch (e: IllegalArgumentException) {
                            Timber.e(e)
                        }
                    }
                }
            }
        }
    }

    fun stopScan(callback: KLScanCallback) {
        scanner.stopScan(callback)
    }

    fun stopScanWithDelay(callback: KLScanCallback) {
        handler.post {
            tokenForCallback(callback).let { token ->
                Timber.tag(TAG).d("Requested stopScan for %s", callback.bluetoothDevice)
                removeDelayedStopScan(token)

                stopScanTokens.add(token)

                handler.sendMessageDelayed(
                    getPostMessage(
                        Runnable {
                            Timber.tag(TAG).d("real stopScan for %s", callback.bluetoothDevice)
                            stopScan(callback)
                        },
                        token
                    ),
                    STOP_DELAY_MILLIS
                )
            }
        }
    }

    private fun removeDelayedStopScan(token: Int) {
        handler.removeCallbacksAndMessages(token)

        stopScanTokens.remove(token)
    }

    private fun hasPendingStopScan(token: Int) = handler.hasMessages(token)

    @VisibleForTesting
    fun tokenForCallback(callback: KLScanCallback): Int {
        return try {
            callback.bluetoothDevice?.address?.macToInt() ?: ANY_TOOTHBRUSH_CALLBACK_TOKEN
        } catch (e: NumberFormatException) {
            ANY_TOOTHBRUSH_CALLBACK_TOKEN
        }
    }

    @VisibleForTesting
    fun detectTooManyScans(callback: KLScanCallback) {
        val startsDuringWindow = startsDuringRestrictionWindow.incrementAndGet()
        if (startsDuringWindow >= MAX_STARTS_PER_RESTRICTION_WINDOW) {
            Timber.tag(TAG).w(
                "Too many scan starts during window (%s). Not scanning for %s",
                startsDuringWindow,
                callback.bluetoothDevice
            )
        }

        handler.sendMessageDelayed(
            getPostMessage(
                Runnable {
                    startsDuringRestrictionWindow.decrementAndGet()
                },
                DECREMENT_COUNTER_TOKEN
            ),
            START_SCAN_RESTRICTION_MILLIS
        )
    }

    @VisibleForTesting
    fun getPostMessage(runnable: Runnable, tokenAndWhat: Int): Message {
        val m = Message.obtain(handler, runnable)
        m.obj = tokenAndWhat
        m.what = tokenAndWhat
        return m
    }

    fun onBluetoothOff() {
        stopScanTokens.forEach { token ->
            handler.removeCallbacksAndMessages(token)
        }
        stopScanTokens.clear()

        handler.removeCallbacksAndMessages(DECREMENT_COUNTER_TOKEN)
        startsDuringRestrictionWindow.set(0)
    }
}

private fun String.macToInt() = replace(":", "").toLong(radix = 16).toInt()
