/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.data.remote

import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.amazondash.data.model.AmazonDashSendTokenRequest
import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@VisibleForApp
interface AmazonDashApi {

    @POST("/v4/accounts/{account_id}/amazon/auth/")
    fun sendToken(
        @Path("account_id") accountId: Long,
        @Body body: AmazonDashSendTokenRequest
    ): Single<Response<ResponseBody>>

    @GET("/v4/amazon/alexa_link/")
    fun getLinks(): Single<Response<AmazonDashGetLinkResponse>>
}
