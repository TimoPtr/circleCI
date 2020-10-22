/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.binary

import androidx.annotation.AnyThread
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toCurrentTimeZone
import com.kolibree.android.sdk.math.Vector
import com.kolibree.android.sdk.util.ByteUtils
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.Arrays
import java.util.Objects
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

/**
 * Payload buffered reader utility Provides simple way to read and parse payload bytes
 *
 *
 * This class is thread safe but reading payloads from multiple threads is strongly discouraged
 */
@AnyThread
class PayloadReader(data: ByteArray) {

    /** Internal little endian buffer  */
    private val buffer: ByteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

    /**
     * Get payload length
     *
     * @return payload byte count
     */
    @JvmField
    val length: Int = data.size

    val bytes: ByteArray
        @Synchronized get() = Arrays.copyOf(buffer.array(), buffer.array().size)

    /**
     * Read a signed 1 byte integer
     *
     * @return signed 1 byte integer
     */
    @Synchronized
    fun readInt8(): Byte = buffer.get()

    /**
     * Read a signed 2 bytes integer
     *
     * @return signed 2 bytes integer
     */
    @Synchronized
    fun readInt16(): Short = buffer.short

    /**
     * Read an unsigned 4 bytes integer (unsigned int)
     *
     * @return an unsigned 4 bytes integer (unsigned int)
     */
    @Synchronized
    fun readUnsignedInt32(): Long = buffer.int.toLong() and 0x00000000FFFFFFFFL

    /**
     * Read an unsigned 2 bytes integer (unsigned short)
     *
     * @return an unsigned 2 bytes integer (unsigned short)
     */
    @Synchronized
    fun readUnsignedInt16(): Int = buffer.short.toInt() and 0x0000FFFF

    /**
     * Read an unsigned 1 byte integer (unsigned byte)
     *
     * @return an unsigned 1 byte integer (unsigned byte)
     */
    @Synchronized
    fun readUnsignedInt8(): Short = (buffer.get().toInt() and 0x00FF).toShort()

    /**
     * Read a boolean (consumes a byte)
     *
     * @return boolean value of read byte
     */
    @Synchronized
    fun readBoolean(): Boolean = buffer.get().toInt() != 0x00

    /**
     * Read a float (4 bytes)
     *
     * @return float
     */
    @Synchronized
    fun readFloat(): Float = buffer.float

    /**
     * Skip bytes in the buffer
     *
     * @param count number of bytes to skip
     * @return the instance of this payload reader to chain calls
     */
    @Synchronized
    fun skip(count: Int): PayloadReader {
        repeat(count) {
            buffer.get()
        }

        return this
    }

    /**
     * Read a String
     *
     * @param byteCount length of the String to read
     * @return non null String
     */
    @Synchronized
    fun readString(byteCount: Int): String {
        val b = ByteArray(byteCount)

        for (i in 0 until byteCount) {
            b[i] = buffer.get()
        }

        return String(b, Charset.forName("UTF-8"))
    }

    /**
     * Read a vector of 3 floats (consumes 12 bytes)
     *
     * @return non null [Vector]
     */
    @Synchronized
    fun readVector(): Vector = Vector(readFloat(), readFloat(), readFloat())

    /**
     * Read a hardware version (consumes 4 bytes)
     *
     * @return non null [HardwareVersion]
     */
    @Synchronized
    fun readHardwareVersion(): HardwareVersion =
        HardwareVersion(readUnsignedInt16(), readUnsignedInt16())

    /**
     * Read a software version (consumes 4 bytes)
     *
     * @return non null [SoftwareVersion]
     */
    @Synchronized
    fun readSoftwareVersion(): SoftwareVersion =
        SoftwareVersion(readUnsignedInt8().toInt(), readUnsignedInt8().toInt(), readUnsignedInt16())

    /**
     * Read a DSP version using the deprecated protocol (consumes 8 bytes)
     *
     * @return [DspVersion]
     */
    @Synchronized
    fun readDspVersionCompat() = DspVersion(
        readUnsignedInt16(),
        readUnsignedInt16(),
        readUnsignedInt32()
    )

    /**
     * Read a DSP version (consumes 6 bytes)
     *
     * @return [DspVersion]
     */
    @Synchronized
    fun readDspVersion() = DspVersion(
        readUnsignedInt16(),
        readUnsignedInt16(),
        readUnsignedInt16().toLong()
    )

    /**
     * Read a 6 bytes formatted date (the date set in the TB is in UTC)
     *
     * @return non null [OffsetDateTime] in the current system timezone
     */
    @Synchronized
    fun readDate(): OffsetDateTime = OffsetDateTime.of(
        BASE_YEAR + readInt8(), // Year
        readInt8().toInt(), // Month
        readInt8().toInt(), // Day of month
        readInt8().toInt(), // Hour of day
        readInt8().toInt(), // Minute
        readInt8().toInt(), // Second
        0,
        ZoneOffset.UTC
    ).toCurrentTimeZone()

    @Synchronized
    fun readDateFromUnixTimeStamp(): OffsetDateTime = OffsetDateTime
        .ofInstant(Instant.ofEpochSecond(readUnsignedInt32()), TrustedClock.systemZone)

    /** Rewind the payload reader  */
    @Synchronized
    fun rewind(): PayloadReader {
        buffer.rewind()
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is PayloadReader) {
            return false
        }
        val that = other as PayloadReader?
        return length == that!!.length && buffer == that.buffer
    }

    override fun hashCode(): Int = Objects.hash(buffer, length)

    override fun toString(): String =
        "PayloadReader{" + "buffer=" + printBuffer() + ", length=" + length + '}'.toString()

    private fun printBuffer(): String = ByteUtils.prettyPrint(buffer.array(), length).toString()

    companion object {
        private const val BASE_YEAR = 2000
    }
}
