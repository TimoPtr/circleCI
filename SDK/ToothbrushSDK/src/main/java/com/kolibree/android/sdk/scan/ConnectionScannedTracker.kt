/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.Mac
import com.kolibree.android.sdk.isBluetoothOn
import io.reactivex.Completable
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Tracks whether we have received a scan result for a given [KLTBConnection]
 *
 * It should be cleared after every reboot and after every BT off event.
 *
 * Read https://kolibree.atlassian.net/browse/KLTB002-9867 description & comments for context
 */
@Keep
class ConnectionScannedTracker
@Inject constructor(context: Context) : BasePreferencesImpl(context), Truncable {
    override fun getPreferencesName(): String = PREFS_NAME_ESTABLISHED_MACS

    internal fun isConnectionAlreadyScanned(mac: Mac): Boolean {
        return prefs.getBoolean(mac, false)
    }

    fun onConnectionScanned(mac: Mac) {
        prefs.edit {
            putBoolean(mac, true)
        }
    }

    private companion object {
        const val PREFS_NAME_ESTABLISHED_MACS = "macs_already_established"
    }

    override fun truncate(): Completable = Completable.fromAction { clear() }
}

/**
 * Singleton to ensure we only register one ClearScannedConnectionsBroadcastReceiver per process
 */
internal object BluetoothSessionResetterRegisterer {
    @VisibleForTesting
    internal val isRegistered = AtomicBoolean(false)

    /**
     * Registers [BluetoothSessionResetterBroadcastReceiver] once per process
     *
     * If the instance is already initialized, invoking this method does nothing
     */
    fun register(context: Context) {
        if (isRegistered.compareAndSet(false, true)) {
            registerBluetoothStateChangedReceiver(context)
        }
    }

    @VisibleForTesting
    fun registerBluetoothStateChangedReceiver(context: Context) {
        context.registerReceiver(
            BluetoothSessionResetterBroadcastReceiver(),
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }
}

/**
 * BroadcastReceiver that will reset [ConnectionScannedTracker] when bluetooth is turned off
 * or the phone reboots
 *
 * Multiple instances of this class will coexist
 * - One instantiated from the System to listen for BOOT events
 * - A second one instantiated by [BluetoothSessionResetterRegisterer]
 */
internal class BluetoothSessionResetterBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (shouldClearConnections(intent)) {
            connectionScannedTracker(context).clear()
        }
    }

    private fun shouldClearConnections(intent: Intent): Boolean {
        return isBluetoothSwitchedOffEvent(intent) || isRebootEvent(intent)
    }

    private fun isBluetoothSwitchedOffEvent(intent: Intent): Boolean =
        intent.isBluetoothOn()?.not() ?: false

    private fun isRebootEvent(intent: Intent): Boolean = intent.action in rebootActions

    @VisibleForTesting
    fun connectionScannedTracker(context: Context) = ConnectionScannedTracker(context)
}

private const val ACTION_REBOOT = "android.intent.action.QUICKBOOT_POWERON"
private const val ACTION_REBOOT_HTC = "com.htc.intent.action.QUICKBOOT_POWERON"

/*
https://stackoverflow.com/a/46294732/218473
 */
private val rebootActions = arrayOf(
    Intent.ACTION_BOOT_COMPLETED,
    ACTION_REBOOT,
    /*For HTC devices*/
    ACTION_REBOOT_HTC
)
