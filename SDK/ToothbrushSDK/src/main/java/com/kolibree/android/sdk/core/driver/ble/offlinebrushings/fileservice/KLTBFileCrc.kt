package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.toHexList
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * 4 Bytes in little endian that specify the expected CRC for a KLTBFile
 */
internal data class KLTBFileCrc(val bytes: List<Byte>) {
    companion object {
        const val SIZE = 4
    }

    val crc: Long

    init {
        validateArgumentSize()

        crc = PayloadReader(bytes.toByteArray()).readUnsignedInt32()
    }

    private fun validateArgumentSize() {
        if (bytes.size != SIZE) {
            throw FileCrcParseException(
                "Byte array must be of length $SIZE, was ${bytes.toHexList()}"
            )
        }
    }
}

internal object KLTBFileCrcParser {

    fun parse(dataFlowable: Flowable<Byte>): Single<KLTBFileCrc> {
        return dataFlowable
            .buffer(KLTBFileCrc.SIZE)
            .take(1)
            .map { KLTBFileCrc(it) }
            .firstOrError()
    }
}
