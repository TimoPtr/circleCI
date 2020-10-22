/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import android.content.Context
import android.util.Base64
import androidx.annotation.Keep
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.kolibree.android.errors.NetworkNotAvailableException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * 007.
 *
 *
 * Never prevent Proguard from obfuscating this class (no rule in conf file and no @Keep
 * annotation)
 *
 *
 * /!\ If you do so the following message will appear in clear and never auto-destroy
 *
 *
 * The password is xored with the name of the class: [NetworkNotAvailableException] If you
 * rename this class make sure to update all the encrypted secrets
 */

@Keep
class KolibreeGuard @Inject
internal constructor() {

    @VisibleForTesting
    val xorPassword: String
        get() = NetworkNotAvailableException::class.java.simpleName

    /**
     * Unencrypt the string parameter.
     *
     * @param encrypted the encripted string we want to unencrypt
     * @return the unencripted string
     * @throws Exception if something went wrong decoding the string
     */
    @Throws(Exception::class)
    fun reveal(
        encrypted: String,
        iv: ByteArray,
        key: ByteArray
    ): String {
        val base64Data = Base64.decode(encrypted, Base64.DEFAULT)
        return String(reveal(base64Data, iv, key))
    }

    @Throws(Exception::class)
    fun reveal(
        encrypted: String,
        iv: ByteArray
    ): String {
        val key = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
        return reveal(encrypted, iv, key)
    }

    @Throws(Exception::class)
    fun revealFromRaw(
        context: Context,
        @RawRes encryptedFileRes: Int,
        @RawRes ivFileRes: Int,
        key: ByteArray = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
    ): ByteArray {
        val bytes = context.resources.openRawResource(encryptedFileRes).use { it.readBytes() }
        val iv =
            context.resources.openRawResource(ivFileRes).bufferedReader().use { it.readLine() }.extractHexToByteArray()

        return decodeAes(bytes, key, iv)
    }

    @Throws(Exception::class)
    fun revealFromStringBase64(
        context: Context,
        @StringRes encryptedStringRes: Int,
        @StringRes ivString: Int,
        key: ByteArray = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
    ): ByteArray {
        val bytes = Base64.decode(context.getString(encryptedStringRes), Base64.DEFAULT)
        val iv = context.getString(ivString).extractHexToByteArray()

        return decodeAes(bytes, key, iv)
    }

    @Throws(Exception::class)
    fun revealFromString(
        context: Context,
        @StringRes encryptedStringRes: Int,
        @StringRes ivString: Int,
        key: ByteArray = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
    ): ByteArray {
        val bytes = context.getString(encryptedStringRes).extractHexToByteArray()
        val iv = context.getString(ivString).extractHexToByteArray()

        return decodeAes(bytes, key, iv)
    }

    @Throws(Exception::class)
    fun reveal(
        bytes: ByteArray,
        iv: ByteArray,
        key: ByteArray = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
    ): ByteArray = decodeAes(bytes, key, iv)

    @Throws(Exception::class)
    fun encrypt(
        bytes: ByteArray,
        iv: ByteArray,
        key: ByteArray = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
    ): ByteArray = encodeAes(bytes, key, iv)

    @Throws(Exception::class)
    fun encrypt(
        text: String,
        iv: ByteArray,
        key: ByteArray = OBFUSCATED_KEY.xor(xorPassword.toByteArray())
    ): String = String(Base64.encode(encrypt(text.toByteArray(), iv, key), Base64.DEFAULT))

    companion object {

        @VisibleForTesting
        fun createInstance(): KolibreeGuard = KolibreeGuard()

        /** Xored AES passkey.  */
        private val OBFUSCATED_KEY =
            byteArrayOf(0x4a, 0x18, 0x48, 0x47, 0x5d, 0x20, 0x1f, 0x00, 0x56, 0x43, 0x2f, 0x49, 0x5d, 0x4e, 0x5c, 0x05)

        /** Decryption algorithm.  */
        private const val ALGORITHM = "AES"

        private const val ALGORITHM_DETAIL = "$ALGORITHM/CBC/PKCS7Padding"

        @Throws(Exception::class)
        private fun encodeAes(message: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
            val secretKey = SecretKeySpec(key, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM_DETAIL)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            return cipher.doFinal(message)
        }

        @Throws(Exception::class)
        private fun decodeAes(encodedMessage: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
            val secretKey = SecretKeySpec(key, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM_DETAIL)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

            return cipher.doFinal(encodedMessage)
        }
    }
}
