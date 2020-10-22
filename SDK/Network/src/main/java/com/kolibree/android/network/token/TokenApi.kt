package com.kolibree.android.network.token

import com.kolibree.android.network.NetworkConstants.SERVICE_REFRESH_TOKEN_V3
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

internal interface TokenApi {

    @GET(SERVICE_REFRESH_TOKEN_V3)
    fun refreshToken(
        @Query("access_token") accessToken: String,
        @Query("refresh_token") refreshToken: String,
        @Query("duration") duration: Int? = null, // Works only on staging, do not use elsewhere!
        @Query("test") test: Boolean? = null // Works only on staging, do not use elsewhere!
    ): Single<ResponseBody>
}
