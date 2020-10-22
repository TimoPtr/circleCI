package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sdk.computeCrc
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal interface KLTBFile {
    val bytes: ByteArray

    fun crc() = bytes.computeCrc()
}

/**
 * Deals with
 * 1. Reading selected file header. Gets file type, version and payload size
 * 2. Asking for X number of bytes
 * 3. Doing specific work depending on file type. For brushings,
 *  3.1 Reads the brushing header (timestamp, is fake, sample count, sampling period)
 *  3.2 Reads N samples
 * 4. Reading CRC and validating the file
 */
internal class KLTBFileParser @Inject constructor() {
    fun parse(dataFlowable: Flowable<Byte>): Single<KLTBFile> {
        val singleSubscription = dataFlowable.replay()

        var flowableConnect: Disposable? = null

        return KLTBFileHeaderParser.parse(singleSubscription)
            .doOnSubscribe { flowableConnect = singleSubscription.connect() }
            .flatMap { header ->
                KLTBBrushingFileReader.read(
                    singleSubscription.skip(header.bytesLittleEndian.size.toLong()),
                    header
                )
            }
            .flatMap { file ->
                KLTBFileCrcParser.parse(singleSubscription.skip(file.bytes.size.toLong()))
                    .map { parsedCrc ->
                        if (parsedCrc.crc != file.crc()) {
                            throw FileInvalidCrcException("CRC was ${file.crc()}, expected ${parsedCrc.crc}")
                        }

                        file
                    }
            }
            .doOnTerminate { flowableConnect.forceDispose() }
    }
}
