package no.nordicsemi.android.ble

import android.bluetooth.BluetoothDevice
import android.os.Handler
import com.kolibree.android.sdk.core.driver.ble.nordic.KLManagerCallbacks
import com.kolibree.android.test.utils.ReflectionUtils
import com.nhaarman.mockitokotlin2.mock

internal class ConnectRequestStub(private val bluetoothDevice: BluetoothDevice) :
    ConnectRequest(Type.CONNECT, bluetoothDevice) {
    fun isAutoConnect() = super.shouldAutoConnect()

    fun isRetry() = canRetry()

    fun retryDelay() = super.getRetryDelay()

    fun succeed() {
        mockHandler()

        super.notifySuccess(bluetoothDevice)
    }

    fun fail() {
        mockHandler()

        super.notifyFail(bluetoothDevice, 1)
    }

    fun setTestManager(bleManager: BleManager<KLManagerCallbacks>) = super.setManager(bleManager)

    private fun mockHandler() {
        ReflectionUtils.setPrivateField(TimeoutableRequest::class.java, this, "handler", mock<Handler>())
    }
}
