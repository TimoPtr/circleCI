package com.kolibree.android.sdk.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import java.util.HashSet

/**
 * Implementation of BluetoothAdapterWrapper that delegates all calls to the real BluetoothAdapter
 *
 *
 * Created by miguelaragues on 19/9/17.
 */
internal class BluetoothAdapterWrapperImpl private constructor() : BluetoothAdapterWrapper {
    private val defaultAdapter: BluetoothAdapter?
        get() = BluetoothAdapter.getDefaultAdapter()

    override fun getRemoteDevice(mac: String): BluetoothDevice? {
        val adapter = defaultAdapter ?: return null
        return adapter.getRemoteDevice(mac)
    }

    override val bondedDevices: Set<BluetoothDevice>
        get() {
            val adapter = defaultAdapter ?: return HashSet()
            return adapter.bondedDevices
        }

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    override fun startLeScan(scanCallback: LeScanCallback): Boolean {
        val adapter = defaultAdapter
        return adapter != null && adapter.startLeScan(scanCallback)
    }

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    override fun stopLeScan(scanCallback: LeScanCallback) {
        val adapter = defaultAdapter ?: return
        adapter.stopLeScan(scanCallback)
    }

    @get:RequiresApi(api = VERSION_CODES.LOLLIPOP)
    override val bluetoothLeScanner: BluetoothLeScanner?
        get() {
            val adapter = defaultAdapter ?: return null
            return adapter.bluetoothLeScanner
        }

    override fun startDiscovery(): Boolean {
        val adapter = defaultAdapter
        return adapter != null && adapter.startDiscovery()
    }

    override val isEnabled: Boolean
        get() {
            val adapter = defaultAdapter
            return adapter != null && adapter.isEnabled
        }

    override fun enable(): Boolean {
        val adapter = defaultAdapter
        return adapter != null && adapter.enable()
    }

    override fun disable(): Boolean {
        val adapter = defaultAdapter
        return adapter != null && adapter.disable()
    }

    override fun cancelDiscovery(): Boolean {
        val adapter = defaultAdapter
        return adapter != null && adapter.cancelDiscovery()
    }

    companion object {
        fun create(): BluetoothAdapterWrapper = BluetoothAdapterWrapperImpl()
    }
}
