package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.util.HexUtils
import io.reactivex.processors.PublishProcessor
import org.junit.Assert.assertNotNull
import org.junit.Test

class KLTBFileParserTest : BaseUnitTest() {

    private val fileParser = KLTBFileParser()

    @Test
    fun parseBrushing_brushing_2244_11_58_10_full() {
        val fileName = "brushing_2244_11-58-10_full.bin"

        val bytes = fileToByteArray(fileName)

        assertNotNull(bytes)

        /*
        bytes         [1, 1, -63, 5, 0, 0, 121, 65, 2, 91, 0, -17, 0, -12, 1, -1, -23, -16, 0, 81, 44]
        header        [1, 1, -63, 5, 0, 0]
        brush header                       [121, 65, 2, 91, 0, -17, 0, -12, 1]
        sample record                                                         [-1, -23, -16, 0, 81, 44, ...])
         */

        val flowableProcessor = PublishProcessor.create<Byte>()

        val observer = fileParser.parse(flowableProcessor).test()

        observer.assertNotComplete()

        bytes.forEach { flowableProcessor.onNext(it) }

        observer.assertComplete()
    }

    @Test
    fun parseBrushing_brushing_2244_11_58_10_full_pause() {
        val fileName = "brushing_2244_11-58-10_full_pause.bin"

        val bytes = fileToByteArray(fileName)

        assertNotNull(bytes)

        val flowableProcessor = PublishProcessor.create<Byte>()

        val observer = fileParser.parse(flowableProcessor).test()

        observer.assertNotComplete()

        bytes.forEach { flowableProcessor.onNext(it) }

        observer.assertComplete()
    }

    @Test
    fun `parseBrushing real content`() {
        val headerHexString = """01-01-CD-00-00-00"""
        val bodyhexString =
            """A3-E6-A6-5D-00-20-00-F4-01-FF-CF-F7-FF-0C-A4-33-D8-F4-F6-0B-0A-1B-DB-F3-F5-02-04-06-DB-F2-F6-02-06-09-DC-F1-F5-02-0D-0F-DD-F0-F5-02-0E-10-DD-EF-F5-02-10-0F-DE-EE-F5-02-0D-0C-FF-DE-EE-F4-03-0D-0A-DF-ED-F4-03-0E-09-DF-ED-F4-01-0D-0B-DF-ED-F4-01-0C-0C-DF-ED-F4-01-0A-0E-DF-EC-F4-02-09-0E-DF-EC-F4-02-08-0F-E0-EC-F4-02-09-0E-FF-E0-EC-F4-02-08-0D-E0-EC-F4-01-09-0D-E0-EC-F4-01-0B-0A-E0-EC-F4-01-0C-07-DF-EC-F4-01-0A-07-DF-EC-F4-02-0A-08-DF-ED-F4-02-08-0B-DF-ED-F4-02-08-0D-FF-DF-ED-F4-02-08-0C-DF-ED-F4-01-09-0A-E4-EC-EE-0A-29-1E-F3-EA-DF-02-0B-0B-F4-EA-DE-00-06-07-F3-EA-DE-01-06-06-F1-EA-DF-01-06-06-EF-E9-E1-02-06-0A"""
        val crcHexString = """6F-E2-47-06"""
        val extraBytesToBeIgnored =
            """00-01-01-CD-00-00-00-A3-E6-A6-5D-00-20-00-F4-01-FF-CF-F7-FF-0C"""

        val byteArray =
            HexUtils.hexStringToByteArray(headerHexString + bodyhexString + crcHexString + extraBytesToBeIgnored)

        val flowableProcessor = PublishProcessor.create<Byte>()

        val observer = fileParser.parse(flowableProcessor).test()

        observer.assertNotComplete()

        byteArray.forEach { flowableProcessor.onNext(it) }

        val expectedFile = KLTBBrushingFile(
            fileHeader = KLTBFileGenericHeader.create(HexUtils.hexStringToByteArray(headerHexString)),
            brushingBytesLittleEndian = HexUtils.hexStringToByteArray(bodyhexString)
        )

        observer.assertComplete().assertValue(expectedFile)
    }

    private fun fileToByteArray(path: String): ByteArray {
        javaClass.classLoader?.getResourceAsStream("file_service/v1/$path").use {
            return it?.readBytes()!!
        }
    }
}
