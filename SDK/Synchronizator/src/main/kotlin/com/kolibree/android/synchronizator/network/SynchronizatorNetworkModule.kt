package com.kolibree.android.synchronizator.network

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object SynchronizatorNetworkModule {

    @Provides
    internal fun providesAccountApiService(
        retrofit: Retrofit
    ): SynchronizeAccountApi {
        return retrofit.create(SynchronizeAccountApi::class.java)
    }
}
