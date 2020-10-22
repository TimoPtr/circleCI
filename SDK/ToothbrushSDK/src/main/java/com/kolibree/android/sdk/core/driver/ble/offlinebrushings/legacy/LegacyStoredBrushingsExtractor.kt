/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.RecordedSession
import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushingsExtractor
import com.kolibree.android.sdk.error.BadRecordException
import com.kolibree.android.sdk.util.MouthZoneIndexMapper
import com.kolibree.kml.MouthZone16
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

internal class LegacyStoredBrushingsExtractor(private val bleManager: KLNordicBleManager) :
    OfflineBrushingsExtractor {

    override fun popRecord(): Single<OfflineBrushing> {
        return Single.create {
            try {
                val header = parseHeader(getHeader())
                val events = grabRecordData(
                    computePacketCount(header.sampleCount),
                    header.samplingPeriodMillis
                )

                val recordedSession = RecordedSession(
                    header.dateTime(),
                    computeBrushingDurationMillis(
                        header.sampleCount,
                        header.samplingPeriodMillis
                    ).toLong(),
                    events.toTypedArray()
                )

                it.onSuccess(recordedSession.toOfflineBrushing())
            } catch (exception: BadRecordException) {
                it.tryOnError(exception)
            }
        }
    }

    override fun deleteRecord(): Completable {
        return bleManager.legacyDeleteNextBrushing()
    }

    @Suppress("TooGenericExceptionCaught")
    override fun recordCount(): Single<Int> {
        return Single.create {
            try {
                it.onSuccess(bleManager.getLegacyBrushingCount())
            } catch (exception: Exception) {
                it.tryOnError(exception)
            }
        }
    }

    /**
     * No-op, introduced for new file system
     */
    override fun startExtractFileSession(): Completable = Completable.complete()

    /**
     * No-op, introduced for new file system
     */
    override fun finishExtractFileSession(): Completable = Completable.complete()

    private fun grabRecordData(
        packetCount: Int,
        samplingPeriodMillis: Int
    ): List<RecordedSession.Event> {
        val events = arrayListOf<RecordedSession.Event>()
        for (packetIndex in 0 until packetCount) {
            val packet = parsePacket(getPacket(packetIndex))
            for (sampleIndex in 0 until SAMPLES_PER_PACKET) {
                if (packet.zones[sampleIndex] != EMPTY_SAMPLE) {
                    events.add(
                        RecordedSession.Event(
                            computeSampleTimestampTenthSeconds(
                                packetIndex,
                                sampleIndex,
                                samplingPeriodMillis
                            ),
                            packet.vibratorBitmask.getBit(sampleIndex),
                            parseSampleZones(packet.zones[sampleIndex])[0]
                        )
                    )
                }
            }
        }

        return events
    }

    @VisibleForTesting
    fun getHeader(): ByteArray {
        return bleManager.legacyPopRecordCommand(byteArrayOf(GET_HEADER_COMMAND))
    }

    @VisibleForTesting
    @Throws(BadRecordException::class)
    fun parseHeader(header: ByteArray): RecordHeader {
        checkHeader(header)
        val reader = PayloadReader(header)
        return RecordHeader(
            reader.skip(1).readUnsignedInt32(),
            reader.readUnsignedInt32(),
            reader.readUnsignedInt16(),
            reader.readUnsignedInt16()
        )
    }

    @VisibleForTesting
    @Throws(BadRecordException::class)
    fun checkHeader(header: ByteArray) {
        if (header.size != RECORD_HEADER_LENGTH) {
            throw BadRecordException("Record header length is supposed to be $RECORD_HEADER_LENGTH")
        }

        if (header[0] != RECORD_HEADER_FLAG) {
            throw BadRecordException("Record header flag is supposed to be $RECORD_HEADER_FLAG")
        }
    }

    @VisibleForTesting
    fun getPacket(packetIndex: Int): ByteArray {
        val command = PayloadWriter(GET_PACKET_COMMAND_LENGTH)
            .writeByte(GET_PACKET_COMMAND)
            .writeUnsignedInt16(packetIndex)
            .bytes
        return bleManager.legacyPopRecordCommand(command)
    }

    @VisibleForTesting
    @Throws(BadRecordException::class)
    fun parsePacket(packet: ByteArray): RecordPacket {
        checkPacket(packet)
        val reader = PayloadReader(packet)
        val packetIndex = reader.skip(1).readUnsignedInt16()
        val vibratorBitmask = Bitmask(reader.readInt8())
        val zones = IntArray(SAMPLES_PER_PACKET)

        for (i in 0 until SAMPLES_PER_PACKET) {
            zones[i] = reader.readUnsignedInt16()
        }

        return RecordPacket(
            packetIndex,
            vibratorBitmask,
            zones
        )
    }

    @VisibleForTesting
    fun computeSampleTimestampTenthSeconds(
        packetIndex: Int,
        sampleIndex: Int,
        samplingPeriodMillis: Int
    ): Int = (packetIndex * SAMPLES_PER_PACKET + sampleIndex) * samplingPeriodMillis / 100

    @VisibleForTesting
    fun parseSampleZones(rawZones: Int): Array<MouthZone16> = arrayOf(
        MouthZoneIndexMapper.mapZoneIdToMouthZone16(rawZones shr MOST_PROBABLE_ZONE_SHIFT),
        MouthZoneIndexMapper.mapZoneIdToMouthZone16(rawZones shr MOST_PROBABLE_ZONE_2_SHIFT and 0xF),
        MouthZoneIndexMapper.mapZoneIdToMouthZone16(rawZones shr MOST_PROBABLE_ZONE_3_SHIFT and 0xF),
        MouthZoneIndexMapper.mapZoneIdToMouthZone16(rawZones and 0xF)
    )

    @VisibleForTesting
    @Throws(BadRecordException::class)
    fun checkPacket(packet: ByteArray) {
        if (packet.size != RECORD_PACKET_LENGTH) {
            throw BadRecordException("Record packet length is supposed to be $RECORD_PACKET_LENGTH")
        }

        if (packet[0] != RECORD_PACKET_FLAG) {
            throw BadRecordException("Record packet flag is supposed to be $RECORD_PACKET_FLAG")
        }
    }

    @VisibleForTesting
    fun computeBrushingDurationMillis(sampleCount: Int, samplingPeriodMillis: Int): Int {
        return sampleCount * samplingPeriodMillis
    }

    @VisibleForTesting
    fun computePacketCount(sampleCount: Int): Int {
        val fullPacketCount = sampleCount / SAMPLES_PER_PACKET
        return if (sampleCount % SAMPLES_PER_PACKET != 0) fullPacketCount + 1 else fullPacketCount
    }

    @VisibleForTesting
    internal data class RecordHeader(
        val crc: Long,
        val timestamp: Long,
        val sampleCount: Int,
        val samplingPeriodMillis: Int
    ) {

        /**
        Returns Record brushing time at current TZ
         */
        fun dateTime(): OffsetDateTime {
            /*
            Toothbrush sends creation time in epoch seconds
             */
            return timestamp.toothbrushTimestampToOffsetDateTime()
        }
    }

    @Suppress("ArrayInDataClass")
    @VisibleForTesting
    internal data class RecordPacket(
        val packetIndex: Int,
        val vibratorBitmask: Bitmask,
        val zones: IntArray
    )

    /*
    https://drive.google.com/file/d/0B1ID5klvd2-VLUZFMWlfZEpsMUU
     */
    companion object {

        @VisibleForTesting
        const val ZONES_PER_SAMPLE = 4

        @VisibleForTesting
        const val SAMPLES_PER_PACKET = 8

        @VisibleForTesting
        const val RECORD_HEADER_LENGTH = 13

        @VisibleForTesting
        const val RECORD_PACKET_LENGTH = 20

        @VisibleForTesting
        const val RECORD_HEADER_FLAG: Byte = 0x01

        @VisibleForTesting
        const val RECORD_PACKET_FLAG: Byte = 0x02

        @VisibleForTesting
        const val GET_HEADER_COMMAND: Byte = 0x03

        @VisibleForTesting
        const val GET_PACKET_COMMAND: Byte = 0x04

        @VisibleForTesting
        const val GET_PACKET_COMMAND_LENGTH = 3

        private const val MOST_PROBABLE_ZONE_SHIFT = 12

        private const val MOST_PROBABLE_ZONE_2_SHIFT = 8

        private const val MOST_PROBABLE_ZONE_3_SHIFT = 4

        // We can get empty sample when the record is finished
        private const val EMPTY_SAMPLE = 0x0000
    }
}

@Keep
fun RecordedSession.toOfflineBrushing(): OfflineBrushing =
    object : OfflineBrushing {
        override val datetime: LocalDateTime = date.toLocalDateTime()
        override val duration: Duration = Duration.of(getDuration(), ChronoUnit.MILLIS)
        override val processedData: String = computeProcessedData() ?: ""
    }

/**
 * Given a Long representing Epoch time in user's timezone, return a ZonedDateTime in UTC
 */
internal fun Long.toothbrushTimestampToOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.ofInstant(Instant.ofEpochSecond(this), TrustedClock.systemZone)
