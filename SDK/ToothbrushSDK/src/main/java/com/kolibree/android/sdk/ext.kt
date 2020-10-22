package com.kolibree.android.sdk

import java.util.zip.CRC32

internal fun Byte?.toHex(): String {
    if (this == null) return "null"

    return "0x${Integer.toHexString(toPositiveInt())}"
}

internal fun Byte.toPositiveInt() = toInt() and 0xFF

internal fun List<Byte>.toHexList(): List<String> {
    return this.map { it.toHex() }
}

internal fun ByteArray.toHexList(): List<String> {
    return this.map { it.toHex() }
}

/**
 * Compute the standard CRC32 value for this byte array
 *
 * @return CRC32 [Long]
 */
internal fun ByteArray.computeCrc(): Long {
    val crc32 = CRC32()
    crc32.update(this)
    return crc32.value
}

internal fun ByteArray.toHexString(): String =
    joinToString(separator = "-") { String.format("%02X", it) }
