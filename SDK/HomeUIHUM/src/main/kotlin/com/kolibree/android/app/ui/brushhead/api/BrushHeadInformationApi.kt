/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.api

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.brushhead.api.model.request.BrushHeadInformationResponse
import com.kolibree.android.app.ui.brushhead.api.model.request.data.BrushHeadData
import com.kolibree.android.commons.models.StrippedMac
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@VisibleForApp
interface BrushHeadInformationApi {

    @POST(BRUSH_HEAD_REQUEST)
    fun updateBrushHead(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Path("serialNumber") serialNumber: String,
        @Path("macAddress") macAddress: StrippedMac,
        @Body body: BrushHeadData
    ): Single<Response<BrushHeadInformationResponse>>

    @GET(BRUSH_HEAD_REQUEST)
    fun getBrushHeadInformation(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Path("serialNumber") serialNumber: String,
        @Path("macAddress") macAddress: StrippedMac
    ): Single<Response<BrushHeadInformationResponse>>
}

private const val BRUSH_HEAD_REQUEST =
    "/v4/accounts/{accountId}/profiles/{profileId}/brushhead/{serialNumber}/{macAddress}/"
