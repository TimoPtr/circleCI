/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import android.content.Intent
import androidx.annotation.Keep
import javax.inject.Inject
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult

/**
 * Class to extract [ScanResult] from an [Intent]
 *
 * It invokes [connectionScannedTracker] for every scan result
 */
@Keep
class IntentScanResultProcessor
@Inject constructor(private val connectionScannedTracker: ConnectionScannedTracker) {
    fun process(intent: Intent): List<String> {
        val macsInIntent = readMacs(intent)

        macsInIntent.forEach { mac ->
            connectionScannedTracker.onConnectionScanned(mac)
        }

        return macsInIntent
    }

    private fun readMacs(intent: Intent): List<String> {
        return if (!intent.hasExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE)) {
            intent.getParcelableArrayListExtra<ScanResult>(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT)
                ?.map { scanResult -> scanResult.device.address }
                ?: emptyList()
        } else {
            emptyList()
        }
    }
}
