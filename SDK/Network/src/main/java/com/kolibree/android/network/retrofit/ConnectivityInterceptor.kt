package com.kolibree.android.network.retrofit

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.utils.NetworkChecker
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

@VisibleForApp
class ConnectivityInterceptor
@Inject constructor(private val networkChecker: NetworkChecker) : Interceptor {
    override fun intercept(chain: Chain): Response {
        if (networkChecker.hasConnectivity()) {
            val builder = chain.request().newBuilder()

            return chain.proceed(builder.build())
        }

        throw ApiError.generateNetworkError()
    }
}
