package com.kolibree.android.sdk

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.jakewharton.rx.ReplayingShare
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.sdk.core.detectLeaks
import io.reactivex.Observable

/**
 * Created by aurelien on 07/01/2017.
 *
 * Bluetooth events receiver
 */
internal class BluetoothStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.isBluetoothOn()?.let { isBluetoothOn ->
            bluetoothStateRelay.accept(isBluetoothOn)
        }
    }

    companion object {

        private val bluetoothStateRelay = BehaviorRelay.create<Boolean>()

        @Volatile
        @VisibleForTesting
        var sharedObservable: Observable<Boolean>? = null

        fun bluetoothStatusObservable(): Observable<Boolean> {
            if (sharedObservable == null) {
                synchronized(bluetoothStateRelay) {
                    if (sharedObservable == null) {
                        sharedObservable = bluetoothStateRelay
                            .doFinally { onAllConnectionObserversUnsubscribed() }
                            .compose(ReplayingShare.instance())
                    }
                }
            }

            return sharedObservable!!
        }

        private fun onAllConnectionObserversUnsubscribed() {
            sharedObservable.detectLeaks("BluetoothState observable")
            synchronized(bluetoothStateRelay) {
                sharedObservable = null
            }
        }
    }
}

/**
 * Returns a value if
 * - The Intent contains information about Bluetooth state change
 * - State is [BluetoothAdapter.STATE_ON] or [BluetoothAdapter.STATE_OFF]
 *
 * Otherwise, returns null
 *
 * @return true or false if bluetooth is on/off, null if it's in a different state or if we can't
 * infer the information from the [Intent]
 */
internal fun Intent.isBluetoothOn(): Boolean? {
    return action?.let {
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(it, ignoreCase = true)) {
            val stateCode = getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

            bluetoothEnabledValue(stateCode)
        } else {
            null
        }
    }
}

/**
 * Maps BluetoothAdapter states to a Boolean value we want to emit
 *
 * Since we only want to inform is bluetooth is on/off, we ignore values different from ON/OFF
 *
 * @return true if state is STATE_ON, false if STATE_OFF. null otherwise
 */
private fun bluetoothEnabledValue(stateCode: Int): Boolean? {
    return when (stateCode) {
        BluetoothAdapter.STATE_ON -> true
        BluetoothAdapter.STATE_OFF -> false
        else -> null
    }
}
