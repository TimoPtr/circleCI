package com.kolibree.android.sdk.core;

import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import org.jetbrains.annotations.NotNull;

public class KolibreeServiceHelper {
  static KLTBConnection createConnection(
      @NotNull KolibreeService service, String mac, String name, ToothbrushModel model) {
    return service.kltbConnectionPoolManager.create(mac, name, model);
  }
}
