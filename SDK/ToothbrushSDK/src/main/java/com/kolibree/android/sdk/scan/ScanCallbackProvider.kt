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
import java.util.HashMap

/**
 * Provides ScanCallback instances for ToothbrushScanCallback
 */
internal class ScanCallbackProvider(
    private val scanResultFactory: ToothbrushScanResultFactory = ToothbrushScanResultFactory()
) {

    private val callbackMap: HashMap<BluetoothDevice?, KLScanCallback> = hashMapOf()

    /**
     * Returns a ScanCallback for the given BluetoothDevice
     *
     * Multiple calls with the same parameter will return the same callback
     */
    fun getOrCreate(toothbrushScanCallback: ToothbrushScanCallback): KLScanCallback {
        synchronized(callbackMap) {
            val scanCallback =
                callbackMap.getOrPut(toothbrushScanCallback.bluetoothDevice()) {
                    scanCallback(
                        toothbrushScanCallback
                    )
                }

            scanCallback.addListener(toothbrushScanCallback)

            return scanCallback
        }
    }

    @VisibleForTesting
    fun scanCallback(toothbrushScanCallback: ToothbrushScanCallback) =
        KLScanCallback(scanResultFactory, toothbrushScanCallback.bluetoothDevice())

    fun get(callback: ToothbrushScanCallback): KLScanCallback? {
        synchronized(callbackMap) {
            return callbackMap[callback.bluetoothDevice()]
        }
    }
}
