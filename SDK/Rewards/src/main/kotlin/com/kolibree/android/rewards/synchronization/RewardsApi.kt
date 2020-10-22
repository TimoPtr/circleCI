package com.kolibree.android.rewards.synchronization

import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeProgressApi
import com.kolibree.android.rewards.synchronization.challenges.ChallengesCatalogApi
import com.kolibree.android.rewards.synchronization.lifetimesmiles.LifetimeSmilesResponse
import com.kolibree.android.rewards.synchronization.prizes.PrizesCatalogApi
import com.kolibree.android.rewards.synchronization.profilesmiles.ProfileSmilesApi
import com.kolibree.android.rewards.synchronization.profilesmileshistory.ProfileSmilesHistoryApi
import com.kolibree.android.rewards.synchronization.profiletier.ProfileTierApi
import com.kolibree.android.rewards.synchronization.redeem.RedeemApi
import com.kolibree.android.rewards.synchronization.redeem.RedeemData
import com.kolibree.android.rewards.synchronization.tiers.TiersCatalogApi
import com.kolibree.android.rewards.synchronization.transfer.TransferApi
import com.kolibree.android.rewards.synchronization.transfer.TransferData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface RewardsApi {
    @GET("/v1/rewards/catalog/list/")
    fun getChallengesCatalog(): Call<ChallengesCatalogApi>

    @GET("/v1/rewards/tier/list/")
    fun getTiersCatalog(): Call<TiersCatalogApi>

    @GET("/v1/rewards/challenges/profile/list/{profileId}/")
    fun getChallengeProgress(@Path("profileId") profileId: Long): Call<ChallengeProgressApi>

    @GET("/v1/rewards/tier/profile/{profileId}/")
    fun getProfileTier(@Path("profileId") profileId: Long): Call<ProfileTierApi>

    @GET("/v1/rewards/smiles/profile/{profileId}/")
    fun getProfileSmiles(@Path("profileId") profileId: Long): Call<ProfileSmilesApi>

    @GET("/v1/rewards/prize/list/*/")
    fun getAllPrizes(): Call<PrizesCatalogApi>

    @GET("/v1/rewards/profile/history/{profileId}/all/{results}/")
    fun getSmilesHistory(
        @Path("profileId") profileId: Long,
        @Path("results") results: Int = SMILES_DEFAULT_NB_RESULTS
    ): Call<ProfileSmilesHistoryApi>

    @POST("/v1/rewards/redeem/smiles/")
    fun claimRedeem(@Body redeemData: RedeemData): Call<RedeemApi>

    @POST("/v1/rewards/transfer/smiles/")
    fun transferSmiles(@Body transferData: TransferData): Call<TransferApi>

    @GET("/v4/accounts/{accountId}/profiles/{profileId}/lifetime_points/")
    fun getLifetimePoints(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long
    ): Call<LifetimeSmilesResponse>
}

const val SMILES_DEFAULT_NB_RESULTS = 40
