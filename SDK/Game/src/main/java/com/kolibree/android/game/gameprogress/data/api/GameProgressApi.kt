/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.api

import com.kolibree.android.game.gameprogress.data.api.model.GameProgressRequest
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressResponse
import com.kolibree.android.game.gameprogress.data.api.model.ProfileGameProgressResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface GameProgressApi {

    @GET("/v1/games/account/{accountId}/profiles/{profileId}/")
    fun getProfileGameProgress(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Single<Response<ProfileGameProgressResponse>>

    @GET("/v1/{gameId}/account/{accountId}/profiles/{profileId}/progress")
    fun getGameProgress(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Path("gameId") gameId: String
    ): Single<Response<GameProgressResponse>>

    @POST("/v1/{gameId}/account/{accountId}/profiles/{profileId}/progress")
    fun setGameProgress(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Path("gameId") gameId: String,
        @Body progress: GameProgressRequest
    ): Single<Response<GameProgressResponse>>
}
