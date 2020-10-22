/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.data.api.model.PartnershipResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@VisibleForApp
interface PartnershipApi {

    @GET("/v1/rewards/account/{account_id}/profile/{profile_id}/partners/")
    fun getPartnerships(
        @Path("account_id") accountId: Long,
        @Path("profile_id") profileId: Long
    ): Single<Response<PartnershipResponse>>

    @POST("/v1/rewards/account/{account_id}/profile/{profile_id}/partner/{partner_id}/unlock/")
    fun unlockPartnership(
        @Path("account_id") accountId: Long,
        @Path("profile_id") profileId: Long,
        @Path("partner_id") partnerId: String
    ): Single<Response<Unit>>

    @POST("/v1/rewards/account/{account_id}/profile/{profile_id}/partner/{partner_id}/disable/")
    fun disablePartnership(
        @Path("account_id") accountId: Long,
        @Path("profile_id") profileId: Long,
        @Path("partner_id") partnerId: String
    ): Single<Response<Unit>>
}
