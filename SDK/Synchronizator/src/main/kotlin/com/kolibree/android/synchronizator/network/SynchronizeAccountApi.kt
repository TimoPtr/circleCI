package com.kolibree.android.synchronizator.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface SynchronizeAccountApi {
    @GET("/v1/account/{accountId}/synchronize/")
    fun synchronizationInfo(
        @Path("accountId") accountId: Long,
        @QueryMap requestParameters: Map<String, Int>
    ): Call<SynchronizeAccountResponse>
}
