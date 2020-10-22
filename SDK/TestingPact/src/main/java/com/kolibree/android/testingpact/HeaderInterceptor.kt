/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testingpact

import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import okhttp3.Interceptor
import okhttp3.Response
import org.apache.commons.codec.binary.Base64

class HeaderInterceptor : Interceptor {

    internal companion object {
        const val BASE_URL = "https://localhost:8081"

        const val ACCESS_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhY2NvdW50X2lkIjoxLCJjbGllbnRfaWQiOjEsImV4cCI6MzEzNjA5NzE5MX0.8iyCyucoxWqdkVyWPjIQ-_EBrMtUO9tv7E8LHb7Epkdna3wjtyBljh-OKHJLaNsXiuCNHJaFxmXT-ZYoMrNyRw"
        const val CLIENT_ID = 1
        const val CLIENT_SECRET = "1234567890"

        private const val ALGORITHM = "HmacSHA256"
        private val CHARSET = Charset.forName("UTF-8")

        @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
        fun clientSignature(url: String): String {
            val sha256HMAC = Mac.getInstance(ALGORITHM)

            val secretKeySpec = SecretKeySpec(
                String(
                    CLIENT_SECRET.toByteArray(),
                    CHARSET
                ).toByteArray(),
                ALGORITHM
            )

            sha256HMAC.init(secretKeySpec)

            val urlUTF8 = String(
                url.toByteArray(),
                CHARSET
            )
            sha256HMAC.update(urlUTF8.toByteArray())
            return Base64.encodeBase64String(sha256HMAC.doFinal())
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder()
            .addHeader("x-access-token", ACCESS_TOKEN)
            .addHeader("x-client-id", CLIENT_ID.toString())
            .addHeader("x-client-secret", CLIENT_SECRET)
            .addHeader("x-client-sig", clientSignature(BASE_URL + chain.request().url().url().path))
            .build())
    }
}
