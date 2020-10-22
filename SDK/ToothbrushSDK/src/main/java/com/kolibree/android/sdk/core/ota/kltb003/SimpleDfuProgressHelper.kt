package com.kolibree.android.sdk.core.ota.kltb003

import no.nordicsemi.android.dfu.DfuProgressListenerAdapter

/** Simplified version of [DfuProgressListenerAdapter]  */
internal abstract class SimpleDfuProgressHelper : DfuProgressListenerAdapter() {

    override fun onProgressChanged(
        deviceAddress: String,
        percent: Int,
        speed: Float,
        avgSpeed: Float,
        currentPart: Int,
        partsTotal: Int
    ) {
        onProgress(percent)
    }

    override fun onDeviceDisconnecting(deviceAddress: String?) {
        onDeviceRebooting()
    }

    protected abstract fun onProgress(percent: Int)

    protected abstract fun onDeviceRebooting()
}
