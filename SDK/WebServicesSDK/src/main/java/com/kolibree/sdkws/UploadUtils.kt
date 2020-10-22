@file:JvmName("UploadUtils")

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.sdkws

import io.reactivex.Completable
import java.io.IOException
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio

internal fun uploadByteArray(url: String, byteArray: ByteArray): Completable {
    return Completable.create { emitter ->
        byteArray.inputStream().use { inputStream ->
            val requestBody = object : RequestBody() {
                override fun contentType() = MediaType.parse("application/octet-stream")

                override fun contentLength(): Long {
                    return inputStream.available().toLong()
                }

                override fun writeTo(sink: BufferedSink) {
                    Okio.source(inputStream).use { source ->
                        sink.writeAll(source)

                        Util.closeQuietly(source)
                    }
                }
            }

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful)
                emitter.tryOnError(IOException("Response failed $response"))
            else {
                emitter.onComplete()
            }
        }
    }
}
