package com.kolibree.android.sdk.scan

import android.bluetooth.BluetoothDevice
import androidx.annotation.Keep

/**
 * Created by aurelien on 19/08/16.
 *
 * Callback for Kolibree toothbrushes bluetooth scan process
 * All callbacks are called from UI thread
 */
@Keep
interface ToothbrushScanCallback {

    /**
     * Called when a Kolibree toothbrush is found
     *
     * Warning : this method's implementation should be synchronized as two threads may call it
     *
     * @param result a non null Toothbrush scan result
     */
    fun onToothbrushFound(result: ToothbrushScanResult)

    /**
     * Called when scan fails
     */
    fun onError(errorCode: Int)

    fun bluetoothDevice(): BluetoothDevice?
}

/**
 * Callback scanning for any Toothbrush
 */
@Keep
interface AnyToothbrushScanCallback : ToothbrushScanCallback

/**
 * Callback scanning for a specific Toothbrush
 */
@Keep
interface SpecificToothbrushScanCallback : ToothbrushScanCallback
