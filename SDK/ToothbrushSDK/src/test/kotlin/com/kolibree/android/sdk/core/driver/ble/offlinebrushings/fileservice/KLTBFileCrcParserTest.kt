package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import io.reactivex.processors.PublishProcessor
import org.junit.Assert.assertEquals
import org.junit.Test

class KLTBFileCrcParserTest : BaseUnitTest() {

    @Test
    fun parse_flowableError_emitsError() {
        val flowable: PublishProcessor<Byte> = mockFlowable()

        val observer = KLTBFileCrcParser.parse(flowable).test()

        flowable.onNext(1)
        observer.assertNotComplete()

        flowable.onNext(3)
        observer.assertNotComplete()

        val expectedException = Exception("Test forced error")
        flowable.onError(expectedException)

        observer.assertError(expectedException)
    }

    @Test
    fun parse_flowableCompletesUnexpectedly_emitsFileCrcParseException() {
        val flowable: PublishProcessor<Byte> = mockFlowable()

        val observer = KLTBFileCrcParser.parse(flowable).test()

        val sentBytes: MutableList<Byte> = mutableListOf()

        flowable.subscribe(
            { value -> sentBytes.add(value) },
            Throwable::printStackTrace
        )

        flowable.onNext(FileType.BRUSHING.value)
        flowable.onNext(1)

        flowable.onComplete()

        observer.assertError(FileCrcParseException::class.java)
    }

    @Test
    fun parse_properHeader_emitsKLTBFileCrc() {
        val flowable: PublishProcessor<Byte> = mockFlowable()

        val observer = KLTBFileCrcParser.parse(flowable).test()

        val sentBytes: MutableList<Byte> = mutableListOf()

        flowable.subscribe(
            { value -> sentBytes.add(value) },
            Throwable::printStackTrace
        )

        val crcBytesFromFile = byteArrayOf(31, 23, 10, 99) // crc = 1661605663
        val crcByteLoader = PayloadReader(crcBytesFromFile)
        val expectedCrc: Long = crcByteLoader.readUnsignedInt32()

        val crcBytes = properCrcBytes(expectedCrc.toInt())

        crcBytes.forEach { flowable.onNext(it) }

        val kltbFileCrc = KLTBFileCrc(sentBytes)

        observer.assertValue(kltbFileCrc).assertComplete()

        assertEquals(expectedCrc, kltbFileCrc.crc)
    }

    private fun mockFlowable() = PublishProcessor.create<Byte>()

    companion object {
        fun properCrcBytes(crc: Int = 1234): List<Byte> {
            val payloadWriter = PayloadWriter(4)

            payloadWriter.writeInt32(crc)

            return payloadWriter.bytes.toList()
        }
    }
}
