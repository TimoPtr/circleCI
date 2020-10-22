/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import androidx.annotation.Keep
import java.io.InputStream
import java.security.SecureRandom

/**
 * Extract from the first line of a stream the HEX array as a ByteArray
 */
@Keep
fun InputStream.extractHexToByteArray(): ByteArray = bufferedReader().use { bufferReader ->
    bufferReader.readLine()?.run { extractHexToByteArray() } ?: ByteArray(0)
}

@Keep
fun InputStream.extractStringFromStream(): String = bufferedReader().use { bufferReader ->
    bufferReader.readLine() ?: ""
}

@Suppress("MagicNumber")
@Keep
fun String.extractHexToByteArray(): ByteArray =
    trim().chunked(2).map { hexString -> hexString.toInt(16).toByte() }.toByteArray()

/**
 * Return a string of Hex like ffffff0a1
 */
@Keep
fun ByteArray.toCompactStringHex(): String = joinToString(separator = "") { String.format("%02X", it) }

@Keep
fun ByteArray.xor(array2: ByteArray): ByteArray =
    (0 until (Math.min(size, array2.size)))
        .map { index ->
            (this[index].toInt() xor array2[index].toInt()).toByte()
        }
        .toByteArray()

@Keep
fun generateRandomIV(ivSize: Int = 16): ByteArray = ByteArray(ivSize).apply {
    SecureRandom().nextBytes(this)
}
