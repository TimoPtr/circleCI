package com.kolibree.sdkws.retrofit

import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.retrofit.ConnectivityInterceptor
import com.kolibree.android.network.utils.NetworkChecker
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.Interceptor.Chain
import okhttp3.Protocol.HTTP_2
import okhttp3.Request.Builder
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectivityInterceptorTest {

    private val networkChecker = mock<NetworkChecker>()

    private val connectivityInterceptor = ConnectivityInterceptor(networkChecker)

    @Test(expected = ApiError::class)
    fun intercept_noNetwork_throwsApiError() {
        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        val chain = mock<Chain>()
        connectivityInterceptor.intercept(chain)
    }

    @Test
    fun intercept_withNetwork_proceedsWithChain() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        val response = okhttp3.Response.Builder()
            .request(Builder().url("http://www.example.com").build())
            .protocol(HTTP_2)
            .code(200)
            .message("")
            .build()

        val chain = mock<Chain>()

        whenever(chain.request()).thenReturn(response.request())

        whenever(chain.proceed(any())).thenReturn(response)

        assertEquals(response, connectivityInterceptor.intercept(chain))

        verify(chain).proceed(any())
    }
}
