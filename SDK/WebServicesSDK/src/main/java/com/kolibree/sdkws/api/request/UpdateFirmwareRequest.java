package com.kolibree.sdkws.api.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.sdkws.Constants;
import com.kolibree.sdkws.networking.RequestMethod;

/** Created by aurelien on 06/01/16. */
public class UpdateFirmwareRequest extends Request {

  public UpdateFirmwareRequest(
      @NonNull String fw, @NonNull String hw, @NonNull String model, @Nullable String serial) {
    super(RequestMethod.GET, buildUrl(fw, hw, model, serial));
  }

  @NonNull
  private static String buildUrl(
      @NonNull String fw, @NonNull String hw, @NonNull String model, @Nullable String serial) {

    final StringBuilder builder = new StringBuilder(Constants.SERVICE_CHECK_FW_UPDATE);
    builder.append("?").append("fw=").append(fw).append("&model=").append(model);

    if (!"kltb003".equals(model)) {
      builder.append("&hw=").append(hw);
    }

    if (serial != null && !serial.isEmpty()) {
      builder.append("&serial=").append(serial);
    }

    return builder.toString();
  }
}
