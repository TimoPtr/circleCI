package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileHeaderParseException
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.KLTBFileGenericHeader
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.KLTBFileHeaderParser
import io.reactivex.processors.PublishProcessor
import org.junit.Test

internal class KLTBFileHeaderParserTest : BaseUnitTest() {
    @Test
    fun parse_flowableError_emitsError() {
        val flowable: PublishProcessor<Byte> = mockFlowable()

        val observer = KLTBFileHeaderParser.parse(flowable).test()

        flowable.onNext(1)
        observer.assertNotComplete()

        flowable.onNext(3)
        observer.assertNotComplete()

        val expectedException = Exception("Test forced error")
        flowable.onError(expectedException)

        observer.assertError(expectedException)
    }

    @Test
    fun parse_flowableCompletesUnexpectedly_emitsHeaderParseException() {
        val flowable: PublishProcessor<Byte> = mockFlowable()

        val observer = KLTBFileHeaderParser.parse(flowable).test()

        val sentBytes: MutableList<Byte> = mutableListOf()

        flowable.subscribe(
            { value -> sentBytes.add(value) },
            Throwable::printStackTrace
        )

        flowable.onNext(FileType.BRUSHING.value)
        flowable.onNext(1)

        flowable.onComplete()

        observer.assertError(FileHeaderParseException::class.java)
    }

    @Test
    fun parse_properHeader_emitsKLTBFileHeader() {
        val flowable: PublishProcessor<Byte> = mockFlowable()

        val observer = KLTBFileHeaderParser.parse(flowable).test()

        val sentBytes: MutableList<Byte> = mutableListOf()

        flowable.subscribe(
            { value -> sentBytes.add(value) },
            Throwable::printStackTrace
        )

        val fileType = FileType.BRUSHING
        val version = 7
        val expectedRecords = 56
        val headerBytes = genericHeaderBytesFromNotification(fileType, version, expectedRecords)

        headerBytes.forEach { flowable.onNext(it) }

        val expectedHeader = KLTBFileGenericHeader.create(sentBytes.toByteArray())

        observer.assertValue(expectedHeader).assertComplete()
    }

    private fun mockFlowable() = PublishProcessor.create<Byte>()

    companion object {

        fun genericHeaderBytesFromNotification(
            fileType: FileType = FileType.BRUSHING,
            version: Int = 1,
            payloadSize: Int = DEFAULT_PAYLOAD_SIZE
        ): ByteArray {
            // notifications are emitted in little endian. The parser should deal with that
            return PayloadWriter(6)
                .writeByte(fileType.value)
                .writeByte(version.toByte())
                .writeInt32(payloadSize).bytes
        }

        private const val DEFAULT_PAYLOAD_SIZE = 450
    }
}

internal fun ByteArray.toBigEndian(): ByteArray = PayloadReader(this).bytes
