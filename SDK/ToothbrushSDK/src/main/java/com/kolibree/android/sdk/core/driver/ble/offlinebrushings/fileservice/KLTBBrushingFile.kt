package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import io.reactivex.Flowable
import io.reactivex.Single

@VisibleForTesting(otherwise = PRIVATE)
internal data class KLTBBrushingFile(
    val fileHeader: KLTBFileGenericHeader,
    val brushingBytesLittleEndian: ByteArray
) : KLTBFile {
    override val bytes: ByteArray = fileHeader.bytesLittleEndian + brushingBytesLittleEndian

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KLTBBrushingFile) return false

        if (fileHeader != other.fileHeader) return false
        if (!brushingBytesLittleEndian.contentEquals(other.brushingBytesLittleEndian)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileHeader.hashCode()
        result = 31 * result + brushingBytesLittleEndian.contentHashCode()
        return result
    }
}

/**
 * Stores the number of bytes specified in the header without attempting to parse them
 */
internal object KLTBBrushingFileReader {
    fun read(dataFlowable: Flowable<Byte>, fileHeader: KLTBFileGenericHeader): Single<KLTBFile> {
        val nbOfBrushingBytes = fileHeader.payloadSize
        return dataFlowable
            .take(nbOfBrushingBytes)
            .collectInto(
                ArrayList<Byte>(nbOfBrushingBytes.toInt()),
                { list, value -> list.add(value) })
            .map {
                if (it.size < nbOfBrushingBytes) throw FileParseException("File stream completed too early")

                it.toByteArray()
            }
            .map {
                KLTBBrushingFile(
                    fileHeader = fileHeader,
                    brushingBytesLittleEndian = it
                )
            }
    }
}
