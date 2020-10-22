/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.amazondash.data.model.AmazonDashSendTokenRequest
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.amazondash.di.AmazonDashCoreModule
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicReference
import okhttp3.ResponseBody
import retrofit2.Response

@Module(includes = [AmazonDashCoreModule::class])
internal class EspressoAmazonDashModule {

    @AppScope
    @Provides
    fun providesFakeAmazonApi(): FakeAmazonDashApi {
        return FakeAmazonDashApi()
    }

    @Provides
    fun providesAmazonDashApi(
        fakeAmazonDashApi: FakeAmazonDashApi
    ): AmazonDashApi = fakeAmazonDashApi
}

internal class FakeAmazonDashApi : AmazonDashApi {

    private val tokenResponse = AtomicReference<Single<Response<ResponseBody>>>()
    private val linksResponse = AtomicReference<Single<Response<AmazonDashGetLinkResponse>>>()

    override fun sendToken(
        accountId: Long,
        body: AmazonDashSendTokenRequest
    ): Single<Response<ResponseBody>> {
        return tokenResponse.getAndSet(null)
            ?: error("You probably forgot to call FakeAmazonDashApi::mockTokenResponse.")
    }

    override fun getLinks(): Single<Response<AmazonDashGetLinkResponse>> {
        return linksResponse.getAndSet(null)
            ?: error("You probably forgot to call FakeAmazonDashApi::mockLinksResponse.")
    }

    fun mockTokenResponse(response: Response<ResponseBody>) {
        mockTokenResponse(Single.just(response))
    }

    fun mockTokenResponse(response: Single<Response<ResponseBody>>) {
        tokenResponse.set(response)
    }

    fun mockLinksResponse(response: Response<AmazonDashGetLinkResponse>) {
        mockLinksResponse(Single.just(response))
    }

    fun mockLinksResponse(response: Single<Response<AmazonDashGetLinkResponse>>) {
        linksResponse.set(response)
    }
}
