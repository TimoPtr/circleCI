package com.kolibree.android.sdk.connection.user

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.sdk.connection.user.UserImpl.Companion.SET_PROFILE_ID_REQUEST_PAYLOAD_LENGTH
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_USER_ID
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class UserImplTest : BaseUnitTest() {

    private val bleDriver = mock<BleDriver>()

    private lateinit var userImpl: UserImpl

    override fun setup() {
        super.setup()

        userImpl = spy(UserImpl(CONNECT_E1, bleDriver))
    }

    /*
    Companion
     */

    @Test
    fun `Companion's SET_PROFILE_ID_REQUEST_PAYLOAD_LENGTH constant value is 5`() {
        assertEquals(5, SET_PROFILE_ID_REQUEST_PAYLOAD_LENGTH)
    }

    /*
    setToothbrushProfileId()
     */

    @Test
    fun `setToothbrushProfileId() prepares payload and calls driver's setDeviceParameter()`() {
        val profileId = 1986L
        userImpl.setToothbrushProfileId(profileId)
        verify(userImpl).createRequestPayload(profileId)
        verify(bleDriver).setDeviceParameter(any())
    }

    /*
    getToothbrushProfileId()
     */

    @Test
    fun `getToothbrushProfileId() parses response from drivers's getDeviceParameter()`() {
        val payload = PayloadReader(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04))
        whenever(bleDriver.getDeviceParameter(any())).thenReturn(payload)
        val profileId = userImpl.getToothbrushProfileId()
        verify(userImpl).getProfileIdDeviceParameter()
        verify(userImpl).parseResponsePayload(payload)
        assertEquals(67305985L, profileId)
    }

    /*
    isToothbrushRunningBootloader()
     */

    @Test
    fun `isToothbrushRunningBootloader() calls driver's isRunningBootloader() method`() {
        userImpl.isToothbrushRunningBootloader()
        verify(bleDriver).isRunningBootloader
    }

    /*
    parseResponsePayload()
     */

    @Test
    fun `parseResponsePayload() correctly parses profile ID`() {
        val payload = PayloadReader(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04))
        assertEquals(67305985L, userImpl.parseResponsePayload(payload))
    }

    /*
    getProfileIdDeviceParameter()
     */

    @Test
    fun `getProfileIdDeviceParameter() calls driver's getDeviceParameter with good payload`() {
        userImpl.getProfileIdDeviceParameter()
        verify(bleDriver).getDeviceParameter(byteArrayOf(DEVICE_PARAMETERS_USER_ID))
    }

    /*
    createRequestPayload()
     */

    @Test
    fun `createRequestPayload() returns good payload`() {
        assertArrayEquals(byteArrayOf(DEVICE_PARAMETERS_USER_ID, 0x01, 0x02, 0x03, 0x04),
            userImpl.createRequestPayload(67305985L))
    }
}
