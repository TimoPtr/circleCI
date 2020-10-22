/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data.api

import com.kolibree.android.questionoftheday.data.api.model.request.AnswerQuestionRequest
import com.kolibree.android.questionoftheday.data.api.model.request.QuestionApiResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface QuestionApi {

    @GET(QUESTION_REQUEST)
    fun fetchQuestion(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Query("timezone") timezone: String
    ): Single<Response<QuestionApiResponse>>

    @PUT(QUESTION_REQUEST)
    fun sendAnswer(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body body: AnswerQuestionRequest
    ): Single<Response<ResponseBody>>
}

private const val QUESTION_REQUEST = "/v1/rewards/{accountId}/{profileId}/question/"
