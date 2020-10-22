/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.core

import androidx.annotation.CallSuper
import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.KLTBConnection

/**
 * Strategy that defines if we should scan for a toothbrush before every reconnection attempt
 */
@Keep
sealed class ScanBeforeReconnectStrategy {
    @CallSuper
    fun shouldScanBeforeReconnect(connection: KLTBConnection): Boolean {
        return internalShouldScanBeforeReconnect(connection)
    }

    protected abstract fun internalShouldScanBeforeReconnect(connection: KLTBConnection): Boolean
}

@Keep
object AlwaysScanBeforeReconnectStrategy : ScanBeforeReconnectStrategy() {
    override fun internalShouldScanBeforeReconnect(connection: KLTBConnection): Boolean {
        return true
    }
}

@Keep
object NeverScanBeforeReconnectStrategy : ScanBeforeReconnectStrategy() {
    override fun internalShouldScanBeforeReconnect(connection: KLTBConnection): Boolean = false
}
