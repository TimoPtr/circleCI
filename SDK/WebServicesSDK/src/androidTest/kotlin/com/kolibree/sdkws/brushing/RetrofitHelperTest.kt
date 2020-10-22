package com.kolibree.sdkws.brushing

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/**
 * Use mock retrofit for the test.
 *
 */
open class RetrofitHelperTest {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://test.com").client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val networkBehavior by lazy {
        val network = NetworkBehavior.create()
        network.setFailurePercent(0)
        network
    }

    val mockRetrofit = MockRetrofit.Builder(retrofit)
        .networkBehavior(networkBehavior)
        .build()

    inline fun <reified T> generateDelegate(): BehaviorDelegate<T> {
        return mockRetrofit.create(T::class.java)
    }
}
