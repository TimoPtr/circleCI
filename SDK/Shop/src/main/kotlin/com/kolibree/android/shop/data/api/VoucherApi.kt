/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.api

import com.kolibree.android.shop.data.api.model.VoucherRequest
import com.kolibree.android.shop.data.api.model.VoucherResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface VoucherApi {

    @POST("/v4/accounts/{accountId}/voucher/")
    fun getVoucher(
        @Path("accountId") accountId: Long,
        @Body voucherRequest: VoucherRequest
    ): Single<Response<VoucherResponse>>
}
