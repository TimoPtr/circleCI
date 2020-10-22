package com.kolibree.sdkws.api.gruware;

import com.kolibree.sdkws.api.response.GruwareResponse;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface GruwareApi {

  @GET("/v2/gruware/")
  Single<GruwareResponse> getGruwareInfos(
      @Query("model") String model,
      @Query("hw") String hw,
      @Query("serial") String serial,
      @Query("fw") String fw);
}
