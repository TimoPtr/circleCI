package com.kolibree.sdkws.brushing

import com.kolibree.sdkws.brushing.models.BrushingApiModel
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.models.CreateMultipleBrushingSessionsBody
import com.kolibree.sdkws.data.model.DeleteBrushingData
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface
 */
internal interface BrushingApi {

    @HTTP(
        method = "DELETE",
        path = "/v2/accounts/{accountId}/profiles/{profileId}/brushings/",
        hasBody = true
    )
    fun deleteBrushing(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body body: DeleteBrushingData
    ): Single<Response<Void>>

    @GET("/v2/accounts/{accountId}/profiles/{profileId}/brushings/")
    fun getBrushings(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Query("pd") pd: Boolean = true,
        @Query("begin_date") beginDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("before_id") beforeBrushingId: Long? = null,
        @Query("after_id") afterBrushingId: Long? = null,
        @Query("limit") limit: Int? = null
    ): Single<Response<BrushingsResponse>>

    @POST("/v2/accounts/{accountId}/profiles/{profileId}/brushings/")
    fun createBrushings(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body brushingsToCreate: @JvmSuppressWildcards CreateMultipleBrushingSessionsBody
    ): Single<Response<BrushingsResponse>>

    @PUT("/v2/accounts/{accountId}/profiles/{profileId}/brushings/")
    fun assignBrushings(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Field("brushings") brushingsToCreate: @JvmSuppressWildcards List<BrushingApiModel>
    ): Single<Response<Void>>
}
