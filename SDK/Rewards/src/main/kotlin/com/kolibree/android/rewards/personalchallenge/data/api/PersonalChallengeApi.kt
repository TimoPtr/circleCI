/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.api

import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeRequest
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface PersonalChallengeApi {

    @GET("/v1/rewards/personal-challenges/profile/{profileId}/")
    fun getChallenge(@Path("profileId") profileId: Long): Single<Response<PersonalChallengeResponse>>

    @POST("/v1/rewards/personal-challenges/profile/{profileId}/")
    fun createChallenge(
        @Path("profileId") profileId: Long,
        @Body request: PersonalChallengeRequest
    ): Single<Response<PersonalChallengeResponse>>

    @PUT("/v1/rewards/personal-challenges/profile/{profileId}/")
    fun updateChallenge(
        @Path("profileId") profileId: Long,
        @Body request: PersonalChallengeRequest
    ): Single<Response<PersonalChallengeResponse>>

    @DELETE("/v1/rewards/personal-challenges/profile/{profileId}/")
    fun deleteChallenge(@Path("profileId") profileId: Long): Single<Response<Void>>
}
