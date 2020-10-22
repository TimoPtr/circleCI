/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.test.TestForcedException
import io.reactivex.processors.PublishProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Test

internal class KLTBBrushingFileTest : BaseUnitTest() {

    @Test
    fun parse_flowableError_emitsError() {
        val flowable = mockFlowable()

        val observer = KLTBBrushingFileReader.read(flowable, fileHeader()).test()

        flowable.onNext(1)
        observer.assertNotComplete()

        flowable.onNext(3)
        observer.assertNotComplete()

        flowable.onError(TestForcedException())

        observer.assertError(TestForcedException::class.java)
    }

    @Test
    fun parse_flowableCompletesUnexpectedly_emitsFileParseException() {
        val flowable = mockFlowable()

        val observer = KLTBBrushingFileReader.read(flowable, fileHeader()).test()

        val sentBytes: MutableList<Byte> = mutableListOf()

        flowable.subscribe(
            { value -> sentBytes.add(value) },
            Throwable::printStackTrace
        )

        flowable.onNext(FileType.BRUSHING.value)
        flowable.onNext(1)

        flowable.onComplete()

        observer.assertError(FileParseException::class.java)
    }

    @Test
    fun parse_properFile_emitsKLTBBrushingFile() {
        val brushingHeaderBytes = properBrushingHeaderBytes(
            timestamp = TrustedClock.getNowInstantUTC().epochSecond.toInt(),
            sampleCount = 1,
            samplingPeriod = 3,
            isFakeBrushing = false
        )
        val brushingRecordBytes = properRecordBytes()

        val flowable = mockFlowable()

        val singleSubscription = flowable.share()

        val fileBytes = brushingHeaderBytes + brushingRecordBytes

        val header = fileHeader(payloadSize = fileBytes.size.toLong())
        val expectedBrushingFile = KLTBBrushingFile(header, fileBytes)

        val observer = KLTBBrushingFileReader.read(
            singleSubscription,
            header
        ).test()

        brushingHeaderBytes.forEach { flowable.onNext(it) }

        observer.assertNotComplete()

        brushingRecordBytes.forEach { flowable.onNext(it) }

        observer.assertValue(expectedBrushingFile).assertComplete()
    }

    private fun mockFlowable() = PublishProcessor.create<Byte>()

    /*
    https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755032/Brushings+file+format+V1
     */
    private fun properBrushingHeaderBytes(
        timestamp: Int = 0,
        sampleCount: Short = 0,
        samplingPeriod: Short = 0,
        isFakeBrushing: Boolean = false
    ): ByteArray {
        val isFakeBrushingByte: Byte = if (isFakeBrushing) 1 else 0

        return ByteBuffer
            .allocate(9)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(timestamp)
            .put(isFakeBrushingByte)
            .putShort(sampleCount)
            .putShort(samplingPeriod)
            .array()
    }

    /**
     * @return file with 1 sample record
     */
    private fun properRecordBytes(): ByteArray {
        return ByteBuffer
            .allocate(7)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(0) // active status bitfield
            .put(10) // roll
            .put(11) // pitch
            .put(12) // yaw
            .put(13) // acc X stddev
            .put(14) // acc Y stddev
            .put(15) // acc Z stddev
            .array()
    }

    private fun fileHeader(
        fileType: FileType = FileType.BRUSHING,
        fileVersion: Byte = 1,
        payloadSize: Long = 500L
    ) =
        KLTBFileGenericHeader(
            payloadSize = payloadSize,
            bytesLittleEndian = arrayOf(fileType.value, fileVersion).toByteArray() +
                PayloadWriter(4).writeInt32(payloadSize.toInt()).bytes.asList().toByteArray()
        )
}
