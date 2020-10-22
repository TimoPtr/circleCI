package com.kolibree.android.sdk.core.driver

internal interface BaseInternalDriver {
    fun cancelPendingOperations()

    /**
     * Check if the toothbrush is running in bootloader mode
     *
     * Will return false on Kolibree toothbrushes
     *
     * @return true if the toothbrush is running in bootloader mode, false otherwise
     */
    var isRunningBootloader: Boolean
}
