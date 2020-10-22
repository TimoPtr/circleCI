/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.ota.kltb002.updater

import java.util.zip.CRC32

internal data class NextObjectHeader(
    @get:JvmName("dataToSend") val dataToSend: ByteArray,
    @get:JvmName("numberOfChunksToSend") val numberOfChunksToSend: Int,
    @get:JvmName("bytesInLastChunk") val bytesInLastChunk: Int,
    @get:JvmName("isLastObject") val isLastObject: Boolean
) {
    fun crc32(): Long {
        val crc32 = CRC32()
        crc32.update(dataToSend)
        return crc32.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NextObjectHeader

        if (!dataToSend.contentEquals(other.dataToSend)) return false
        if (numberOfChunksToSend != other.numberOfChunksToSend) return false
        if (bytesInLastChunk != other.bytesInLastChunk) return false
        if (isLastObject != other.isLastObject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dataToSend.contentHashCode()
        result = 31 * result + numberOfChunksToSend
        result = 31 * result + bytesInLastChunk
        result = 31 * result + isLastObject.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun create(
            data: ByteArray,
            totalChunksTransmitted: Int,
            chunksRemaining: Int
        ): NextObjectHeader {
            val chunksToSend: Int
            val bytesToSend: Int
            val bytesInLastChunk: Int
            var isLastObject = false
            if (chunksRemaining < CHUNKS_PER_OBJECT) {
                chunksToSend = chunksRemaining
                val totalBytesTransmitted: Int = totalChunksTransmitted * BYTES_PER_CHUNK
                bytesToSend = data.size - totalBytesTransmitted
                isLastObject = true
                val remaining: Int = bytesToSend % BYTES_PER_CHUNK
                bytesInLastChunk = if (remaining == 0) BYTES_PER_CHUNK else remaining
            } else {
                chunksToSend = CHUNKS_PER_OBJECT
                bytesToSend = CHUNKS_PER_OBJECT * BYTES_PER_CHUNK
                bytesInLastChunk = BYTES_PER_CHUNK
            }

            val offset: Int = totalChunksTransmitted * BYTES_PER_CHUNK
            val dataToSend = data.copyOfRange(offset, offset + bytesToSend)
            return NextObjectHeader(
                dataToSend,
                chunksToSend,
                bytesInLastChunk,
                isLastObject
            )
        }
    }
}

internal const val BYTES_PER_CHUNK = 20
internal const val CHUNKS_PER_OBJECT = 799
