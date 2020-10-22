package com.kolibree.android.sdk.core.driver.ble.nordic

import android.bluetooth.BluetoothDevice
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

/** [DfuUtils] tests  */
class DfuUtilsTest {

    @Test
    fun getDFUMac_rangeStart() {
        assertEquals("AA:BB:CC:DD:EE:01", DfuUtils.getDFUMac("AA:BB:CC:DD:EE:00"))
    }

    @Test
    fun getDFUMac_rangeUnit() {
        assertEquals("AA:BB:CC:DD:EE:BF", DfuUtils.getDFUMac("AA:BB:CC:DD:EE:BE"))
    }

    @Test
    fun getDFUMac_rangePow1() {
        assertEquals("AA:BB:CC:DD:EE:C0", DfuUtils.getDFUMac("AA:BB:CC:DD:EE:BF"))
    }

    @Test
    fun getDFUMac_rangeOverflow() {
        assertEquals("AA:BB:CC:DD:EE:00", DfuUtils.getDFUMac("AA:BB:CC:DD:EE:FF"))
    }

    @Test
    fun getDFUMac_doesNotReplaceEveryByte() {
        assertEquals("AA:AA:AA:AA:AA:AB", DfuUtils.getDFUMac("AA:AA:AA:AA:AA:AA"))
    }

    @Test
    fun getDFUMac_bluetoothDevice() {
        val device: BluetoothDevice = mock()
        whenever(device.address).thenReturn("AA:BB:CC:DD:EE:C2")
        assertEquals("AA:BB:CC:DD:EE:C3", DfuUtils.getDFUMac(device))
    }

    @Test
    fun getMainAppMac_rangeUnit() {
        assertEquals("AA:BB:CC:DD:EE:AF", DfuUtils.getMainAppMac("AA:BB:CC:DD:EE:B0"))
    }

    @Test
    fun getMainAppMac_rangePowMinus1() {
        assertEquals("AA:BB:CC:DD:EE:BF", DfuUtils.getMainAppMac("AA:BB:CC:DD:EE:C0"))
    }

    @Test
    fun getMainAppMac_rangeUnderflow() {
        assertEquals("AA:BB:CC:DD:EE:FF", DfuUtils.getMainAppMac("AA:BB:CC:DD:EE:00"))
    }
}
