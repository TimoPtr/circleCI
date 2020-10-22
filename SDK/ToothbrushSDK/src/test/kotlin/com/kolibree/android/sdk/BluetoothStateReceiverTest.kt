/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.content.Intent
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.BluetoothStateReceiver.Companion.bluetoothStatusObservable
import com.kolibree.android.test.extensions.assertLastValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

class BluetoothStateReceiverTest : BaseUnitTest() {
    private val stateReceiver = BluetoothStateReceiver()

    override fun setup() {
        super.setup()

        BluetoothStateReceiver.sharedObservable = null
    }

    override fun tearDown() {
        super.tearDown()

        BluetoothStateReceiver.sharedObservable = null
    }

    @Test
    fun `bluetoothStatusObservable returns same instance for multiple invocations if there's at least one subscriber`() {
        val observable = bluetoothStatusObservable()

        observable.test()

        assertEquals(observable, bluetoothStatusObservable())
        assertEquals(observable, bluetoothStatusObservable())
    }

    @Test
    fun `bluetoothStatusObservable returns different instance after the last subscriber disposes the subscription`() {
        val observable1 = bluetoothStatusObservable()
        val observable2 = bluetoothStatusObservable()

        assertEquals(observable1, observable2)

        val observer1 = observable1.test()
        val observer2 = observable2.test()

        assertEquals(observable1, bluetoothStatusObservable())

        observer2.dispose()

        assertEquals(observable1, bluetoothStatusObservable())

        observer1.dispose()

        assertNotSame(observable1, bluetoothStatusObservable())
    }

    /*
    State handling
     */

    @Test
    fun `bluetoothStatusObservable emits true after receiving STATE_ON`() {
        val observer = bluetoothStatusObservable().test()
            .assertNotComplete()

        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(STATE_ON))

        observer.assertLastValue(true)
    }

    @Test
    fun `bluetoothStatusObservable emits false after receiving STATE_OFF`() {
        val observer = bluetoothStatusObservable().test()
            .assertNotComplete()

        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(STATE_OFF))

        observer.assertLastValue(false)
    }

    @Test
    fun `bluetoothStatusObservable emits nothing after receiving STATE_TURNING_OFF`() {
        val observer = bluetoothStatusObservable().test()
            .assertNotComplete()

        // since bluetoothStateRelay is a static val, we can't reset it to not emit anything :-(
        val countPreNewState = observer.valueCount()

        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(STATE_TURNING_OFF))

        observer.assertValueCount(countPreNewState)
            .assertNotComplete()
    }

    @Test
    fun `bluetoothStatusObservable emits nothing after receiving STATE_TURNING_ON`() {
        val observer = bluetoothStatusObservable().test()
            .assertNoValues()
            .assertNotComplete()

        val countPreNewState = observer.valueCount()

        stateReceiver.onReceive(mock(), mockBluetoothIntentWithState(STATE_TURNING_ON))

        observer.assertValueCount(countPreNewState)
            .assertNotComplete()
    }
}

internal fun mockBluetoothIntentWithState(state: Int): Intent {
    val intent = mock<Intent>()

    whenever(intent.action).thenReturn(BluetoothAdapter.ACTION_STATE_CHANGED)
    whenever(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
        .thenReturn(state)

    return intent
}
