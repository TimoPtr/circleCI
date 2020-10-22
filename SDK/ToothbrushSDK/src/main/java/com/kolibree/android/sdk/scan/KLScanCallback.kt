/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.scan

import android.bluetooth.BluetoothDevice
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.core.notification.ListenerNotifier
import com.kolibree.android.sdk.core.notification.ListenerPool
import com.kolibree.android.sdk.core.notification.UniqueListenerPool
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import timber.log.Timber

internal class KLScanCallback(
    private val scanResultFactory: ToothbrushScanResultFactory,
    val bluetoothDevice: BluetoothDevice?,
    @VisibleForTesting
    val listeners: ListenerPool<ToothbrushScanCallback> =
        UniqueListenerPool(
            "${KLScanCallback::class.java.simpleName} for $bluetoothDevice",
            true
        ),
    private val connectionScannedTracker: ConnectionScannedTracker =
        ConnectionScannedTracker(KolibreeAndroidSdk.getSdkComponent().applicationContext())
) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        Timber.tag(NordicBleScannerWrapper.TAG).d(
            "Scan result %s in %s, associated to %s",
            result,
            this,
            bluetoothDevice
        )

        try {
            val scanResult = scanResultFactory.parseScanResult(result)

            connectionScannedTracker.onConnectionScanned(scanResult.mac)

            listeners.notifyListeners(ListenerNotifier { it.onToothbrushFound(scanResult) })
        } catch (ignore: NotKolibreeToothbrushException) {
        }
    }

    override fun onBatchScanResults(results: List<ScanResult>) {
        Timber.tag(NordicBleScannerWrapper.TAG)
            .d("Batch results %d. Scanner is %s", results.size, this)
        var i = 0
        val size = results.size
        while (i < size) {
            Timber.tag(NordicBleScannerWrapper.TAG).d("Result %s", results[i])
            onScanResult(0, results[i])
            i++
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Timber.e("onScanFailed %s for %s", errorCode, bluetoothDevice)
        listeners.notifyListeners(ListenerNotifier { it.onError(errorCode) })
    }

    fun addListener(toothbrushScanCallback: ToothbrushScanCallback) {
        listeners.add(toothbrushScanCallback)
    }

    fun removeListener(toothbrushScanCallback: ToothbrushScanCallback) {
        listeners.remove(toothbrushScanCallback)
    }

    fun hasListeners() = listeners.size() > 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KLScanCallback) return false

        if (bluetoothDevice != other.bluetoothDevice) return false

        return true
    }

    override fun hashCode(): Int {
        return bluetoothDevice?.hashCode() ?: 0
    }
}
