package com.kolibree.android.sdk.core.ota.kltb002.updates

import com.kolibree.android.sdk.computeCrc
import com.kolibree.android.sdk.version.SoftwareVersion

/** Firmware and rom update base implementation of [OtaUpdate]  */
internal abstract class BaseFirmwareUpdate(
    final override val data: ByteArray,
    version: String,
    backendProvidedCrc: Long?
) : OtaUpdate {

    final override val version = SoftwareVersion(version)

    final override val crc = backendProvidedCrc ?: data.computeCrc()

    final override val type = OtaUpdate.TYPE_FIRMWARE

    // Downgrading firmware is not allowed
    final override fun isCompatible(version: SoftwareVersion) = this.version.isNewerOrSame(version)

    @Suppress("TooGenericExceptionThrown")
    @Throws(Exception::class)
    final override fun checkCRC() {
        val computedCrc = data.computeCrc()

        if (computedCrc != crc) {
            throw Exception("CRC mismatch, expected $crc got $computedCrc")
        }
    }
}
