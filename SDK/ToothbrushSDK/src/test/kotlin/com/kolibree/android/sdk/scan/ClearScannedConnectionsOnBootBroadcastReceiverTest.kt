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
import android.content.Intent
import android.location.LocationManager
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.mockBluetoothIntentWithState
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ClearScannedConnectionsOnBootBroadcastReceiverTest : BaseUnitTest() {
    private var stateReceiver = BluetoothSessionResetterBroadcastReceiver()

    @Test
    fun `onReceive invokes connectionScannedTracker clear after receiving reboot event`() {
        spyStateReceiver()

        rebootActions.forEach { action ->
            val connectionScannedTracker = mockConnectionScannedTracker()

            val intent: Intent = mock<Intent>().apply {
                whenever(this.action).thenReturn(action)
            }

            stateReceiver.onReceive(mock(), intent)

            verify(connectionScannedTracker).clear()
        }
    }

    @Test
    fun `onReceive never invokes connectionScannedTracker clear after receiving STATE_ON`() {
        /*
        This would crash if we attempted to clear connectionScannedTracker
         */
        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(BluetoothAdapter.STATE_ON))
    }

    @Test
    fun `onReceive never invokes connectionScannedTracker clear after receiving STATE_TURNING_OFF`() {
        /*
        This would crash if we attempted to clear connectionScannedTracker
         */
        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(BluetoothAdapter.STATE_TURNING_OFF))
    }

    @Test
    fun `onReceive never invokes connectionScannedTracker clear after receiving STATE_TURNING_ON`() {
        /*
        This would crash if we attempted to clear connectionScannedTracker
         */
        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(BluetoothAdapter.STATE_TURNING_ON))
    }

    @Test
    fun `onReceive invokes connectionScannedTracker clear after receiving STATE_OFF`() {
        spyStateReceiver()

        val connectionScannedTracker = mockConnectionScannedTracker()

        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(BluetoothAdapter.STATE_OFF))

        verify(connectionScannedTracker).clear()
    }

    @Test
    fun `onReceive does nothing if Intent does not refer to Bluetooth state change`() {
        val intent = mock<Intent>()

        whenever(intent.action).thenReturn(LocationManager.PROVIDERS_CHANGED_ACTION)

        /*
        These would crash if we attempted to clear connectionScannedTracker
         */
        stateReceiver.onReceive(mock(), intent)
        stateReceiver.onReceive(mock(), mock())
    }

    /*
    Utils
     */

    private fun spyStateReceiver() {
        stateReceiver = spy(BluetoothSessionResetterBroadcastReceiver())
    }

    private fun mockConnectionScannedTracker(): ConnectionScannedTracker {
        val connectionScannedTracker = mock<ConnectionScannedTracker>()
        doReturn(connectionScannedTracker)
            .whenever(stateReceiver)
            .connectionScannedTracker(any())
        return connectionScannedTracker
    }
}

private val rebootActions = arrayOf(
    Intent.ACTION_BOOT_COMPLETED,
    "android.intent.action.QUICKBOOT_POWERON",
    /*For HTC devices*/
    "com.htc.intent.action.QUICKBOOT_POWERON"
)
