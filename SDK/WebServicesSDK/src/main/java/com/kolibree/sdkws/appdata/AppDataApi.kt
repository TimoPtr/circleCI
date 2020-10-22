package com.kolibree.sdkws.appdata

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * App data webservice API definition
 */
internal interface AppDataApi {

    @GET("/v1/accounts/{accountId}/profiles/{profileId}/client_game_progress/")
    fun getAppData(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Single<AppDataImpl>

    @POST("/v1/accounts/{accountId}/profiles/{profileId}/client_game_progress/")
    fun saveAppData(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body appData: @JvmSuppressWildcards AppDataImpl
    ): Single<Response<Void>>
}
