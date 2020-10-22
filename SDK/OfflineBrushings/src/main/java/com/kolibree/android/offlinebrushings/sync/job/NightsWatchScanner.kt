/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.Context
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import javax.inject.Inject
import timber.log.Timber

internal interface NightsWatchScanner {
    fun startScan(): StartScanResult

    fun stopScan()
}

internal enum class StartScanResult {
    FAILURE,
    /**
     * There's no need to start scan in the background
     *
     * This probably means
     * - User has zero toothbrushes
     * - User only has V1 toothbrushes
     */
    SCAN_NOT_NEEDED,
    SUCCESS
}

internal class NightsWatchScannerImpl @Inject constructor(
    context: Context,
    private val toothbrushScannerFactory: ToothbrushScannerFactory,
    private val scanPendingIntentProvider: ScanPendingIntentProvider,
    private val macsToScanProvider: NightsWatchMacsToScanProvider
) : NightsWatchScanner {
    private val applicationContext = context.applicationContext

    /**
     * Attempts to start scanning for toothbrushes
     *
     * There are at least 4 scenarios under which we won't start scanning
     * - Bluetooth is not available
     * - User has zero toothbrushes
     * - User only has V1 toothbrushes
     * - Device does not support BLE
     *
     * @return [StartScanResult]
     */
    override fun startScan(): StartScanResult {
        val scanner = toothbrushScannerFactory.getCompatibleBleScanner()

        if (scanner == null) {
            Timber.tag(tag()).w("Device does not support BLE")

            return StartScanResult.SCAN_NOT_NEEDED
        }

        val macs = macsToScanProvider.provide()

        if (macs.isEmpty()) {
            Timber.tag(tag()).d("No toothbrush paired, canceling background extraction")

            return StartScanResult.SCAN_NOT_NEEDED
        }

        return realStartScan(scanner, macs)
    }

    private fun realStartScan(
        scanner: ToothbrushScanner,
        macs: List<String>
    ): StartScanResult {
        val startScanSucceeded = scanner.startScan(
            context = applicationContext,
            macAddresses = macs,
            pendingIntent = scanPendingIntentProvider.provide()
        )

        return if (startScanSucceeded) {
            Timber.tag(tag()).i("Invoked startScan on $macs")

            StartScanResult.SUCCESS
        } else {
            Timber.tag(tag()).d("Bluetooth not available, not starting scan")

            StartScanResult.FAILURE
        }
    }

    override fun stopScan() {
        toothbrushScannerFactory.getCompatibleBleScanner()?.let { scanner ->
            scanner.stopScan(applicationContext, scanPendingIntentProvider.provide())

            Timber.tag(tag()).i("Invoked stopScan")
        } ?: Timber.tag(tag()).d("Bluetooth not available, not starting scan")
    }
}
