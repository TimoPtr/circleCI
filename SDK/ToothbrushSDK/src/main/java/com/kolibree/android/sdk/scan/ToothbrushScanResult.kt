package com.kolibree.android.sdk.scan

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel

/**
 * Created by miguelaragues on 20/9/17.
 */
@Keep
interface ToothbrushScanResult : Parcelable {

    val mac: String

    /**
     * Get toothbrush name
     *
     * @return non null name
     */

    val name: String

    /**
     * Get toothbrush model
     *
     * @return non null toothbrush model
     */

    val model: ToothbrushModel

    /**
     * Get toothbrush owner device
     *
     * This method only applies to V2 toothbrushes
     *
     * @return the owner device of a V2 toothbrush, 0 if V1 one
     */
    val ownerDevice: Long

    /**
     * Check if M1 devices are running bootloader
     *
     * @return true if running bootloader, false otherwise
     */
    @Deprecated("This method is deprecated, please use toothbrushApp() method instead")
    val isRunningBootloader: Boolean

    /**
     * Check if the toothbrush can be seamlessly connected
     *
     * This method applies only to V2 toothbrushes
     *
     * @return true if the toothbrush can be seamlessly connected, false if not or V1
     */
    val isSeamlessConnectionAvailable: Boolean

    /**
     * Get the app that the toothbrush is currently running
     *
     * @return ToothbrushApp
     */
    val toothbrushApp: ToothbrushApp
}

@Keep
internal fun ToothbrushScanResult.isDfu(): Boolean = toothbrushApp == ToothbrushApp.DFU_BOOTLOADER
