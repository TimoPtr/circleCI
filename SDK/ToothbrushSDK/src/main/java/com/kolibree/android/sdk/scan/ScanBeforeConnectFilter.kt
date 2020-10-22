/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import com.kolibree.android.bluetoothTagFor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.core.ScanBeforeReconnectStrategy
import javax.inject.Inject
import timber.log.Timber

/**
 * Helper to decide whether we should start a BLE scan for a [KLTBConnection] before attempting to
 * connect to it or if we can skip the scan step
 *
 * Read https://kolibree.atlassian.net/browse/KLTB002-9867 description & comments for context
 */
internal class ScanBeforeConnectFilter
@Inject constructor(
    private val connectionScannedTracker: ConnectionScannedTracker,
    private val scanBeforeReconnectStrategy: ScanBeforeReconnectStrategy
) {

    fun scanBeforeConnect(connection: KLTBConnection): Boolean {
        /*
        There's a known issue where isConnectionAlreadyScanned even tho it's false

        Check documentation in [EstablishConnectionFilter] before touching this method
         */
        if (connectionScannedTracker.isConnectionAlreadyScanned(connection.toothbrush().mac)) {
            Timber.tag(TAG)
                .v(
                    "Connection already scanned: %s, is bootloader %s",
                    connection.mac(),
                    connection.toothbrush().isRunningBootloader
                )
            return false
        }

        return scanBeforeReconnectStrategy.shouldScanBeforeReconnect(connection)
            .also { Timber.tag(TAG).v("scanBeforeConnect $it (${connection.mac()})") }
    }
}

private val TAG = bluetoothTagFor(ScanBeforeConnectFilter::class)
