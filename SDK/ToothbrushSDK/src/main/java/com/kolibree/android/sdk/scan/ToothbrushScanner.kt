package com.kolibree.android.sdk.scan

import android.app.PendingIntent
import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.sdk.error.BluetoothNotEnabledException

/**
 * Created by miguelaragues on 18/9/17.
 */
@Keep
interface ToothbrushScanner {

    /**
     * Look for Kolibree toothbrushes
     *
     * Toothbrushes found will be notified to callback
     *
     * @param callback Callback to be notified when a toothbrush is found or if there are errors
     * @param includeBondedDevices if set the scanner will also add bonded toothbrushes
     */
    @Throws(BluetoothNotEnabledException::class)
    fun startScan(callback: AnyToothbrushScanCallback, includeBondedDevices: Boolean)

    /**
     * Look for Kolibree toothbrushes in low power mode
     *
     * The scan results will be delivered via the PendingIntent. Use this method of scanning if your
     * process is not always running and it should be started when scan results are available.
     *
     * When the PendingIntent is delivered, the Intent passed to the receiver or activity will
     * contain one or more of the extras EXTRA_CALLBACK_TYPE, EXTRA_ERROR_CODE and
     * EXTRA_LIST_SCAN_RESULT to indicate the result of the scan.
     *
     * If [macAddresses] is empty, invoking this method will have no effect
     *
     * @param context [Context]
     * @param macAddresses [List]<[String]> non-v1 mac addresses we want to be notified of
     * @param pendingIntent [PendingIntent] used to notify of successful scan results
     *
     * @return true if startScan succeeded or [macAddresses] is empty. false if Bluetooth is off
     */
    fun startScan(
        context: Context,
        macAddresses: List<String>,
        pendingIntent: PendingIntent
    ): Boolean

    /**
     * Look for the specific BluetoothDevice
     *
     * @param specificToothbrushScanCallback Callback to be notified when a toothbrush is found or if there are errors
     */
    @Throws(BluetoothNotEnabledException::class)
    fun scanFor(specificToothbrushScanCallback: SpecificToothbrushScanCallback)

    /**
     * Stop delivering scan results to callback
     *
     * If it was the only callback looking for callback.BluetoothDevice, we will stop the scan
     */
    fun stopScan(callback: ToothbrushScanCallback)

    /**
     * Stop delivering scan results through PendingIntent
     *
     * If Bluetooth is off, invoking this method has no effect
     */
    fun stopScan(context: Context, pendingIntent: PendingIntent)
}
