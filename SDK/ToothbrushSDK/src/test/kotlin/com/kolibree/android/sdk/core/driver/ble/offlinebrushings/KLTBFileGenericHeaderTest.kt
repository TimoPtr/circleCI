package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileHeaderParseException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.KLTBFileGenericHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class KLTBFileGenericHeaderTest : CommonBaseTest() {
    @Test(expected = FileHeaderParseException::class)
    fun create_emptyList_throwsHeaderParseException() {
        KLTBFileGenericHeader.create(ByteArray(0))
    }

    @Test
    fun create_listOfSizeBelow6_throwsHeaderParseException() {
        var exceptionCounter = 0
        (0.until(5)).forEach { value ->
            try {
                KLTBFileGenericHeader.create(value.downTo(0).map { it.toByte() }.toByteArray())
            } catch (e: FileHeaderParseException) {
                exceptionCounter = exceptionCounter.inc()
            }
        }

        assertEquals(5, exceptionCounter)
    }

    @Test(expected = FileHeaderParseException::class)
    fun create_size7_throwsHeaderParseException() {
        KLTBFileGenericHeader.create(6.downTo(0).map { it.toByte() }.toByteArray())
    }

    @Test
    fun create_specificPayloadSize_containsExpectedSpecificPayloadSize() {
        val expectedPayloadSize: Int = 943435989
        val byteList = mutableListOf(FileType.BRUSHING.value, 0)
        val byteBuffer = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(expectedPayloadSize)

        byteList.addAll(byteBuffer.array().toList())

        val header = KLTBFileGenericHeader.create(byteList.toByteArray())

        assertEquals(expectedPayloadSize.toLong(), header.payloadSize)
    }
}
