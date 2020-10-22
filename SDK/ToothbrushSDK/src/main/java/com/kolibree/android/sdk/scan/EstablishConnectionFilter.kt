/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.Mac
import com.kolibree.android.sdk.dagger.ToothbrushSDKScope
import javax.inject.Inject

/**
 * Filters if we can attempt to establish a connection to a given [KLTBConnection]
 *
 * It's a mutable Singleton. Invoking [enableScanBeforeConnect] or [disableScanBeforeConnect] will
 * affect everyone that uses an instance.
 *
 * This class is thread safe
 *
 * Read https://kolibree.atlassian.net/browse/KLTB002-9867 description & comments for context
 *
 * Known Issue: we can't identify whether BT was switched off/on since our application was stopped.
 * This means that connectionScannedTracker.isConnectionAlreadyScanned(mac) might return true even
 * tho it should return false.
 *
 * When initializing KLTBConnectionPool from NightsWatch, what will happen is that the connection
 * attempt will time out because [ScanBeforeConnectFilter.scanBeforeConnect] will return false,
 * thus we won't initiate a scan
 */
@ToothbrushSDKScope
internal class EstablishConnectionFilter
@Inject constructor(private val connectionScannedTracker: ConnectionScannedTracker) {
    private var canScanBeforeConnect: Boolean = true

    @Synchronized
    fun canAttemptConnection(mac: Mac): Boolean {
        val isToothbrushAlreadyScanned =
            connectionScannedTracker.isConnectionAlreadyScanned(mac)

        return isToothbrushAlreadyScanned || canScanBeforeConnect
    }

    @Synchronized
    fun enableScanBeforeConnect() {
        canScanBeforeConnect = true
    }

    @Synchronized
    fun disableScanBeforeConnect() {
        canScanBeforeConnect = false
    }
}
