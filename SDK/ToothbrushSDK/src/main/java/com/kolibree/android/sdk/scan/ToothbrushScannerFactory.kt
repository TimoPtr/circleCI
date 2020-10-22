package com.kolibree.android.sdk.scan

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel

/**
 * Created by miguelaragues on 21/9/17.
 */
@Keep
interface ToothbrushScannerFactory {

    /**
     * Returns a BLE scanner if the device supports BLE
     */
    fun getCompatibleBleScanner(): ToothbrushScanner?

    fun getScanner(context: Context, model: ToothbrushModel): ToothbrushScanner?
}
