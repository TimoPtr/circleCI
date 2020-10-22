package com.kolibree.android.sdk.bluetooth

import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import com.kolibree.android.annotation.VisibleForApp

/**
 * Wrapper around BluetoothAdapter so that we can mock the bluetooth dependencies during testing
 *
 * Created by miguelaragues on 19/9/17.
 */

@VisibleForApp
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface BluetoothAdapterWrapper {

    val bondedDevices: Set<BluetoothDevice>

    @get:RequiresApi(api = VERSION_CODES.LOLLIPOP)
    val bluetoothLeScanner: BluetoothLeScanner?

    val isEnabled: Boolean

    /**
     * Returns a BluetoothDevice unless the hardware device doesn't support Bluetooth. In that case,
     * it returns null.
     *
     * If the MAC is invalid, it'll throw a IllegalArgumentException
     *
     * @param mac a well formed Mac address that identifies the device
     */
    fun getRemoteDevice(mac: String): BluetoothDevice?

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    fun startLeScan(scanCallback: LeScanCallback): Boolean

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    fun stopLeScan(scanCallback: LeScanCallback)

    fun startDiscovery(): Boolean

    fun enable(): Boolean

    fun disable(): Boolean

    fun cancelDiscovery(): Boolean
}
