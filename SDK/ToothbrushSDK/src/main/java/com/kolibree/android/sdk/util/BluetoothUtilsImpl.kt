package com.kolibree.android.sdk.util

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.BluetoothStateReceiver
import com.kolibree.android.sdk.BluetoothStateReceiver.Companion.bluetoothStatusObservable
import com.kolibree.android.sdk.KolibreeAndroidSdk.Companion.getSdkComponent
import com.kolibree.android.sdk.core.detectLeaks
import io.reactivex.Observable
import javax.inject.Inject
import timber.log.Timber

/**
 * Created by aurelien on 19/08/16.
 *
 * Bluetooth utility
 *
 * 19/9/2017 - maragues
 *
 * This class is no longer static or final so that it can be mocked for tests
 */
internal class BluetoothUtilsImpl @Inject constructor(context: Context) : IBluetoothUtils {
    private val context: Context = context.applicationContext

    @VisibleForTesting
    @Volatile
    var receiver: BroadcastReceiver? = null

    @VisibleForTesting
    @Volatile
    var bluetoothStateObservable: Observable<Boolean>? = null

    override fun bluetoothStateObservable(): Observable<Boolean> {
        if (bluetoothStateObservable == null) {
            synchronized(this) {
                if (bluetoothStateObservable == null) {
                    bluetoothStateObservable =
                        bluetoothStatusObservable()
                            .doOnSubscribe { maybeRegisterBluetoothStateReceiver() }
                            .doFinally { onAllBluetoothStateObserversUnsubscribed() }
                            .publish()
                            .refCount()
                }
            }
        }
        return bluetoothStateObservable!!
    }

    /**
     * Android 8 and above prevents most implicit BroadcastReceivers, so we need to register
     * explicitly
     */
    @VisibleForTesting
    fun maybeRegisterBluetoothStateReceiver() {
        if (shouldRegisterExplicitBluetoothReceiver()) {
            synchronized(this) {
                if (shouldRegisterExplicitBluetoothReceiver()) {
                    receiver = BluetoothStateReceiver()
                    context.registerReceiver(
                        receiver,
                        IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                    )
                }
            }
        }
    }

    @VisibleForTesting
    fun shouldRegisterExplicitBluetoothReceiver(): Boolean {
        return VERSION.SDK_INT >= VERSION_CODES.O && receiver == null
    }

    @VisibleForTesting
    fun onAllBluetoothStateObserversUnsubscribed() {
        bluetoothStateObservable.detectLeaks("BluetoothUtils observable")
        synchronized(this) {
            bluetoothStateObservable = null
            maybeUnregisterBluetoothStateReceiver()
        }
    }

    @VisibleForTesting
    fun maybeUnregisterBluetoothStateReceiver() {
        if (receiver != null) {
            synchronized(this) {
                if (receiver != null) {
                    context.unregisterReceiver(receiver)
                    receiver = null
                }
            }
        }
    }

    /**
     * Check if the device is BLE compatible
     *
     * @return true if BLE is supported, false otherwise
     */
    override fun deviceSupportsBle(): Boolean {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true
        } else {
            Timber.w("Device does not have FEATURE_BLUETOOTH_LE")
        }
        return false
    }

    /**
     * Check if Bluetooth is activated on device
     *
     * @return true if activated, false otherwise
     */
    override val isBluetoothEnabled: Boolean
        get() {
            val bluetoothAdapterWrapper = getSdkComponent().bluetoothAdapterWrapper()
            return bluetoothAdapterWrapper != null && bluetoothAdapterWrapper.isEnabled
        }

    /**
     * Activate bluetooth on device
     *
     * @param enable boolean
     */
    override fun enableBluetooth(enable: Boolean) {
        val bluetoothAdapterWrapper = getSdkComponent().bluetoothAdapterWrapper()
        if (bluetoothAdapterWrapper != null) {
            if (enable) {
                bluetoothAdapterWrapper.enable()
            } else {
                bluetoothAdapterWrapper.disable()
            }
        }
    }
}
