package com.kolibree.android.sdk.util

import androidx.annotation.Keep
import io.reactivex.Observable

/**
 * Created by miguelaragues on 19/9/17.
 */
@Keep
interface IBluetoothUtils {

    val isBluetoothEnabled: Boolean
    fun deviceSupportsBle(): Boolean

    fun enableBluetooth(enable: Boolean)

    /**
     * Returns an Observable that emits bluetooth status updates
     */
    fun bluetoothStateObservable(): Observable<Boolean>
}
