/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network

import com.google.common.base.Optional
import com.instabug.library.Instabug
import com.instabug.library.okhttplogger.InstabugOkhttpInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

@Module
internal object NetworkInterceptorModule {
    @Provides
    fun providesEmptyNetworkInterceptor(): Optional<Interceptor> {
        return Optional.of(StripSensitiveDataInstabugOkHttpInterceptor())
    }
}

/**
 * Interceptor to upload network logs to Instabug stripping Request and Response headers to avoid
 * logging sensitive data
 */
private class StripSensitiveDataInstabugOkHttpInterceptor : Interceptor {
    private val instabugOkHttpInterceptor = InstabugOkhttpInterceptor()

    override fun intercept(chain: Interceptor.Chain): Response {
        val realResponse = chain.proceed(chain.request())

        if (Instabug.isBuilt() && Instabug.isEnabled()) {
            instabugOkHttpInterceptor.intercept(chainForInstabug(chain, realResponse))
        }

        return realResponse
    }

    /**
     * We don't want to log request/response bodies to avoid exposing sensitive data, so we feed a
     * Chain that's stripped of headers and body fields
     */
    private fun chainForInstabug(
        chain: Interceptor.Chain,
        realResponse: Response
    ): Interceptor.Chain {
        val realRequest = chain.request()

        val instabugLoggingRequest = createInstabugLogRequest(realRequest)

        return object : Interceptor.Chain by chain {
            override fun request(): Request = instabugLoggingRequest

            override fun proceed(request: Request): Response {
                return Response.Builder()
                    .code(realResponse.code())
                    .request(instabugLoggingRequest)
                    .protocol(realResponse.protocol())
                    .message(realResponse.message())
                    .body(null)
                    .headers(realResponse.headers())
                    .build()
            }
        }
    }

    private fun createInstabugLogRequest(realRequest: Request): Request {
        val fakeRequestBody = realRequest.body()?.let {
            RequestBody.create(it.contentType(), "")
        }

        return Request.Builder()
            .url(realRequest.url())
            .method(realRequest.method(), fakeRequestBody)
            .build()
    }
}
