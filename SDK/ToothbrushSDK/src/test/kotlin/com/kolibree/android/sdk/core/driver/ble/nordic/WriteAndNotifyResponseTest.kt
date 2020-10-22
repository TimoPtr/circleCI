package com.kolibree.android.sdk.core.driver.ble.nordic

import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.android.sdk.error.CommandFailedException
import com.kolibree.android.sdk.toHex
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nordicsemi.android.ble.data.Data
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WriteAndNotifyResponseTest : CommonBaseTest() {
    private val writeAndNotifyResponse = spy(WriteAndNotifyResponse())

    /*
    ON DATA RECEIVED
     */

    @Test
    fun onDataReceived_isActionCommandFalse_neverInvokesOnInvalidDataReceived() {
        whenever(writeAndNotifyResponse.isActionCommand()).thenReturn(false)

        writeAndNotifyResponse.onDataReceived(mock(), mockData())

        verify(writeAndNotifyResponse, never()).onInvalidDataReceived(any(), any())
    }

    @Test
    fun onDataReceived_isActionCommandTrue_validateResponseTrue_neverInvokesOnInvalidDataReceived() {
        whenever(writeAndNotifyResponse.isActionCommand()).thenReturn(true)
        whenever(writeAndNotifyResponse.validateActionCommandResponse()).thenReturn(true)

        writeAndNotifyResponse.onDataReceived(mock(), mockData())

        verify(writeAndNotifyResponse, never()).onInvalidDataReceived(any(), any())
    }

    @Test
    fun onDataReceived_isActionCommandTrue_validateResponseFalse_invokesOnInvalidDataReceived() {
        whenever(writeAndNotifyResponse.isActionCommand()).thenReturn(true)
        whenever(writeAndNotifyResponse.validateActionCommandResponse()).thenReturn(false)

        writeAndNotifyResponse.onDataReceived(mock(), mockData())

        verify(writeAndNotifyResponse).onInvalidDataReceived(any(), any())
    }

    /*
    COMMAND ID
     */
    @Test
    fun commandId_noData_returnsNull() {
        assertNull(writeAndNotifyResponse.commandId())
    }

    @Test
    fun commandId_emptyData_returnsNull() {
        mockDataReceived(byteArrayOf())

        assertNull(writeAndNotifyResponse.commandId())
    }

    @Test
    fun commandId_withData_returnsFirstByte() {
        val expectedByte: Byte = 0x15
        mockDataReceived(byteArrayOf(expectedByte))

        assertEquals(expectedByte, writeAndNotifyResponse.commandId())
    }

    /*
    IS ACTION COMMAND
     */
    @Test
    fun isActionCommand_between0x10And0x17_returnsTrue() {
        for (commandId in 0x10..0x17) {
            mockDataReceived(byteArrayOf(commandId.toByte()))

            assertTrue(writeAndNotifyResponse.isActionCommand())
        }
    }

    @Test
    fun isActionCommand_0x09_returnsFalse() {
        mockDataReceived(byteArrayOf(0x09.toByte()))

        assertFalse(writeAndNotifyResponse.isActionCommand())
    }

    @Test
    fun isActionCommand_0x18_returnsFalse() {
        mockDataReceived(byteArrayOf(0x18.toByte()))

        assertFalse(writeAndNotifyResponse.isActionCommand())
    }

    /*
    VALIDATE ACTION COMMAND RESPONSE
     */
    @Test
    fun validateActionCommandResponse_statusReturnsNull_returnsFalse() {
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(null)

        assertFalse(writeAndNotifyResponse.validateActionCommandResponse())
    }

    @Test
    fun validateActionCommandResponse_statusReturnsFailure_commandIdisNot0x13_returnsFalse() {
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(KLNordicBleManager.RESPONSE_FAILURE)
        whenever(writeAndNotifyResponse.commandId()).thenReturn(0x10)

        assertFalse(writeAndNotifyResponse.validateActionCommandResponse())
    }

    @Test
    fun validateActionCommandResponse_statusReturnsFailure_commandIdis0x13_returnsTrue() {
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(KLNordicBleManager.RESPONSE_FAILURE)
        whenever(writeAndNotifyResponse.commandId()).thenReturn(0x13)

        assertTrue(writeAndNotifyResponse.validateActionCommandResponse())
    }

    @Test
    fun validateActionCommandResponse_statusReturnsSuccess_commandIdis0x13_returnsTrue() {
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(KLNordicBleManager.RESPONSE_SUCCESS)
        whenever(writeAndNotifyResponse.commandId()).thenReturn(0x13)

        assertTrue(writeAndNotifyResponse.validateActionCommandResponse())
    }

    @Test
    fun validateActionCommandResponse_statusReturnsSuccess_commandIdisNot0x13_returnsTrue() {
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(KLNordicBleManager.RESPONSE_SUCCESS)
        whenever(writeAndNotifyResponse.commandId()).thenReturn(0x10)

        assertTrue(writeAndNotifyResponse.validateActionCommandResponse())
    }

    /*
    STATUS
     */
    @Test
    fun status_statusByteReturnsNull_returnsNoData() {
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(null)

        assertEquals("no data", writeAndNotifyResponse.status())
    }

    @Test
    fun status_statusByteReturnsNull_returnsStatusAsString() {
        val expectedStatus: Byte = 0x08
        whenever(writeAndNotifyResponse.statusByte()).thenReturn(expectedStatus)

        assertEquals(expectedStatus.toHex(), writeAndNotifyResponse.status())
    }

    /*
    STATUS
     */
    @Test
    fun statusByte_responseMinSizeFalse_returnNull() {
        whenever(writeAndNotifyResponse.responseHasMinSize()).thenReturn(false)

        assertNull(writeAndNotifyResponse.statusByte())
    }

    @Test
    fun statusByte_responseMinSizeTrue_returnsNoData() {
        whenever(writeAndNotifyResponse.responseHasMinSize()).thenReturn(true)

        val expectedStatus: Byte = 0x08
        mockDataReceived(byteArrayOf(0x00, expectedStatus))

        assertEquals(expectedStatus, writeAndNotifyResponse.statusByte())
    }

    /*
    responseThrowIfNotValid
     */

    @Test(expected = CommandFailedException::class)
    fun `when response is null, throw CommandFailedException`() {
        assertNull(writeAndNotifyResponse.response())

        writeAndNotifyResponse.responseThrowIfNotValid()
    }

    @Test(expected = CommandFailedException::class)
    fun `when operation is not valid, throw CommandFailedException`() {
        writeAndNotifyResponse.onInvalidDataReceived(mock(), Data())

        assertFalse(writeAndNotifyResponse.isValid)

        writeAndNotifyResponse.responseThrowIfNotValid()
    }

    @Test
    fun `when operation is valid and response is not null, return response`() {
        val expectedResponse = byteArrayOf(1)
        mockDataReceived(expectedResponse)

        assertTrue(writeAndNotifyResponse.isValid)
        assertNotNull(writeAndNotifyResponse.response())

        assertEquals(expectedResponse, writeAndNotifyResponse.responseThrowIfNotValid())
    }

    /*
    RESPONSE HAS MIN SIZE
     */
    @Test
    fun responseHasMinSize_rawDataIsNull_returnsFalse() {
        mockDataReceived(mock<Data>())

        assertFalse(writeAndNotifyResponse.responseHasMinSize())
    }

    @Test
    fun responseHasMinSize_sizeIs0_returnsFalse() {
        mockDataReceived(byteArrayOf())

        assertFalse(writeAndNotifyResponse.responseHasMinSize())
    }

    @Test
    fun responseHasMinSize_sizeIs1_returnsFalse() {
        mockDataReceived(byteArrayOf(0x01))

        assertFalse(writeAndNotifyResponse.responseHasMinSize())
    }

    @Test
    fun responseHasMinSize_sizeIs2_returnsTrue() {
        mockDataReceived(byteArrayOf(0x01, 0x02))

        assertTrue(writeAndNotifyResponse.responseHasMinSize())
    }

    private fun mockDataReceived(data: Data) {
        writeAndNotifyResponse.onDataReceived(mock(), data)
    }

    private fun mockDataReceived(response: ByteArray = byteArrayOf()) {
        writeAndNotifyResponse.onDataReceived(mock(), mockData(response))
    }

    private fun mockData(response: ByteArray = byteArrayOf()): Data {
        return Data(response)
    }
}
