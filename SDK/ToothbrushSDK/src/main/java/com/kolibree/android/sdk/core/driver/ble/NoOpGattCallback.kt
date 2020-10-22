/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import android.bluetooth.BluetoothDevice
import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.sdk.core.driver.ble.nordic.KLManagerCallbacks
import java.util.UUID
import timber.log.Timber

/**
 * No-op [KLManagerCallbacks] implementation
 *
 * BLE library does not support removing the callback or passing null, thus we are forced to use
 * this trick
 */
internal object NoOpGattCallback : KLManagerCallbacks {
    override fun onNotify(uuid: UUID, value: ByteArray?) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onNotify")
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onDeviceConnecting ${device.address}")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onDeviceConnected ${device.address}")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onDeviceDisconnecting ${device.address}")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onDeviceDisconnected ${device.address}")
    }

    override fun onLinkLossOccurred(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onLinkLossOccurred ${device.address}")
    }

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onServicesDiscovered ${device.address}")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onDeviceReady ${device.address}")
    }

    override fun onBondingRequired(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onBondingRequired ${device.address}")
    }

    override fun onBonded(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onBonded ${device.address}")
    }

    override fun onBondingFailed(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onBondingFailed ${device.address}")
    }

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onError ${device.address}")
    }

    override fun onDeviceNotSupported(device: BluetoothDevice) {
        // no-op
        Timber.tag(TAG).d("NoOpBTCallback onDeviceNotSupported ${device.address}")
    }
}

private val TAG = bluetoothTagFor(NoOpGattCallback::class.java)
