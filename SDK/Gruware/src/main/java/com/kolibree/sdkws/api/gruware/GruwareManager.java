package com.kolibree.sdkws.api.gruware;

import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.sdkws.api.response.GruwareResponse;
import io.reactivex.Single;

@VisibleForApp
public interface GruwareManager {

  Single<GruwareResponse> getGruwareInfos(
      String model, String hw, String serial, String firmwareVersion);
}
