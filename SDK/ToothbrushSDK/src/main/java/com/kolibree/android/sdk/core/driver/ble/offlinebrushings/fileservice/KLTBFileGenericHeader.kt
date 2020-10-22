package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.toHexList
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Structure for Generic Header.
 *
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755025/Generic+file+format
 *
 * Size 6
 *
 * Byte 0 -> type
 * Byte 1 -> version
 * Bytes 2-4 -> specific payload size
 */
internal data class KLTBFileGenericHeader @VisibleForTesting constructor(
    val payloadSize: Long,
    val bytesLittleEndian: ByteArray
) {
    companion object {
        const val SIZE = 6

        fun create(bytesLittleEndian: ByteArray): KLTBFileGenericHeader {
            validateArgumentSize(bytesLittleEndian)

            // skip fileType and fileVersion. KML takes care of that
            val payloadReader = PayloadReader(bytesLittleEndian).skip(2)

            return KLTBFileGenericHeader(
                payloadSize = payloadReader.readUnsignedInt32(),
                bytesLittleEndian = bytesLittleEndian
            )
        }

        private fun validateArgumentSize(bytes: ByteArray) {
            if (bytes.size != SIZE) {
                throw FileHeaderParseException(
                    "Byte array must be of length $SIZE, was ${bytes.toHexList()}"
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KLTBFileGenericHeader) return false

        if (payloadSize != other.payloadSize) return false
        if (!bytesLittleEndian.contentEquals(other.bytesLittleEndian)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payloadSize.hashCode()
        result = 31 * result + bytesLittleEndian.contentHashCode()
        return result
    }
}

internal object KLTBFileHeaderParser {

    fun parse(dataFlowable: Flowable<Byte>): Single<KLTBFileGenericHeader> {
        return dataFlowable
            .buffer(KLTBFileGenericHeader.SIZE)
            .take(1)
            .map { KLTBFileGenericHeader.create(it.toByteArray()) }
            .firstOrError()
    }
}
