/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.errorhandler

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

internal class ErrorHandlerInterceptor
@Inject constructor(private val errorHandler: NetworkErrorHandler) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        return errorHandler.accept(response)
    }
}
