package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy.LegacyStoredBrushingsExtractor
import com.kolibree.android.sdk.error.BadRecordException
import com.kolibree.android.sdk.error.FailureReason
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mock

internal class LegacyOfflineBrushingsExtractorTest : BaseUnitTest() {
    @Mock
    lateinit var bleManager: KLNordicBleManager

    lateinit var offlineExtractor: LegacyStoredBrushingsExtractor

    override fun setup() {
        super.setup()

        offlineExtractor = spy(
            LegacyStoredBrushingsExtractor(
                bleManager
            )
        )
    }

    /*
    DELETE NEXT RECORD
     */

    @Test
    fun deleteNextRecord_redirectsToOfflineBrushingsInteractor() {
        whenever(bleManager.legacyDeleteNextBrushing()).thenReturn(Completable.complete())

        offlineExtractor.deleteRecord().test()

        verify(bleManager).legacyDeleteNextBrushing()
    }

    /*
    GET REMAINING RECORD COUNT
     */

    @Test
    @Throws(FailureReason::class)
    fun getRemainingRecordCount_redirectsToOfflineBrushingsInteractor() {
        val expectedRemainingCount = 894
        whenever(bleManager.getLegacyBrushingCount()).thenReturn(expectedRemainingCount)

        offlineExtractor.recordCount().test().assertValue(expectedRemainingCount)
    }

    @Test
    fun valueOf_ZONES_PER_SAMPLE() {
        Assert.assertEquals(4, LegacyStoredBrushingsExtractor.ZONES_PER_SAMPLE)
    }

    @Test
    fun valueOf_SAMPLES_PER_PACKET() {
        Assert.assertEquals(8, LegacyStoredBrushingsExtractor.SAMPLES_PER_PACKET)
    }

    @Test
    fun valueOf_RECORD_HEADER_LENGTH() {
        Assert.assertEquals(13, LegacyStoredBrushingsExtractor.RECORD_HEADER_LENGTH)
    }

    @Test
    fun valueOf_RECORD_PACKET_LENGTH() {
        Assert.assertEquals(20, LegacyStoredBrushingsExtractor.RECORD_PACKET_LENGTH)
    }

    @Test
    fun valueOf_RECORD_HEADER_FLAG() {
        Assert.assertEquals(0x01.toByte(), LegacyStoredBrushingsExtractor.RECORD_HEADER_FLAG)
    }

    @Test
    fun valueOf_RECORD_PACKET_FLAG() {
        Assert.assertEquals(0x02.toByte(), LegacyStoredBrushingsExtractor.RECORD_PACKET_FLAG)
    }

    @Test
    fun valueOf_GET_HEADER_COMMAND() {
        Assert.assertEquals(0x03.toByte(), LegacyStoredBrushingsExtractor.GET_HEADER_COMMAND)
    }

    @Test
    fun valueOf_GET_PACKET_COMMAND() {
        Assert.assertEquals(0x04.toByte(), LegacyStoredBrushingsExtractor.GET_PACKET_COMMAND)
    }

    @Test
    fun valueOf_GET_PACKET_COMMAND_LENGTH() {
        Assert.assertEquals(3, LegacyStoredBrushingsExtractor.GET_PACKET_COMMAND_LENGTH)
    }

    @Test
    fun recordCount_callsGetRemainingRecordCount() {
        whenever(bleManager.getLegacyBrushingCount()).thenReturn(3)
        val testObserver = offlineExtractor.recordCount().test()
        testObserver.assertComplete()
        testObserver.assertValue(3)
    }

    @Test
    fun deleteRecord_callsDeleteRecordCommand() {
        whenever(bleManager.legacyDeleteNextBrushing()).thenReturn(Completable.complete())
        val testObserver = offlineExtractor.deleteRecord().test()
        testObserver.assertComplete()
        verify(bleManager).legacyDeleteNextBrushing()
    }

    @Test
    fun getHeader_callsPopRecordCharCommandWithHeaderCommand() {
        offlineExtractor.getHeader()
        verify(bleManager).legacyPopRecordCommand(eq(byteArrayOf(0x03)))
    }

    @Test(expected = BadRecordException::class)
    fun checkHeader_badLength_throwsException() {
        offlineExtractor.checkHeader(byteArrayOf(0x01, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A))
    }

    @Test(expected = BadRecordException::class)
    fun checkHeader_badFlag_throwsException() {
        offlineExtractor.checkHeader(
            byteArrayOf(
                0x00,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A
            )
        )
    }

    @Test
    fun checkHeader_goodData_doesNotThrowException() {
        offlineExtractor.checkHeader(GOOD_HEADER)
    }

    @Test
    fun parseHeader_callsCheckHeader() {
        offlineExtractor.parseHeader(GOOD_HEADER)
        verify(offlineExtractor).checkHeader(eq(GOOD_HEADER))
    }

    @Test
    fun parseHeader_works() {
        val header = offlineExtractor.parseHeader(GOOD_HEADER)
        Assert.assertEquals(0x7CBFCDBEL, header.crc)
        Assert.assertEquals(1490790501L, header.timestamp)
        Assert.assertEquals(290, header.sampleCount)
        Assert.assertEquals(500, header.samplingPeriodMillis)
    }

    @Test
    fun getPacket_callsPopRecordCharCommandWithGoodPayload() {
        offlineExtractor.getPacket(33)
        verify(bleManager).legacyPopRecordCommand(eq(byteArrayOf(0x04, 0x21, 0x00)))
    }

    @Test(expected = BadRecordException::class)
    fun checkPacket_badLength_throwsException() {
        offlineExtractor.checkPacket(byteArrayOf(0x02, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A))
    }

    @Test(expected = BadRecordException::class)
    fun checkPacket_badFlag_throwsException() {
        offlineExtractor.checkHeader(
            byteArrayOf(
                0x01,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A,
                0x0A
            )
        )
    }

    @Test
    fun checkPacket_goodData_doesNotThrowException() {
        offlineExtractor.checkPacket(GOOD_PACKET)
    }

    @Test
    fun parseHeader_callsCheckPacket() {
        offlineExtractor.parsePacket(GOOD_PACKET)
        verify(offlineExtractor).checkPacket(eq(GOOD_PACKET))
    }

    @Test
    fun computeEventTimestampTenthSeconds_firstEvent_works() {
        Assert.assertEquals(0, offlineExtractor.computeSampleTimestampTenthSeconds(0, 0, 500))
    }

    @Test
    fun computeEventTimestampTenthSeconds_nthEvent_works() {
        Assert.assertEquals(
            515,
            offlineExtractor.computeSampleTimestampTenthSeconds(12, 7, 500)
        )
    }

    @Test
    fun parsePacket_callsCheckPacket() {
        offlineExtractor.parsePacket(GOOD_PACKET)
        verify(offlineExtractor).checkPacket(eq(GOOD_PACKET))
    }

    @Test
    fun parsePacket_correctlyParsesPacket() {
        val parsedPacket = offlineExtractor.parsePacket(GOOD_PACKET)
        Assert.assertEquals(36, parsedPacket.packetIndex)
        Assert.assertEquals(0xBF.toByte(), parsedPacket.vibratorBitmask.get())
        Assert.assertArrayEquals(
            intArrayOf(
                0x0101,
                0x0101,
                0x0101,
                0x0101,
                0xB612,
                0x0101,
                0x0101,
                0x0101
            ), parsedPacket.zones
        )
    }

    @Test
    fun parseSampleZones_works() {
        val zones = offlineExtractor.parseSampleZones(0xB612)
        Assert.assertEquals(MouthZone16.UpMolRiOcc, zones[0])
        Assert.assertEquals(MouthZone16.LoIncExt, zones[1])
        Assert.assertEquals(MouthZone16.LoMolLeExt, zones[2])
        Assert.assertEquals(MouthZone16.LoMolLeInt, zones[3])
    }

    @Test
    fun computeBrushingDurationMillis_works() {
        assertEquals(
            20000,
            offlineExtractor.computeBrushingDurationMillis(40, 500)
        )
    }

    @Test
    fun computePacketCount_onlyFullPackets() {
        Assert.assertEquals(11, offlineExtractor.computePacketCount(88))
    }

    @Test
    fun computePacketCount_someSamplesMore() {
        Assert.assertEquals(12, offlineExtractor.computePacketCount(89))
    }

    companion object {

        private val GOOD_HEADER = byteArrayOf(
            0x01,
            0xBE.toByte(),
            0xCD.toByte(),
            0xBF.toByte(),
            0x7C.toByte(),
            0x65.toByte(),
            0xA8.toByte(),
            0xDB.toByte(),
            0x58.toByte(),
            0x22.toByte(),
            0x01,
            0xF4.toByte(),
            0x01
        )

        private val GOOD_PACKET = byteArrayOf(
            0x02,
            0x24,
            0x00,
            0xBF.toByte(),
            0x01,
            0x01,
            0x01,
            0x01,
            0x01,
            0x01,
            0x01,
            0x01,
            0x12,
            0xB6.toByte(),
            0x01,
            0x01,
            0x01,
            0x01,
            0x01,
            0x01
        )
    }
}
