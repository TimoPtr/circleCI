/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.binary

import androidx.annotation.AnyThread
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import timber.log.Timber

/**
 * Fluent payload writer
 *
 *
 * This class helps to write a payload without dealing with bytes
 *
 *
 * Thread safe but should not be used across several threads
 */
@AnyThread
class PayloadWriter
/**
 * Create a payload writer
 *
 * @param length the length of the payload
 */
    (length: Int) {

    /** Internal byte buffer  */
    private val buffer: ByteBuffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN)

    /**
     * Get the resulting byte array
     *
     *
     * All non written indexes are padded with zeros
     *
     * @return non null byte array
     */
    val bytes: ByteArray
        @Synchronized get() = buffer.array()

    /**
     * Write a byte to the payload
     *
     * @param b byte
     * @return the instance of this PayloadWriter
     */
    @Synchronized
    fun writeByte(b: Byte): PayloadWriter {
        buffer.put(b)
        return this
    }

    @Synchronized
    fun writeUnsignedInt8(number: Short): PayloadWriter {
        if (number > MAX_UNSIGNED_INT8) { // it's unsigned
            Timber.e(
                "\n***WARNING**\n\nNumber %s won't fit to one byte. Unexpected behavior ahead", number
            )
        }
        if (number < 0) {
            Timber.e("\n***WARNING**\n\nNumber %s is below zero. Unexpected behavior ahead", number)
        }
        buffer.put(number.toByte())
        return this
    }

    /**
     * Write an unsigned 2-bytes integer
     *
     * @param number unsigned integer
     * @return non null [PayloadWriter] instance
     */
    @Synchronized
    fun writeUnsignedInt16(number: Int): PayloadWriter {
        if (number > MAX_UNSIGNED_SHORT) { // it's unsigned
            Timber.e(
                "\n***WARNING**\n\nNumber %s won't fit to 2 bytes. Unexpected behavior ahead", number
            )
        }
        if (number < 0) {
            Timber.e("\n***WARNING**\n\nNumber %s is below zero. Unexpected behavior ahead", number)
        }
        buffer.putShort((number and 0x0000FFFF).toShort())
        return this
    }

    /**
     * Write an signed 2-bytes integer
     *
     * @param number short
     * @return non null [PayloadWriter] instance
     */
    @Synchronized
    fun writeInt16(number: Short): PayloadWriter {
        buffer.putShort(number)
        return this
    }

    /**
     * Write 4-bytes signed integer
     *
     * @param i integer
     * @return non null [PayloadWriter] instance
     */
    @Synchronized
    fun writeInt32(i: Int): PayloadWriter {
        buffer.putInt(i)
        return this
    }

    /**
     * Write a byte array to the payload
     *
     * @param array non null byte array
     * @return this instance
     */
    @Synchronized
    fun writeByteArray(array: ByteArray): PayloadWriter {
        buffer.put(array)
        return this
    }

    /**
     * Write a date in YMDhms format (will write 6 bytes)
     * We makes sure that the date that we send to the brush is in UTC format
     *
     * @param date non null Date
     * @return this instance
     */
    @Synchronized
    fun writeDate(date: OffsetDateTime): PayloadWriter {
        val utcDate = date.withOffsetSameInstant(ZoneOffset.UTC)
        writeByte((utcDate.year - BASE_YEAR).toByte())
        writeByte(utcDate.month.value.toByte())
        writeByte(utcDate.dayOfMonth.toByte())
        writeByte(utcDate.hour.toByte())
        writeByte(utcDate.minute.toByte())
        writeByte(utcDate.second.toByte())

        return this
    }

    /**
     * Convert a String to the UTF-8 charset and write it to the buffer
     *
     *
     * Make sure that you properly checked the length of the input string (some characters like 'é'
     * require two bytes)
     *
     * @param s non null String
     * @return this instance
     */
    @Synchronized
    fun writeString(s: String): PayloadWriter {
        writeByteArray(s.toByteArray(Charset.forName("UTF-8")))
        return this
    }

    /**
     * Write a hardware version (4 bytes)
     *
     * @param version non null HardwareVersion
     * @return this instance
     */
    @Synchronized
    fun writeHardwareVersion(version: HardwareVersion): PayloadWriter =
        writeUnsignedInt16(version.major).writeUnsignedInt16(version.minor)

    /**
     * Write a software version (4 bytes)
     *
     * @param version non null SoftwareVersion
     * @return this instance
     */
    @Synchronized
    fun writeSoftwareVersion(version: SoftwareVersion): PayloadWriter = writeByte(version.major.toByte())
        .writeByte(version.minor.toByte())
        .writeUnsignedInt16(version.revision)

    /** Clear payload for reuse  */
    @Synchronized
    fun clear() {
        buffer.clear()
    }

    companion object {
        const val MAX_UNSIGNED_SHORT = 65535
        const val MAX_UNSIGNED_INT8 = 255
        private const val BASE_YEAR = 2000
    }
}
