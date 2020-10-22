package com.kolibree.sdkws.api.gruware;

import androidx.annotation.NonNull;
import com.kolibree.sdkws.api.response.GruwareResponse;
import io.reactivex.Single;
import javax.inject.Inject;

final class GruwareManagerImpl implements GruwareManager {

  private final GruwareApi gruwareApi;

  @Inject
  GruwareManagerImpl(GruwareApi gruwareApi) {
    this.gruwareApi = gruwareApi;
  }

  @NonNull
  public Single<GruwareResponse> getGruwareInfos(
      String model, String hw, String serial, String firmwareVersion) {
    return gruwareApi.getGruwareInfos(model, hw, serial, firmwareVersion);
  }
}
