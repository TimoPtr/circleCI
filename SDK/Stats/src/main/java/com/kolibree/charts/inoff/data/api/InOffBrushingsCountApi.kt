/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.data.api

import com.kolibree.charts.inoff.data.api.model.InOffBrushingsCountResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface InOffBrushingsCountApi {

    @GET("/v1/stats/accounts/{account_id}/profiles/{profile_id}/offline_inapp_brushings/")
    fun getInOffBrushingsCount(
        @Path("account_id") accountId: Long,
        @Path("profile_id") profileId: Long
    ): Call<InOffBrushingsCountResponse>
}
